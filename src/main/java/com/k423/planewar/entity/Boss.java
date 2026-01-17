package com.k423.planewar.entity;

import com.k423.planewar.ai.BossPatterns;
import com.k423.planewar.audio.Sfx;
import com.k423.planewar.core.Config;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.gfx.Assets;
import com.k423.planewar.gfx.Fonts;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

/**
 * Boss。分三阶段，血量越低弹幕越凶：
 * 阶段一：扇形 + 瞄准点射；阶段二：加螺旋弹幕；阶段三：弹幕雨 + 召唤无人机 + 狂暴红化。
 */
public final class Boss {

    public enum State {ENTER, FIGHT, DYING, DEAD}

    public final String name;
    public final boolean finalBoss;
    public State state = State.ENTER;
    public double x = Config.WIDTH / 2.0;
    public double y = -220;
    public int hp;
    public int maxHp;
    public int phase = 1;
    public double hitFlash;
    public double sizeW = 288;
    public double sizeH = 288;
    public double radius = 112;
    public double targetY = 250;

    private double t;
    private double attackTimer = 1.2;
    private double secondaryTimer = 3.0;
    private double summonTimer = 6;
    private double spiralAngle;
    private double spiralTimer;
    private double dyingTimer;
    private double charge;

    public Boss(String name, boolean finalBoss, int hp) {
        this.name = name;
        this.finalBoss = finalBoss;
        this.hp = hp;
        this.maxHp = hp;
    }

    public double hpRatio() {
        return Math.max(0, hp / (double) maxHp);
    }

    public boolean isFightable() {
        return state == State.FIGHT;
    }

    public void update(World world, double dt) {
        t += dt;
        hitFlash = Math.max(0, hitFlash - dt * 5);
        charge = Math.max(0, charge - dt * 2.2);

        switch (state) {
            case ENTER -> {
                y += 170 * dt;
                if (y >= targetY) {
                    y = targetY;
                    state = State.FIGHT;
                    attackTimer = 1.6;
                }
            }
            case FIGHT -> {
                updatePhase();
                double speed = 0.55 + phase * 0.16;
                double span = 150 + phase * 26;
                x = Config.WIDTH / 2.0 + Math.sin(t * speed) * span;
                y = targetY + Math.sin(t * 1.4) * 16 + (phase - 1) * 10;
                radius = sizeW * 0.34;
                attack(world, dt);
            }
            case DYING -> {
                dyingTimer += dt;
                y += 16 * dt;
                if (dyingTimer > 1.7) {
                    state = State.DEAD;
                }
            }
            default -> {
            }
        }
    }

    private void updatePhase() {
        double r = hpRatio();
        int next = r > 0.7 ? 1 : r > 0.35 ? 2 : 3;
        if (next != phase) {
            phase = next;
            attackTimer = Math.max(attackTimer, 0.8);
            if (phase == 3) {
                charge = 1;
            }
        }
    }

    private void attack(World world, double dt) {
        attackTimer -= dt;
        secondaryTimer -= dt;
        summonTimer -= dt;
        spiralTimer -= dt;

        if (attackTimer < 0.45 && attackTimer > 0) {
            charge = Math.max(charge, 0.7);
        }

        if (attackTimer <= 0) {
            switch (phase) {
                case 1 -> {
                    BossPatterns.fan(world, x, y + 70, BossPatterns.aimAtPlayer(world, x, y),
                            7, 62, 235, 10, Palette.MAGENTA);
                    attackTimer = 1.9;
                }
                case 2 -> {
                    BossPatterns.fan(world, x, y + 70, BossPatterns.aimAtPlayer(world, x, y),
                            9, 78, 250, 10, Palette.MAGENTA);
                    attackTimer = 1.5;
                }
                default -> {
                    BossPatterns.fan(world, x, y + 70, BossPatterns.aimAtPlayer(world, x, y),
                            11, 96, 265, 10, Palette.RED);
                    attackTimer = 1.0;
                }
            }
        }

        // 螺旋：阶段二起
        if (phase >= 2 && spiralTimer <= 0) {
            spiralAngle += 0.42;
            BossPatterns.radial(world, x, y + 40, spiralAngle, phase >= 3 ? 4 : 2, 210, 8,
                    phase >= 3 ? Palette.RED : Palette.MAGENTA);
            spiralTimer = phase >= 3 ? 0.115 : 0.16;
        }

        if (secondaryTimer <= 0) {
            BossPatterns.aimedBurst(world, x, y + 80, phase >= 2 ? 5 : 3, 5, 330, 8, Palette.ORANGE);
            secondaryTimer = phase >= 2 ? 2.6 : 3.4;
        }

        // 阶段三：弹幕雨 + 召唤无人机
        if (phase >= 3) {
            if (summonTimer <= 0) {
                world.spawnDrone(x - 120, y + 40);
                world.spawnDrone(x + 120, y + 40);
                world.texts.spawn("召唤无人机", x, y + 120, Palette.ORANGE, 20, true, 1.0);
                summonTimer = 8.0;
            }
            if (t % 5.0 < dt) {
                BossPatterns.curtain(world, -20, 9, 235, 9, Palette.RED);
            }
        }
    }

    public void damage(World world, int amount) {
        if (state == State.DYING || state == State.DEAD || state == State.ENTER) {
            return;
        }
        hp -= amount;
        hitFlash = 1;
        world.audio.play(Sfx.HIT, 0.25f, 1.4f);
        if (hp <= 0) {
            hp = 0;
            state = State.DYING;
            dyingTimer = 0;
            world.audio.play(Sfx.BIG_EXPLOSION);
            world.fx.flash(Palette.WHITE, 0.9);
            world.fx.shake(40);
            world.fx.hitstop(0.16);
            world.clearAllEnemyBullets();
        }
    }

    public void render(World world, Renderer r) {
        double pulse = 0.5 + 0.5 * Math.sin(t * 4);
        r.sprite(Assets.shadow(Assets.BOSS), x + 6, y + 8, sizeW, sizeH, 0.25f);
        r.sprite(Assets.BOSS, x, y, sizeW, sizeH, 1f);
        if (hitFlash > 0.05) {
            r.sprite(Assets.white(Assets.BOSS), x, y, sizeW, sizeH, 0.35f * (float) hitFlash);
        }

        if (state == State.FIGHT && phase == 3) {
            double aura = 0.5 + 0.5 * Math.sin(t * 9);
            r.fillRect(x - sizeW / 2 - 6, y - sizeH / 2 - 6, sizeW + 12, 3, Palette.RED, 0.5f + 0.3f * (float) aura);
            r.fillRect(x - sizeW / 2 - 6, y + sizeH / 2 + 3, sizeW + 12, 3, Palette.RED, 0.5f + 0.3f * (float) aura);
        }

        if (state == State.ENTER && y > 40) {
            r.text("WARNING", x, y - sizeH * 0.55, Fonts.bold(26), Palette.RED, Renderer.Align.CENTER,
                    0.5f + 0.5f * (float) Math.sin(t * 14));
        }
    }
}
