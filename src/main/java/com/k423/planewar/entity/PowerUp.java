package com.k423.planewar.entity;

import com.k423.planewar.core.Config;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.gfx.Assets;
import com.k423.planewar.gfx.Fonts;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

/**
 * 掉落道具。靠近玩家时会被"吸"过去，拾取后给对应增益。
 */
public final class PowerUp extends Entity {

    public PowerUpType type = PowerUpType.WEAPON;
    public double bobPhase;
    private double spin;

    public PowerUp init(PowerUpType type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = Config.POWERUP_FALL_SPEED;
        this.radius = 22;
        this.alive = true;
        this.age = 0;
        this.bobPhase = Math.random() * MathUtils.TAU;
        this.spin = 0;
        return this;
    }

    @Override
    public void update(World world, double dt) {
        age += dt;
        spin += dt * 2.2;
        y += vy * dt;
        x += Math.sin(age * 2.4 + bobPhase) * 26 * dt;

        Player p = world.player;
        if (p != null && p.alive) {
            double d = MathUtils.distance(x, y, p.x, p.y);
            if (d < Config.POWERUP_MAGNET_RADIUS) {
                double pull = MathUtils.smoothstep(1 - d / Config.POWERUP_MAGNET_RADIUS);
                double a = Math.atan2(p.y - y, p.x - x);
                double v = 140 + 420 * pull;
                x += Math.cos(a) * v * dt;
                y += Math.sin(a) * v * dt;
            }
        }

        if (y > Config.HEIGHT + 40) {
            alive = false;
        }
    }

    @Override
    public void render(Renderer r) {
        double bob = Math.sin(age * 4 + bobPhase) * 3;
        double pulse = 0.5 + 0.5 * Math.sin(age * 6 + bobPhase);
        java.awt.image.BufferedImage icon = switch (type) {
            case WEAPON -> Assets.ICON_POWER;
            case SHIELD -> Assets.ICON_SHIELD;
            case BOMB -> Assets.ICON_BOMB;
            case HEAL -> Assets.ICON_LIFE;
            case SCORE -> Assets.ICON_SCORE;
        };
        r.sprite(icon, x, y + bob, 32, 32, 1f);
        if (pulse > 0.6) {
            r.sprite(Assets.white(icon), x, y + bob, 32, 32, 0.35f * (float) pulse);
        }
    }
}
