package com.k423.planewar.vfx;

import com.k423.planewar.core.Config;
import com.k423.planewar.gfx.Fonts;
import com.k423.planewar.gfx.Renderer;

import java.awt.Color;
import java.awt.Font;

/** 飘字：得分、连击提示、道具名。固定容量，循环复用。 */
public final class FloatingTexts {

    private final String[] text = new String[Config.MAX_FLOATING_TEXT];
    private final double[] x = new double[Config.MAX_FLOATING_TEXT];
    private final double[] y = new double[Config.MAX_FLOATING_TEXT];
    private final double[] vy = new double[Config.MAX_FLOATING_TEXT];
    private final double[] life = new double[Config.MAX_FLOATING_TEXT];
    private final double[] maxLife = new double[Config.MAX_FLOATING_TEXT];
    private final int[] color = new int[Config.MAX_FLOATING_TEXT];
    private final int[] fontSize = new int[Config.MAX_FLOATING_TEXT];
    private final boolean[] bold = new boolean[Config.MAX_FLOATING_TEXT];
    private int cursor;

    public void spawn(String s, double px, double py, Color c, int size, boolean boldText, double lifetime) {
        int i = cursor;
        cursor = (cursor + 1) % text.length;
        text[i] = s;
        x[i] = px;
        y[i] = py;
        vy[i] = -46;
        life[i] = maxLife[i] = lifetime;
        color[i] = c.getRGB();
        fontSize[i] = size;
        bold[i] = boldText;
    }

    public void update(double dt) {
        for (int i = 0; i < text.length; i++) {
            if (life[i] <= 0) {
                continue;
            }
            life[i] -= dt;
            y[i] += vy[i] * dt;
            vy[i] *= Math.pow(0.94, dt * 60);
        }
    }

    public void render(Renderer r) {
        for (int i = 0; i < text.length; i++) {
            if (life[i] <= 0 || text[i] == null) {
                continue;
            }
            float t = (float) (life[i] / maxLife[i]);
            float a = Math.min(1f, t * 2.2f);
            Font f = bold[i] ? Fonts.bold(fontSize[i]) : Fonts.plain(fontSize[i]);
            r.text(text[i], x[i], y[i], f, new Color(color[i]), Renderer.Align.CENTER, a);
        }
    }

    public void clear() {
        for (int i = 0; i < text.length; i++) {
            life[i] = 0;
            text[i] = null;
        }
    }
}
