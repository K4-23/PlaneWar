package com.k423.planewar.entity;

import com.k423.planewar.core.Config;
import com.k423.planewar.gfx.Assets;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

import java.awt.image.BufferedImage;

/**
 * 一次性爆炸演出。小爆炸用 32 帧火球，大爆炸叠一层 40 帧冲击波。
 */
public final class Explosion extends Entity {

    private static final double SMALL_DURATION = 0.48;
    private static final double BIG_DURATION = 0.78;

    public double size = 90;
    public boolean big;
    private double t;
    private double duration;

    public Explosion init(double x, double y, double size, boolean big) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.big = big;
        this.t = 0;
        this.duration = big ? BIG_DURATION : SMALL_DURATION;
        this.alive = true;
        this.age = 0;
        return this;
    }

    @Override
    public void update(World world, double dt) {
        age += dt;
        t += dt;
        if (t >= duration) {
            alive = false;
        }
    }

    @Override
    public void render(Renderer r) {
        double progress = Math.max(0, Math.min(1, t / duration));
        int idx = (int) Math.min(Assets.EXPLOSION.length - 1, progress * Assets.EXPLOSION.length);
        float a = (float) (1 - progress * progress);
        r.sprite(Assets.EXPLOSION[idx], x, y, size, size, Math.max(0, a));
    }

    private static BufferedImage frameAt(BufferedImage[] frames, double progress) {
        if (frames == null || frames.length == 0) {
            return null;
        }
        int idx = (int) Math.min(frames.length - 1, Math.max(0, progress * frames.length));
        return frames[idx];
    }

    private static double MathUtils01(double v) {
        return Math.max(0, Math.min(1, v));
    }
}
