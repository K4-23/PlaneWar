package com.k423.planewar.entity;

import com.k423.planewar.core.Config;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.gfx.Assets;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * 敌机。移动由"类型 + 入场路线"两部分决定，开火节奏由类型决定，
 * 具体的弹幕形状交给 {@link com.k423.planewar.ai.BossPatterns} 里的静态方法。
 */
public final class Enemy extends Entity {

    public static final int PATTERN_STRAIGHT = 0;
    public static final int PATTERN_HOVER = 1;
    public static final int PATTERN_ORBIT = 2;
    public static final int PATTERN_DIVE = 3;

    public EnemyType type = EnemyType.SCOUT;
    public int hp = 1;
    public int maxHp = 1;
    public int pattern = PATTERN_STRAIGHT;
    public int contactDamage = 10;
    public double speedMul = 1;
    public double entryY = 260;
    public double fireTimer;
    public double hitFlash;
    public double swayAmp = 30;
    public double swayFreq = 1.6;
    public double orbitRadius = 120;
    public double orbitAngle;

    private double dirX = 1;
    private double spinAngle;

    public Enemy init(EnemyType type, double x, double y, double entryY, int pattern, double speedMul) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.vx = 0;
        this.vy = 0;
        this.entryY = entryY;
        this.pattern = pattern;
        this.speedMul = speedMul;
        this.hp = type.hp();
        this.maxHp = type.hp();
        this.radius = sizeOf(type) * 0.26;
        this.contactDamage = type.contactDamage();
        this.fireTimer = type.fireInterval() * (0.6 + 0.4 * Math.random());
        this.alive = true;
        this.age = 0;
        this.hitFlash = 0;
        this.dirX = Math.random() < 0.5 ? -1 : 1;
        this.orbitAngle = Math.random() * MathUtils.TAU;
        this.swayAmp = type == EnemyType.SCOUT ? 20 : 38;
        this.swayFreq = 1.2 + Math.random() * 0.8;
        this.spinAngle = 0;
        return this;
    }

    @Override
    public void update(World world, double dt) {
        age += dt;
        hitFlash = Math.max(0, hitFlash - dt * 6);
        double speed = type.speed() * speedMul;

        switch (pattern) {
            case PATTERN_HOVER -> {
                if (y < entryY) {
                    y += speed * dt * 1.4;
                } else {
                    x += dirX * speed * 0.85 * dt;
                    if (x < radius + 10) {
                        x = radius + 10;
                        dirX = 1;
                    } else if (x > Config.WIDTH - radius - 10) {
                        x = Config.WIDTH - radius - 10;
                        dirX = -1;
                    }
                    y += Math.sin(age * 2.2) * 12 * dt;
                }
            }
            case PATTERN_ORBIT -> {
                double cx = Config.WIDTH / 2.0;
                double cy = entryY;
                orbitAngle += dt * 1.15 * dirX;
                x = cx + Math.cos(orbitAngle) * orbitRadius;
                y = cy + Math.sin(orbitAngle) * orbitRadius * 0.55;
            }
            case PATTERN_DIVE -> {
                double targetX = world.player != null ? world.player.x : x;
                vx += (targetX - x) * 0.6 * dt;
                vx = MathUtils.clamp(vx, -180, 180);
                x += vx * dt;
                y += speed * 1.15 * dt;
            }
            default -> {
                y += speed * dt;
                x += Math.sin(age * swayFreq) * swayAmp * dt;
            }
        }

        if (pattern != PATTERN_ORBIT && y < entryY) {
            y += Math.sin(age * 3.1) * 6 * dt;
        }

        // 开火
        fireTimer -= dt;
        if (fireTimer <= 0 && onScreen()) {
            fireTimer = type.fireInterval() * (0.75 + Math.random() * 0.5);
            shoot(world);
        }

        // 出界回收
        if (y > Config.HEIGHT + 120 || x < -160 || x > Config.WIDTH + 160) {
            if (pattern != PATTERN_ORBIT) {
                alive = false;
            }
        }
    }

    private boolean onScreen() {
        return y > -20 && y < Config.HEIGHT * 0.9;
    }

    private void shoot(World world) {
        if (world.player == null || !world.player.alive) {
            return;
        }
        double angle = Math.atan2(world.player.y - y, world.player.x - x);
        switch (type) {
            case SCOUT -> world.spawnEnemyBullet(x, y + 16, angle, 260, 8, Palette.ORANGE);
            case FIGHTER -> {
                for (int i = -1; i <= 1; i++) {
                    world.spawnEnemyBullet(x, y + 20, angle + i * 0.20, 285, 8, Palette.ORANGE);
                }
            }
            case TANK -> world.spawnEnemyBullet(x, y + 30, angle, 215, 13, Palette.AMBER);
            case ELITE -> {
                spinAngle += 0.55;
                for (int i = 0; i < 4; i++) {
                    double a = spinAngle + i * MathUtils.TAU / 4;
                    world.spawnEnemyBullet(x, y, a, 195, 9, Palette.MAGENTA);
                }
            }
            case DRONE -> world.spawnEnemyBullet(x, y + 10, angle, 330, 7, Palette.ORANGE);
            default -> {
            }
        }
    }

    public void damage(World world, int amount) {
        hp -= amount;
        hitFlash = 1;
        if (hp <= 0) {
            world.killEnemy(this);
        }
    }

    private static BufferedImage spriteOf(EnemyType type) {
        return switch (type) {
            case SCOUT -> Assets.ENEMY_SCOUT;
            case FIGHTER -> Assets.ENEMY_FIGHTER;
            case TANK -> Assets.ENEMY_TANK;
            case ELITE -> Assets.ENEMY_ELITE;
            case DRONE -> Assets.ENEMY_DRONE;
        };
    }

    /** 像素素材固定 32x32，这里按机型给放大倍数（整数倍，保持像素锐利）。 */
    private static double sizeOf(EnemyType type) {
        return switch (type) {
            case SCOUT -> 64;
            case FIGHTER -> 96;
            case TANK -> 128;
            case ELITE -> 160;
            case DRONE -> 64;
        };
    }

    @Override
    public void render(Renderer r) {
        BufferedImage sprite = spriteOf(type);
        double size = sizeOf(type);

        r.sprite(Assets.shadow(sprite), x + 4, y + 5, size, size, 0.2f);
        r.sprite(sprite, x, y, size, size, 1f);
        if (hitFlash > 0.05) {
            r.sprite(Assets.white(sprite), x, y, size, size, 0.75f * (float) hitFlash);
        }

        // 高血量单位显示像素血条
        if (maxHp > 60 && hp < maxHp) {
            double barW = size * 0.8;
            double ratio = Math.max(0, hp / (double) maxHp);
            double by = y - size / 2 - 10;
            r.fillRect(x - barW / 2 - 2, by - 1, barW + 4, 5, Palette.DARK, 0.85f);
            r.fillRect(x - barW / 2, by, barW * ratio, 3, Palette.hpColor(ratio), 1f);
        }
    }
}
