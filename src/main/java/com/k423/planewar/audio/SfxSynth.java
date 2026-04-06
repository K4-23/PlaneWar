package com.k423.planewar.audio;
import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import static com.k423.planewar.audio.Synth.Wave.*;
/** 启动时把 14 种音效预合成成 16bit 单声道 PCM，播放时不再合成。 */
final class SfxSynth {
    private static final int SR = Synth.SR;
    private SfxSynth() { }
    static Map<Sfx, short[]> synthesizeAll() {
        Map<Sfx, short[]> map = new EnumMap<>(Sfx.class);
        for (Sfx s : Sfx.values()) map.put(s, build(s));
        return map;
    }
    private static short[] build(Sfx s) {
        return switch (s) {
            case SHOOT -> {
                double[] m = mix(0.14);
                add(m, 0, 0.12, 1500, 420, SINE, 0.55, 0.002, 0.05);
                add(m, 0, 0.08, 2300, 650, SQUARE, 0.09, 0.002, 0.02, 0.3);
                noise(m, 0, 0.05, 0.10, 0.001, 0.015, 9000, 3000, false);
                yield finish(m);
            }
            case ENEMY_SHOOT -> {
                double[] m = mix(0.16);
                add(m, 0, 0.14, 520, 160, SQUARE, 0.32, 0.002, 0.06, 0.35);
                add(m, 0, 0.13, 250, 100, SINE, 0.38, 0.002, 0.07);
                noise(m, 0, 0.05, 0.06, 0.001, 0.02, 1800, 600, false);
                yield finish(m);
            }
            case HIT -> {
                double[] m = mix(0.2);
                add(m, 0, 0.14, 2400, 2400, SINE, 0.24, 0.001, 0.045);
                add(m, 0, 0.12, 3500, 3500, SINE, 0.16, 0.001, 0.035);
                add(m, 0, 0.10, 5200, 5200, SINE, 0.10, 0.001, 0.025);
                noise(m, 0, 0.025, 0.15, 0.001, 0.006, 13000, 8000, false);
                yield finish(m);
            }
            case EXPLOSION -> {
                double[] m = mix(0.7);
                noise(m, 0, 0.6, 0.75, 0.002, 0.20, 7000, 150, true);
                add(m, 0, 0.45, 150, 40, SINE, 0.55, 0.002, 0.13);
                add(m, 0, 0.05, 4000, 800, SQUARE, 0.10, 0.001, 0.012, 0.5);
                noise(m, 0, 0.06, 0.35, 0.001, 0.012, 16000, 5000, false);
                yield finish(m);
            }
            case BIG_EXPLOSION -> {
                double[] m = mix(1.6);
                noise(m, 0, 1.5, 0.8, 0.003, 0.5, 6000, 60, true);
                add(m, 0, 1.0, 90, 26, SINE, 0.8, 0.003, 0.35);
                add(m, 0.1, 1.1, 180, 50, TRI, 0.35, 0.01, 0.5);
                noise(m, 0, 0.08, 0.5, 0.001, 0.015, 16000, 3000, false);
                noise(m, 0.2, 1.2, 0.25, 0.02, 0.5, 1400, 120, true);
                yield finish(m);
            }
            case PLAYER_HIT -> {
                double[] m = mix(0.55);
                add(m, 0, 0.4, 950, 320, SQUARE, 0.25, 0.002, 0.2, 0.25);
                add(m, 0.04, 0.36, 1150, 430, SQUARE, 0.16, 0.002, 0.16, 0.2);
                add(m, 0, 0.4, 120, 45, SINE, 0.5, 0.002, 0.16);
                noise(m, 0, 0.08, 0.15, 0.001, 0.03, 5000, 1000, true);
                yield finish(m);
            }
            case POWERUP -> {
                double[] m = mix(0.6);
                double[] n = {523.25, 659.25, 783.99};
                for (int i = 0; i < n.length; i++) {
                    add(m, i * 0.075, 0.32, n[i], n[i], SINE, 0.2, 0.004, 0.10);
                    add(m, i * 0.075, 0.22, n[i] * 2, n[i] * 2, TRI, 0.07, 0.004, 0.07);
                }
                yield finish(m);
            }
            case BOMB -> {
                double[] m = mix(1.4);
                add(m, 0, 1.25, 220, 25, SINE, 0.85, 0.005, 0.4);
                add(m, 0, 1.0, 110, 32, TRI, 0.4, 0.005, 0.3);
                noise(m, 0, 1.1, 0.5, 0.01, 0.4, 5000, 70, true);
                noise(m, 0, 0.09, 0.4, 0.001, 0.02, 14000, 3000, false);
                yield finish(m);
            }
            case SHIELD_BREAK -> {
                double[] m = mix(0.7);
                double[] n = {2600, 3400, 4500, 5700, 6900};
                for (int i = 0; i < n.length; i++)
                    add(m, i * 0.02, 0.3 - i * 0.035, n[i], n[i] * 0.85, SINE, 0.16, 0.001, 0.06);
                noise(m, 0, 0.4, 0.22, 0.001, 0.10, 15000, 4000, false);
                add(m, 0, 0.35, 3200, 1400, TRI, 0.09, 0.001, 0.10);
                yield finish(m);
            }
            case BOSS_WARNING -> {
                double[] m = mix(1.0);
                add(m, 0, 0.24, 640, 470, SQUARE, 0.24, 0.008, 0.2, 0.5);
                add(m, 0.45, 0.30, 640, 400, SQUARE, 0.26, 0.008, 0.24, 0.5);
                add(m, 0, 0.9, 108, 100, SINE, 0.2, 0.05, 0.6);
                yield finish(m);
            }
            case UI_CLICK -> {
                double[] m = mix(0.07);
                add(m, 0, 0.05, 2200, 1400, SINE, 0.25, 0.001, 0.015);
                noise(m, 0, 0.012, 0.10, 0.001, 0.004, 12000, 7000, false);
                yield finish(m);
            }
            case UI_HOVER -> {
                double[] m = mix(0.05);
                add(m, 0, 0.04, 1500, 1100, SINE, 0.13, 0.001, 0.012);
                yield finish(m);
            }
            case VICTORY -> {
                double[] m = mix(1.9);
                double[] n = {261.63, 329.63, 392.0, 523.25};
                for (int i = 0; i < n.length; i++) {
                    add(m, i * 0.16, 0.55, n[i], n[i], TRI, 0.2, 0.015, 0.3);
                    add(m, i * 0.16, 0.45, n[i] * 2, n[i] * 2, SINE, 0.06, 0.015, 0.2);
                }
                double[] chord = {523.25, 659.25, 783.99};
                for (double f : chord) add(m, 0.82, 1.0, f, f, TRI, 0.12, 0.02, 0.5);
                add(m, 0.82, 1.0, 130.81, 130.81, SINE, 0.2, 0.02, 0.6);
                yield finish(m);
            }
            case DEFEAT -> {
                double[] m = mix(2.1);
                double[] n = {440.0, 349.23, 293.66, 220.0};
                for (int i = 0; i < n.length; i++) {
                    add(m, i * 0.3, 0.75, n[i], n[i], TRI, 0.2, 0.03, 0.35);
                    add(m, i * 0.3, 0.6, n[i] / 2, n[i] / 2, SINE, 0.1, 0.03, 0.3);
                }
                add(m, 0.9, 1.15, 110, 110, SINE, 0.18, 0.05, 0.6);
                yield finish(m);
            }
        };
    }
    private static double[] mix(double seconds) { return new double[(int) (seconds * SR)]; }
    private static void add(double[] out, double at, double dur, double f0, double f1,
                            Wave w, double amp, double att, double tau) {
        add(out, at, dur, f0, f1, w, amp, att, tau, 0.5);
    }
    private static void add(double[] out, double at, double dur, double f0, double f1,
                            Wave w, double amp, double att, double tau, double duty) {
        int s0 = (int) (at * SR);
        int n = Math.min((int) (dur * SR), out.length - s0);
        if (n <= 0) return;
        double phase = 0, f = f0, ratio = Math.pow(f1 / f0, 1.0 / n);
        for (int i = 0; i < n; i++) {
            phase += f / SR;
            out[s0 + i] += Synth.wave(w, phase, duty) * amp * Synth.env((double) i / SR, dur, att, tau);
            f *= ratio;
        }
    }
    private static void noise(double[] out, double at, double dur, double amp, double att, double tau,
                              double cut0, double cut1, boolean pink) {
        int s0 = (int) (at * SR);
        int n = Math.min((int) (dur * SR), out.length - s0);
        if (n <= 0) return;
        Random rnd = new Random(1234567L + s0);
        Synth.Pink pk = pink ? new Synth.Pink() : null;
        double lp = 0, cut = cut0, ratio = Math.pow(cut1 / cut0, 1.0 / n);
        for (int i = 0; i < n; i++) {
            double x = pink ? pk.next() : rnd.nextDouble() * 2 - 1;
            double a = 1 - Math.exp(-Synth.TAU * cut / SR);
            lp += (x - lp) * a;
            out[s0 + i] += lp * amp * Synth.env((double) i / SR, dur, att, tau) * 1.7;
            cut *= ratio;
        }
    }
    private static short[] finish(double[] m) {
        short[] pcm = new short[m.length];
        int fade = (int) (0.004 * SR);
        for (int i = 0; i < m.length; i++) {
            double g = Math.min(1, Math.min((double) i / fade, (double) (m.length - i) / fade));
            pcm[i] = (short) Math.round(Math.tanh(m[i] * 1.1) * g * 32767);
        }
        return pcm;
    }
}
