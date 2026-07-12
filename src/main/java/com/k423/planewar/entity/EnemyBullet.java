package com.k423.planewar.entity;

import com.k423.planewar.core.Config;
import com.k423.planewar.gfx.Assets;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

import java.awt.Color;

/** 敌方子弹。形状/颜色由发射者决定，这里只负责直线飞和回收。 */
public final class EnemyBullet extends Entity {

    public Color color = Palette.MAGENTA;
    public double width = 18;
    public double height = 18;
    public double spin;

    public EnemyBullet reset(double x, double y, double vx, double vy, double radius, Color color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
        this.width = radius * 2;
        this.height = radius * 2;
        this.color = color;
        this.alive = true;
        this.age = 0;
        this.spin = radius > 10 ? 0.6 : 0;
        return this;
    }

    @Override
    public void update(World world, double dt) {
        age += dt;
        x += vx * dt;
        y += vy * dt;
        if (age > 0.15 && world.rng().nextDouble() < 0.04) {
            world.particles.trail(x, y, color, 3.0, 0.16);
        }
        if (y > Config.HEIGHT + 40 || y < -80 || x < -60 || x > Config.WIDTH + 60) {
            alive = false;
        }
    }

    @Override
    public void render(Renderer r) {
        double s = Math.max(6, radius * 1.25);
        r.fillRect(x - s / 2 - 1, y - s / 2 - 1, s + 2, s + 2, Palette.DARK, 0.75f);
        r.fillRect(x - s / 2, y - s / 2, s, s, color, 1f);
        r.fillRect(x - s / 4, y - s / 4, s / 2, s / 2, Palette.WHITE, 1f);
    }
}
