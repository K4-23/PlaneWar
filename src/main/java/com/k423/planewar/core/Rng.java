package com.k423.planewar.core;

/**
 * 确定性随机数（xorshift64*）。用固定种子就能复现整局游戏，方便写测试。
 */
public final class Rng {

    private long state;

    public Rng() {
        this(System.nanoTime());
    }

    public Rng(long seed) {
        this.state = seed == 0 ? 0x9E3779B97F4A7C15L : seed;
    }

    public void reseed(long seed) {
        this.state = seed == 0 ? 0x9E3779B97F4A7C15L : seed;
    }

    public long nextLong() {
        long x = state;
        x ^= x >>> 12;
        x ^= x << 25;
        x ^= x >>> 27;
        state = x;
        return x * 0x2545F4914F6CDD1DL;
    }

    /** [0,1) */
    public double nextDouble() {
        return (nextLong() >>> 11) * 0x1.0p-53;
    }

    /** [0,bound) */
    public int nextInt(int bound) {
        if (bound <= 0) {
            return 0;
        }
        long v = nextLong() >>> 1;
        return (int) (v % bound);
    }

    /** [min,max) */
    public double range(double min, double max) {
        return min + nextDouble() * (max - min);
    }

    public int range(int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + nextInt(max - min);
    }

    public double gaussian() {
        // Box-Muller
        double u1 = Math.max(1e-9, nextDouble());
        double u2 = nextDouble();
        return Math.sqrt(-2 * Math.log(u1)) * Math.cos(MathUtils.TAU * u2);
    }

    public <T> T pick(T[] values) {
        return values[nextInt(values.length)];
    }
}
