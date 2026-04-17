package com.k423.planewar.audio;
import javax.sound.sampled.*;
import java.util.*;
/** 程序化音频引擎：预合成音效 + 实时音乐音序器 + 单混音线程，无声卡时静默降级。 */
public final class AudioEngine implements AutoCloseable {
    private static final int SR = 44100, BUFFER_FRAMES = 4096, MIX_FRAMES = SR / 100;
    private static boolean warned;
    private final Map<Sfx, short[]> sounds;
    private final List<Voice> voices = new ArrayList<>();
    private final Object lock = new Object();
    private final MusicSynth music = new MusicSynth();
    private final SourceDataLine line;
    private final boolean available;
    private Thread mixer;
    private volatile boolean running, closed;
    private volatile float sfxVolume = 0.8f, musicVolume = 0.7f;
    public AudioEngine() {
        sounds = SfxSynth.synthesizeAll();
        SourceDataLine l = null;
        boolean ok = false;
        try {
            AudioFormat fmt = new AudioFormat(SR, 16, 1, true, false);
            l = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, fmt));
            l.open(fmt, BUFFER_FRAMES * 2);
            l.start();
            ok = true;
        } catch (Exception e) {
            if (l != null) { try { l.close(); } catch (Exception ignored) { } }
            l = null;
            warnSilent();
        }
        line = l;
        available = ok;
        if (ok) {
            running = true;
            mixer = new Thread(this::mixLoop, "planewar-audio");
            mixer.setDaemon(true);
            mixer.start();
        }
    }
    public boolean isAvailable() { return available; }
    public void play(Sfx sfx) { play(sfx, 1f, 1f); }
    public void play(Sfx sfx, float volume, float pitch) {
        if (!available || closed || sfx == null) return;
        short[] pcm = sounds.get(sfx);
        volume = clamp(volume);
        if (pcm == null || volume <= 0f) return;
        pitch = Math.max(0.5f, Math.min(2f, pitch));
        synchronized (lock) {
            if (voices.size() >= 24) voices.remove(0);
            voices.add(new Voice(pcm, volume, pitch));
        }
    }
    public void startMusic(int track) {
        if (available && !closed && track >= 0 && track <= 3) music.setTrack(track);
    }
    public void stopMusic() { startMusic(0); }
    public void setMusicVolume(float volume) { musicVolume = clamp(volume); }
    public void setSfxVolume(float volume) { sfxVolume = clamp(volume); }
    public float musicVolume() { return musicVolume; }
    public float sfxVolume() { return sfxVolume; }
    @Override
    public synchronized void close() {
        if (closed) return;
        closed = true;
        running = false;
        if (line != null) {
            try { line.stop(); } catch (Exception ignored) { }
            try { line.close(); } catch (Exception ignored) { }
        }
        if (mixer != null) {
            try { mixer.join(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
    private void mixLoop() {
        byte[] bytes = new byte[MIX_FRAMES * 2];
        float[] sfx = new float[MIX_FRAMES];
        float[] mus = new float[MIX_FRAMES];
        while (running) {
            Arrays.fill(sfx, 0f);
            Arrays.fill(mus, 0f);
            synchronized (lock) {
                for (int i = voices.size() - 1; i >= 0; i--) {
                    if (!voices.get(i).mixInto(sfx)) voices.remove(i);
                }
            }
            music.render(mus);
            float sv = sfxVolume, mv = musicVolume;
            for (int i = 0; i < MIX_FRAMES; i++) {
                short v = (short) Math.round(Math.tanh(sfx[i] * sv + mus[i] * mv) * 32767);
                bytes[i * 2] = (byte) v;
                bytes[i * 2 + 1] = (byte) (v >> 8);
            }
            try { line.write(bytes, 0, bytes.length); } catch (Exception e) { break; }
        }
    }
    private static float clamp(float v) { return v < 0f ? 0f : Math.min(v, 1f); }
    private static synchronized void warnSilent() {
        if (warned) return;
        warned = true;
        System.err.println("[audio] 音频设备不可用，已进入静默模式");
    }
    /** 正在播放的音效实例；loop 为扩展位，当前所有 SFX 一次性播放。 */
    private static final class Voice {
        private final short[] pcm;
        private final double step;
        private final float volume;
        private final boolean loop = false;
        private double pos;
        Voice(short[] pcm, float volume, float pitch) {
            this.pcm = pcm; this.volume = volume; this.step = pitch;
        }
        boolean mixInto(float[] out) {
            for (int i = 0; i < out.length; i++) {
                int i0 = (int) pos;
                if (i0 >= pcm.length - 1) { if (!loop) return false; pos = 0; i0 = 0; }
                double frac = pos - i0;
                double s = pcm[i0] + (pcm[i0 + 1] - pcm[i0]) * frac;
                out[i] += (float) (s / 32768.0 * volume);
                pos += step;
            }
            return true;
        }
    }
}
