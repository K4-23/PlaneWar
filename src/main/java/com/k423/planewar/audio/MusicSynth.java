package com.k423.planewar.audio;

import java.util.Random;

import static com.k423.planewar.audio.Synth.Wave.*;

/** 实时音序器：按采样推进 16 分音符，旋律与鼓点全部现场合成。 */
final class MusicSynth {
    private static final int SR = Synth.SR;
    private static final double[] BPM = {0, 84, 148, 160};
    private static final int[][] MENU = {{60, 64, 67, 71}, {57, 60, 64, 67}, {53, 57, 60, 64}, {55, 59, 62, 65}};
    private static final int[][] BATTLE = {{57, 60, 64}, {53, 57, 60}, {48, 52, 55}, {55, 59, 62}};
    private static final int[] BOSS_ROOTS = {40, 39, 38, 37};
    private static final int[] ARP = {0, 1, 2, 1, 2, 0, 1, 2};

    private final Voice[] voices = new Voice[24];
    private final Random rnd = new Random(0xA0D10);
    private int active, requested, step, bar;
    private double stepLen, next;
    private float gain;

    MusicSynth() {
        for (int i = 0; i < voices.length; i++) voices[i] = new Voice();
    }

    synchronized void setTrack(int track) {
        if (track != requested) requested = track;
    }

    synchronized void render(float[] out) {
        if (requested != active) { // 先淡出再切轨，避免爆音
            gain = Math.max(0f, gain - 0.2f);
            if (gain <= 0f) {
                active = requested;
                reset();
            }
        } else if (active != 0 && gain < 1f) {
            gain = Math.min(1f, gain + 0.1f);
        }
        if (active == 0) return;
        for (int i = 0; i < out.length; i++) {
            if (next <= 0) { fire(); next += stepLen; }
            next -= 1;
            double sum = 0;
            for (Voice v : voices) if (v.active) sum += v.next();
            out[i] += (float) (sum * gain);
        }
    }

    private void reset() {
        step = 0; bar = 0; next = 0; gain = 0;
        stepLen = active == 0 ? 1 : 60.0 / BPM[active] / 4.0 * SR;
        for (Voice v : voices) v.active = false;
    }

    private void fire() {
        switch (active) {
            case 1 -> menu();
            case 2 -> battle();
            case 3 -> boss();
        }
        if (++step >= 16) { step = 0; bar = (bar + 1) % 4; }
    }

    /** 菜单：C 大调 I-vi-IV-V，慢速 pad + 稀疏琶音，BPM 84。 */
    private void menu() {
        int[] ch = MENU[bar];
        double barDur = 16 * stepLen / SR;
        if (step == 0) {
            for (int m : ch) tone(m, 0.09, barDur, 0.18, 3.0, TRI);
            tone(ch[0] - 12, 0.11, barDur, 0.02, 2.5, TRI);
        }
        if ((step & 3) == 0) tone(ch[(step >> 2) & 3] + 12, 0.10, 0.4, 0.004, 0.12, SINE);
    }

    /** 战斗：A 小调 i-VI-III-VII，16 分琶音 + 底鼓/军鼓，BPM 148。 */
    private void battle() {
        int[] ch = BATTLE[bar];
        if (step == 0) tone(ch[0] - 12, 0.16, 16 * stepLen / SR, 0.01, 1.2, TRI);
        if ((step & 3) == 0) sweep(150, 45, 0.4, 0.16, 0.05);
        if (step == 4 || step == 12) snare(0.15);
        int n = ch[ARP[step & 7]] + 12 + ((step & 8) != 0 ? 12 : 0);
        tone(n, 0.065, 0.11, 0.003, 0.05, TRI);
    }

    /** Boss：半音下行贝斯 + 密集镲，BPM 160。 */
    private void boss() {
        int root = BOSS_ROOTS[bar];
        if ((step & 1) == 0) tone(root, 0.16, 0.34, 0.004, 0.12, TRI);
        if ((step & 3) == 0) sweep(150, 42, 0.32, 0.14, 0.05);
        noise(step == 2 || step == 10 ? 0.07 : 0.04, 0.035, 0.012);
        if (step == 4 || step == 12) snare(0.10);
    }

    private void tone(int midi, double amp, double dur, double att, double tau, Synth.Wave w) {
        double f = freq(midi);
        spawn(f, f, amp, dur, att, tau, w, 0.5, false);
    }

    private void sweep(double f0, double f1, double amp, double dur, double tau) {
        spawn(f0, f1, amp, dur, 0.001, tau, SINE, 0.5, false);
    }

    private void snare(double amp) {
        spawn(1, 1, amp, 0.11, 0.001, 0.03, null, 0.5, true);
        spawn(190, 120, amp * 0.5, 0.07, 0.001, 0.02, TRI, 0.5, false);
    }

    private void noise(double amp, double dur, double tau) {
        spawn(1, 1, amp, dur, 0.001, tau, null, 0.5, true);
    }

    private void spawn(double f0, double f1, double amp, double dur, double att, double tau, Synth.Wave w, double duty, boolean noisy) {
        for (Voice v : voices) {
            if (!v.active) { v.init(f0, f1, amp, dur, att, tau, w, duty, noisy); return; }
        }
    }

    private static double freq(double midi) { return 440.0 * Math.pow(2.0, (midi - 69.0) / 12.0); }

    /** 实时音符/鼓点：起音 + 指数衰减 + 尾段淡出。 */
    private final class Voice {
        boolean active;
        private Synth.Wave wave;
        private double phase, inc, incMul, duty, amp, dur, att, tau, t;
        private boolean noisy;

        void init(double f0, double f1, double amp, double dur, double att, double tau, Synth.Wave wave, double duty, boolean noisy) {
            this.wave = wave; this.noisy = noisy; this.phase = 0; this.t = 0;
            this.inc = f0 / SR; this.incMul = Math.pow(f1 / f0, 1.0 / (dur * SR));
            this.duty = duty; this.amp = amp; this.dur = dur; this.att = att; this.tau = tau;
            active = true;
        }

        double next() {
            if (t >= dur) { active = false; return 0; }
            double e = (t < att ? t / att : Math.exp(-(t - att) / tau)) * amp;
            e *= Math.min(1, (dur - t) / 0.01);
            double s = noisy ? rnd.nextDouble() * 2 - 1 : Synth.wave(wave, phase, duty);
            phase += inc; inc *= incMul; t += 1.0;
            return s * e;
        }
    }
}
