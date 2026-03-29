package com.k423.planewar.audio;
import java.util.Random;
/** DSP 工具：振荡器、包络、粉噪声。 */
final class Synth {
    static final int SR = 44100;
    static final double TAU = Math.PI * 2;
    enum Wave { SINE, SQUARE, SAW, TRI }
    /** phase 以 1 为周期，duty 仅方波使用。 */
    static double wave(Wave w, double phase, double duty) {
        double p = phase % 1;
        return switch (w) {
            case SINE -> Math.sin(p * TAU);
            case SQUARE -> p < duty ? 1 : -1;
            case SAW -> 2 * p - 1;
            case TRI -> 1 - 4 * Math.abs(p - 0.5);
        };
    }
    /** 线性起音 + 指数衰减 + 8ms 尾部淡出，防止爆音。 */
    static double env(double t, double dur, double att, double tau) {
        double e = t < att ? t / att : Math.exp(-(t - att) / tau);
        return e * Math.min(1, (dur - t) / 0.008);
    }
    /** 三级一阶滤波叠加的白噪声，近似 1/f 粉噪声。 */
    static final class Pink {
        private final Random rnd = new Random(0x51EED);
        private double b0, b1, b2;
        double next() {
            double w = rnd.nextDouble() * 2 - 1;
            b0 = 0.99765 * b0 + w * 0.099046;
            b1 = 0.963 * b1 + w * 0.296516;
            b2 = 0.57 * b2 + w * 1.052691;
            return (b0 + b1 + b2 + w * 0.1848) * 0.25;
        }
    }
}
