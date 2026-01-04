package com.k423.planewar.entity;

import com.k423.planewar.core.Config;
import com.k423.planewar.core.Rng;
import com.k423.planewar.gfx.Assets;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

/** 玩家子弹。向上飞，越界回收。 */
public final class PlayerBullet extends Entity {

    public int damage = 10;
    public double width = 16;
    public double height = 30;
    public boolean piercing;
    public double hitFlash;

    public PlayerBullet reset(double x, double y, double vx, double vy, int damage, double size) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.width = size;
        this.height = size * 1.9;
        this.radius = size * 0.42;
        this.alive = true;
        this.age = 0;
        this.hitFlash = 0;
        return this;
    }

    @Override
    public void update(World world, double dt) {
        age += dt;
        x += vx * dt;
        y += vy * dt;
        hitFlash = Math.max(0, hitFlash - dt * 5);
        if (y < -40 || x < -40 || x > Config.WIDTH + 40) {
            alive = false;
            return;
        }
        if (age > 0.02 && world.rng().nextDouble() < 0.22) {
            world.particles.trail(x, y + height * 0.4, Palette.alpha(Palette.CYAN, 0.7f), 3.2, 0.22);
        }
    }

    @Override
    public void render(Renderer r) {
        r.orb(x, y, width * 2.4, Palette.CYAN, 0.32f);
        r.sprite(Assets.BULLET_PLAYER, x, y, width, height, 1f);

    }
}
