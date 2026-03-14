package com.k423.planewar.entity;

import com.k423.planewar.audio.Sfx;
import com.k423.planewar.core.Config;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.gfx.Assets;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

import java.awt.Color;

/**
 * 玩家战机。键鼠两套操作：
 * 键盘 WASD/方向键移动，鼠标最近动过就自动切换成跟随鼠标；开火是自动的，空格/回车不影响。
 */
public final class Player extends Entity {

    public int lives = Config.PLAYER_LIVES;
    public int weaponLevel = 2;
    public int bombs = 2;
    public double shieldTime;
    public double invulnTime;
    public double fireTimer;
    public double hitFlash;
    public double bank;
    public double enginePulse;

    private double mouseActivity;
    private double prevMouseX = Double.NaN;
    private double prevMouseY = Double.NaN;
    private double muzzleFlash;

    public Player() {
        this.x = Config.WIDTH / 2.0;
        this.y = Config.HEIGHT - 190;
        this.radius = Config.PLAYER_RADIUS;
    }

    public boolean isInvulnerable() {
        return invulnTime > 0;
    }

    public boolean hasShield() {
        return shieldTime > 0;
    }

    @Override
    public void update(World world, double dt) {
        age += dt;
        fireTimer -= dt;
        muzzleFlash = Math.max(0, muzzleFlash - dt * 8);
        hitFlash = Math.max(0, hitFlash - dt * 4);
        invulnTime = Math.max(0, invulnTime - dt);
        shieldTime = Math.max(0, shieldTime - dt);
        mouseActivity = Math.max(0, mouseActivity - dt);
        enginePulse += dt;

        move(world, dt);

        if (fireTimer <= 0) {
            fire(world);
            fireTimer = fireInterval();
        }

        if (world.input.bomb() && bombs > 0) {
            bombs--;
            world.useBomb();
        }

        // 尾焰
        if (world.rng().nextDouble() < 0.85) {
            world.particles.trail(x + (world.rng().nextDouble() - 0.5) * 14, y + 34,
                    Palette.alpha(Palette.CYAN, 0.55f), 4.5, 0.30);
        }
    }

    private void move(World world, double dt) {
        double kx = 0;
        double ky = 0;
        if (world.input.left()) {
            kx -= 1;
        }
        if (world.input.right()) {
            kx += 1;
        }
        if (world.input.up()) {
            ky -= 1;
        }
        if (world.input.down()) {
            ky += 1;
        }

        boolean keyboard = kx != 0 || ky != 0;
        if (keyboard) {
            mouseActivity = 0;
            double len = Math.hypot(kx, ky);
            kx /= len;
            ky /= len;
            double speed = world.input.focus() ? Config.PLAYER_FOCUS_SPEED : Config.PLAYER_SPEED;
            x += kx * speed * dt;
            y += ky * speed * dt;
            bank = MathUtils.lerp(bank, kx * 0.22, Math.min(1, dt * 10));
        } else {
            double mx = world.input.mouseX();
            double my = world.input.mouseY();
            if (!Double.isNaN(prevMouseX)
                    && (Math.abs(mx - prevMouseX) > 0.8 || Math.abs(my - prevMouseY) > 0.8)) {
                mouseActivity = 1.6;
            }
            prevMouseX = mx;
            prevMouseY = my;
            if (mouseActivity > 0) {
                double dx = mx - x;
                double dy = my - y;
                double dist = Math.hypot(dx, dy);
                double speed = Config.PLAYER_SPEED * 1.6 * dt;
                if (dist > 1) {
                    double step = Math.min(dist, speed);
                    x += dx / dist * step;
                    y += dy / dist * step;
                    bank = MathUtils.lerp(bank, Math.max(-0.25, Math.min(0.25, dx * 0.01)),
                            Math.min(1, dt * 10));
                }
            } else {
                bank = MathUtils.lerp(bank, 0, Math.min(1, dt * 6));
            }
        }

        x = MathUtils.clamp(x, radius + 8, Config.WIDTH - radius - 8);
        y = MathUtils.clamp(y, radius + 120, Config.HEIGHT - radius - 10);
    }

    public double fireInterval() {
        return Math.max(Config.MIN_FIRE_INTERVAL, Config.START_FIRE_INTERVAL - (weaponLevel - 1) * Config.FIRE_INTERVAL_PER_LEVEL);
    }

    private void fire(World world) {
        int dmg = Config.PLAYER_BULLET_DAMAGE + (weaponLevel - 1) * 2;
        double speed = Config.PLAYER_BULLET_SPEED;
        double size = 18;
        muzzleFlash = 1;
        world.audio.play(Sfx.SHOOT, 0.55f, 1.0f + weaponLevel * 0.02f);
        world.particles.spark(x, y - 30, 3, Palette.CYAN, 180, 0.16, 2.0);

        switch (weaponLevel) {
            case 1 -> world.spawnPlayerBullet(x, y - 34, 0, -speed, dmg, size);
            case 2 -> {
                world.spawnPlayerBullet(x - 13, y - 28, 0, -speed, dmg, size);
                world.spawnPlayerBullet(x + 13, y - 28, 0, -speed, dmg, size);
            }
            case 3 -> {
                world.spawnPlayerBullet(x, y - 38, 0, -speed, dmg, size + 2);
                world.spawnPlayerBullet(x - 18, y - 24, -95, -speed, dmg, size);
                world.spawnPlayerBullet(x + 18, y - 24, 95, -speed, dmg, size);
            }
            case 4 -> {
                world.spawnPlayerBullet(x, y - 38, 0, -speed, dmg, size + 2);
                world.spawnPlayerBullet(x - 15, y - 30, -90, -speed, dmg, size);
                world.spawnPlayerBullet(x + 15, y - 30, 90, -speed, dmg, size);
                world.spawnPlayerBullet(x - 26, y - 18, -190, -speed, dmg - 2, size - 2);
                world.spawnPlayerBullet(x + 26, y - 18, 190, -speed, dmg - 2, size - 2);
            }
            default -> {
                world.spawnPlayerBullet(x, y - 40, 0, -speed, dmg + 2, size + 4);
                world.spawnPlayerBullet(x - 12, y - 32, -70, -speed, dmg, size);
                world.spawnPlayerBullet(x + 12, y - 32, 70, -speed, dmg, size);
                world.spawnPlayerBullet(x - 26, y - 20, -170, -speed, dmg - 2, size - 2);
                world.spawnPlayerBullet(x + 26, y - 20, 170, -speed, dmg - 2, size - 2);
                world.spawnPlayerBullet(x - 36, y - 10, -300, -speed * 0.92, dmg - 4, size - 3);
                world.spawnPlayerBullet(x + 36, y - 10, 300, -speed * 0.92, dmg - 4, size - 3);
            }
        }
    }

    /** 被击中。护盾优先顶掉，其次扣命并原地复活。 */
    public void hit(World world, int damage) {
        if (invulnTime > 0 || !alive) {
            return;
        }
        if (shieldTime > 0) {
            shieldTime = 0;
            invulnTime = 1.0;
            world.audio.play(Sfx.SHIELD_BREAK);
            world.fx.flash(Palette.ICE, 0.45);
            world.fx.shake(14);
            world.particles.ring(x, y, Palette.ICE, 20, 150, 0.4);
            world.particles.spark(x, y, 26, Palette.ICE, 340, 0.5, 2.6);
            world.texts.spawn("护盾破裂", x, y - 60, Palette.ICE, 22, true, 1.0);
            return;
        }

        lives--;
        weaponLevel = Math.max(1, weaponLevel - 1);
        hitFlash = 1;
        world.audio.play(Sfx.PLAYER_HIT);
        world.fx.flash(Palette.RED, 0.55);
        world.fx.shake(30);
        world.fx.hitstop(0.10);
        world.particles.explosion(x, y, 1.6, Palette.RED);
        world.particles.debris(x, y, 16, Palette.CYAN, 300, 0.9, 4);
        world.clearEnemyBulletsAround(x, y, 190);

        if (lives <= 0) {
            alive = false;
            world.onPlayerDestroyed();
            return;
        }

        x = Config.WIDTH / 2.0;
        y = Config.HEIGHT - 190;
        invulnTime = Config.RESPAWN_INVULN;
        shieldTime = 0.8;
        world.texts.spawn("剩余机体 " + lives, x, y - 70, Palette.AMBER, 24, true, 1.2);
    }

    public void addWeaponLevel(World world) {
        weaponLevel = Math.min(Config.MAX_WEAPON_LEVEL, weaponLevel + 1);
        world.texts.spawn("火力 " + weaponLevel + " 级", x, y - 70, Palette.CYAN, 22, true, 1.1);
    }

    @Override
    public void render(Renderer r) {
        // 无敌时间闪烁：整机隔帧隐藏
        boolean visible = true;
        if (invulnTime > 0 && invulnTime < Config.RESPAWN_INVULN) {
            visible = Math.sin(age * 34) > 0.1;
        }

        // 尾焰：几个像素方块，比径向光斑更贴像素画风
        double flame = 0.6 + 0.4 * Math.sin(enginePulse * 30);
        r.fillRect(x - 14, y + 40, 6, 10 + flame * 8, Palette.AMBER, 0.95f);
        r.fillRect(x + 8, y + 40, 6, 10 + flame * 8, Palette.AMBER, 0.95f);
        r.fillRect(x - 13, y + 42, 4, 6 + flame * 5, Palette.WHITE, 0.85f);
        r.fillRect(x + 9, y + 42, 4, 6 + flame * 5, Palette.WHITE, 0.85f);

        if (visible) {
            r.push();
            r.translate(x, y);
            r.rotate(bank * 0.4);
            r.sprite(Assets.shadow(Assets.PLAYER), 3, 4, 96, 96, 0.2f);
            r.sprite(Assets.PLAYER, 0, 0, 96, 96, 1f);
            r.pop();
        }
        if (muzzleFlash > 0.05) {
            r.fillRect(x - 4, y - 52, 8, 12 * muzzleFlash + 6, Palette.WHITE, 0.9f);
        }
        if (hitFlash > 0.05) {
            r.sprite(Assets.white(Assets.PLAYER), x, y, 96, 96, 0.7f * (float) hitFlash);
        }
        if (shieldTime > 0) {
            double pulse = 0.5 + 0.5 * Math.sin(age * 8);
            float a = (float) Math.min(1, shieldTime) * (0.45f + 0.25f * (float) pulse);
            double s = 38;
            // 像素护盾：四边各画两段小方块
            for (double[] seg : new double[][]{
                    {-s, -s, 20, 3}, {s - 20, -s, 20, 3},
                    {-s, s, 20, 3}, {s - 20, s, 20, 3},
                    {-s, -s, 3, 20}, {-s, s - 20, 3, 20},
                    {s, -s, 3, 20}, {s, s - 20, 3, 20}}) {
                r.fillRect(x + seg[0], y + seg[1], seg[2], seg[3], Palette.CYAN, a);
            }
        }
        r.resetAlpha();
    }
}
