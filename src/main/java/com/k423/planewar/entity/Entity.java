package com.k423.planewar.entity;

import com.k423.planewar.gfx.Renderer;
import com.k423.planewar.vfx.FloatingTexts;
import com.k423.planewar.vfx.ParticleSystem;
import com.k423.planewar.vfx.ScreenEffects;

/**
 * 实体基类。坐标用画布坐标（720x1280），碰撞用圆。
 * 所有实体只在 {@link World} 里被更新，更新顺序由 World 决定。
 */
public abstract class Entity {

    public double x;
    public double y;
    public double vx;
    public double vy;
    public double radius = 12;
    public boolean alive = true;
    public double age;

    public abstract void update(World world, double dt);

    public abstract void render(Renderer r);

    public boolean isOffscreen(double margin) {
        return y < -margin || y > com.k423.planewar.core.Config.HEIGHT + margin
                || x < -margin || x > com.k423.planewar.core.Config.WIDTH + margin;
    }

    public double centerX() {
        return x;
    }

    public double centerY() {
        return y;
    }

    // 下面三个只是给子类少写几层 getter，保持 World 里的更新代码短一些。
    protected static ParticleSystem particles(World w) {
        return w.particles;
    }

    protected static FloatingTexts texts(World w) {
        return w.texts;
    }

    protected static ScreenEffects fx(World w) {
        return w.fx;
    }
}
