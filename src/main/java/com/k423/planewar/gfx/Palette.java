package com.k423.planewar.gfx;

import java.awt.Color;

/** 配色。整体走"夜间突袭 + 霓虹"：深海军蓝底，青色为主光，品红/琥珀做警示和奖励色。 */
public final class Palette {

    private Palette() {
    }

    public static final Color VOID = new Color(4, 7, 18);
    public static final Color NIGHT = new Color(9, 14, 32);
    public static final Color NIGHT_SOFT = new Color(16, 24, 48);

    public static final Color CYAN = new Color(70, 225, 255);
    public static final Color CYAN_DIM = new Color(38, 120, 150);
    public static final Color ICE = new Color(180, 244, 255);

    public static final Color MAGENTA = new Color(255, 62, 140);
    public static final Color MAGENTA_DIM = new Color(120, 30, 70);

    public static final Color AMBER = new Color(255, 194, 74);
    public static final Color LIME = new Color(124, 255, 107);
    public static final Color ORANGE = new Color(255, 130, 40);
    public static final Color RED = new Color(255, 72, 72);
    public static final Color WHITE = new Color(240, 250, 255);
    public static final Color GREY = new Color(140, 160, 180);
    public static final Color DARK = new Color(6, 10, 22, 200);

    public static Color alpha(Color c, float a) {
        int alpha = Math.round(Math.max(0, Math.min(1, a)) * 255);
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }

    public static Color mix(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
                (int) Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    /** 从绿色 10% 到红色，用于血条。 */
    public static Color hpColor(double ratio) {
        ratio = Math.max(0, Math.min(1, ratio));
        if (ratio > 0.5) {
            return mix(AMBER, LIME, (ratio - 0.5) * 2);
        }
        return mix(RED, AMBER, ratio * 2);
    }
}
