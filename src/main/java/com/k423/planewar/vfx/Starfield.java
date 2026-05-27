package com.k423.planewar.vfx;

import com.k423.planewar.core.Config;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.core.Rng;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

import java.awt.Color;

/**
 * 三层视差星空 + 星云。菜单里会加速成"跃迁"效果。
 */
public final class Starfield {

    private static final int LAYERS = 3;
    private static final int[] COUNTS = {90, 60, 34};

    private final double[][] x;
    private final double[][] y;
    private final double[][] depth;
    private final double[][] twinkle;
    private final double[] speed;
    private final Rng rng;
    private double warp;
    private double time;

    public Starfield(Rng rng) {
        this.rng = rng;
        x = new double[LAYERS][];
        y = new double[LAYERS][];
        depth = new double[LAYERS][];
        twinkle = new double[LAYERS][];
        speed = new double[]{26, 62, 140};
        for (int l = 0; l < LAYERS; l++) {
            int n = COUNTS[l];
            x[l] = new double[n];
            y[l] = new double[n];
            depth[l] = new double[n];
            twinkle[l] = new double[n];
            for (int i = 0; i < n; i++) {
                x[l][i] = rng.range(0, Config.WIDTH);
                y[l][i] = rng.range(0, Config.HEIGHT);
                depth[l][i] = rng.range(0.6, 1.4);
                twinkle[l][i] = rng.range(0, MathUtils.TAU);
            }
        }
    }

    public void setWarp(double amount) {
        warp = MathUtils.clamp(amount, 0, 1);
    }

    public void update(double dt) {
        time += dt;
        for (int l = 0; l < LAYERS; l++) {
            double v = speed[l] * (1 + warp * 9);
            for (int i = 0; i < x[l].length; i++) {
                y[l][i] += v * depth[l][i] * dt;
                if (y[l][i] > Config.HEIGHT + 4) {
                    y[l][i] = -4;
                    x[l][i] = rng.range(0, Config.WIDTH);
                }
            }
        }
    }

    public void render(Renderer r, double intensity) {
        // 星云底色
        r.verticalGradient(0, 0, Config.WIDTH, Config.HEIGHT,
                Palette.NIGHT, Palette.VOID, 1f);
        r.orb(Config.WIDTH * 0.2, 260, 420, new Color(30, 80, 130), 0.30f);
        r.orb(Config.WIDTH * 0.85, 900, 380, new Color(90, 25, 80), 0.26f);

        for (int l = 0; l < LAYERS; l++) {
            for (int i = 0; i < x[l].length; i++) {
                double tw = 0.55 + 0.45 * Math.sin(time * 2.1 + twinkle[l][i]);
                double size = (l + 1) * 0.9 * depth[l][i];
                float a = (float) (Math.min(1, tw) * (0.18 + l * 0.16) * intensity);
                if (warp > 0.02) {
                    double len = (warp * 90) * (l + 1) / 3.0;
                    r.line(x[l][i], y[l][i] - len, x[l][i], y[l][i], Palette.ICE, 1.4f, a);
                } else {
                    r.fillRect(x[l][i], y[l][i], size, size, Palette.ICE, a);
                }
            }
        }
    }
}
