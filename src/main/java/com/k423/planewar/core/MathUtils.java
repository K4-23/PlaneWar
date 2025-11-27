package com.k423.planewar.core;

/** 数学小工具。全部是纯函数，方便单元测试。 */
public final class MathUtils {

    public static final double TAU = Math.PI * 2;
    public static final double DEG = Math.PI / 180.0;

    private MathUtils() {
    }

    public static double clamp(double v, double min, double max) {
        return v < min ? min : Math.min(v, max);
    }

    public static int clampInt(int v, int min, int max) {
        return v < min ? min : Math.min(v, max);
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** 把 v 从 [a,b] 映射到 [0,1]，超出范围会被裁掉。 */
    public static double invLerp(double a, double b, double v) {
        if (a == b) {
            return 0;
        }
        return clamp((v - a) / (b - a), 0, 1);
    }

    public static double smoothstep(double t) {
        t = clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }

    public static double easeOutCubic(double t) {
        t = clamp(t, 0, 1);
        double u = 1 - t;
        return 1 - u * u * u;
    }

    public static double easeInCubic(double t) {
        t = clamp(t, 0, 1);
        return t * t * t;
    }

    public static double easeOutBack(double t) {
        t = clamp(t, 0, 1);
        double c = 1.70158;
        double u = t - 1;
        return 1 + (c + 1) * u * u * u + c * u * u;
    }

    public static double pulse(double t, double period) {
        double phase = (t % period) / period;
        return 0.5 + 0.5 * Math.sin(phase * TAU);
    }

    public static double angleTo(double x1, double y1, double x2, double y2) {
        return Math.atan2(y2 - y1, x2 - x1);
    }

    public static double distance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double distanceSq(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    /** 圆形碰撞：只做距离比较，避免开方。 */
    public static boolean circlesHit(double x1, double y1, double r1, double x2, double y2, double r2) {
        double r = r1 + r2;
        return distanceSq(x1, y1, x2, y2) <= r * r;
    }

    /** 角度归一化到 [-PI, PI)。 */
    public static double wrapAngle(double a) {
        while (a >= Math.PI) {
            a -= TAU;
        }
        while (a < -Math.PI) {
            a += TAU;
        }
        return a;
    }
}
