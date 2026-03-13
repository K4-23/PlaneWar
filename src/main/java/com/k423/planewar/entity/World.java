package com.k423.planewar.entity;

import com.k423.planewar.ai.SpawnDirector;
import com.k423.planewar.audio.AudioEngine;
import com.k423.planewar.audio.Sfx;
import com.k423.planewar.core.Config;
import com.k423.planewar.core.Difficulty;
import com.k423.planewar.core.Input;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.core.Rng;
import com.k423.planewar.gfx.Assets;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;
import com.k423.planewar.vfx.FloatingTexts;
import com.k423.planewar.vfx.ParticleSystem;
import com.k423.planewar.vfx.ScreenEffects;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 一局游戏的全部状态。更新顺序固定：玩家 → 导演 → 子弹 → 敌机 → Boss → 道具 → 碰撞 → 回收。
 * 子弹和爆炸做了对象池，长时间游玩不会让 GC 抖动。
 */
public final class World {

    public final Rng rng;
    public final Difficulty difficulty;
    public final Input input;
    public final AudioEngine audio;
    public final ParticleSystem particles;
    public final FloatingTexts texts;
    public final ScreenEffects fx = new ScreenEffects();
    public final Player player = new Player();
    public final SpawnDirector director;

    public final List<Enemy> enemies = new ArrayList<>();
    public final List<PlayerBullet> playerBullets = new ArrayList<>();
    public final List<EnemyBullet> enemyBullets = new ArrayList<>();
    public final List<PowerUp> powerups = new ArrayList<>();
    public final List<Explosion> explosions = new ArrayList<>();

    public Boss boss;

    public double time;
    public double runTime;
    public int score;
    public int kills;
    public int combo;
    public int maxCombo;
    public int bombsUsed;
    public int powerupsCollected;
    public double comboTimer;
    public double scoreBoost;
    public boolean gameOver;
    public boolean victory;
    public double bossWarning;
    public double bgScroll;
    public double endDelay;

    public String bannerTitle = "";
    public String bannerSub = "";
    public double bannerTimer;

    private final Deque<PlayerBullet> playerBulletPool = new ArrayDeque<>();
    private final Deque<EnemyBullet> enemyBulletPool = new ArrayDeque<>();
    private final Deque<Explosion> explosionPool = new ArrayDeque<>();
    private double hitSoundCooldown;

    public World(Rng rng, Difficulty difficulty, Input input, AudioEngine audio) {
        this.rng = rng;
        this.difficulty = difficulty;
        this.input = input;
        this.audio = audio;
        this.particles = new ParticleSystem(rng);
        this.texts = new FloatingTexts();
        this.director = new SpawnDirector(rng);
    }

    public Rng rng() {
        return rng;
    }

    // ------------------------------------------------------------------ 更新

    public void update(double dt) {
        time += dt;
        hitSoundCooldown = Math.max(0, hitSoundCooldown - dt);
        bossWarning = Math.max(0, bossWarning - dt);
        bannerTimer = Math.max(0, bannerTimer - dt);
        bgScroll += 34 * dt;
        if (bgScroll > Config.HEIGHT) {
            bgScroll -= Config.HEIGHT;
        }

        if (!gameOver && !victory) {
            runTime += dt;
            if (player.alive) {
                player.update(this, dt);
            }
            director.update(this, dt);

            updatePlayerBullets(dt);
            updateEnemies(dt);
            if (boss != null) {
                boss.update(this, dt);
                if (boss.state == Boss.State.DEAD) {
                    removeBossAndAdvance();
                }
            }
            updateEnemyBullets(dt);
            updatePowerUps(dt);
            resolveCollisions();
            updateCombo(dt);
        }

        updateExplosions(dt);
        particles.update(dt);
        texts.update(dt);
        fx.update(dt);

        if (gameOver || victory) {
            endDelay += dt;
        }
    }

    private void updatePlayerBullets(double dt) {
        for (int i = playerBullets.size() - 1; i >= 0; i--) {
            PlayerBullet b = playerBullets.get(i);
            b.update(this, dt);
            if (!b.alive) {
                playerBullets.remove(i);
                playerBulletPool.push(b);
            }
        }
    }

    private void updateEnemyBullets(double dt) {
        for (int i = enemyBullets.size() - 1; i >= 0; i--) {
            EnemyBullet b = enemyBullets.get(i);
            b.update(this, dt);
            if (!b.alive) {
                enemyBullets.remove(i);
                enemyBulletPool.push(b);
            }
        }
    }

    private void updateEnemies(double dt) {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(this, dt);
            if (!e.alive) {
                enemies.remove(i);
            }
        }
    }

    private void updatePowerUps(double dt) {
        for (int i = powerups.size() - 1; i >= 0; i--) {
            PowerUp p = powerups.get(i);
            p.update(this, dt);
            if (!p.alive) {
                powerups.remove(i);
            }
        }
    }

    private void updateExplosions(double dt) {
        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion e = explosions.get(i);
            e.update(this, dt);
            if (!e.alive) {
                explosions.remove(i);
                explosionPool.push(e);
            }
        }
    }

    private void updateCombo(double dt) {
        if (combo > 0) {
            comboTimer -= dt;
            if (comboTimer <= 0) {
                combo = 0;
                comboTimer = 0;
            }
        }
        scoreBoost = Math.max(0, scoreBoost - dt);
    }

    // ------------------------------------------------------------------ 碰撞

    private void resolveCollisions() {
        // 玩家子弹 → 敌机 / Boss
        for (int i = playerBullets.size() - 1; i >= 0; i--) {
            PlayerBullet b = playerBullets.get(i);
            boolean consumed = false;

            for (Enemy e : enemies) {
                if (!e.alive) {
                    continue;
                }
                if (MathUtils.circlesHit(b.x, b.y, b.radius, e.x, e.y, e.radius)) {
                    e.damage(this, b.damage);
                    hitFeedback(b.x, b.y, e);
                    b.alive = false;
                    consumed = true;
                    break;
                }
            }
            if (consumed) {
                continue;
            }

            if (boss != null && boss.isFightable()
                    && MathUtils.circlesHit(b.x, b.y, b.radius, boss.x, boss.y, boss.radius)) {
                boss.damage(this, b.damage);
                hitFeedback(b.x, b.y, null);
                b.alive = false;
            }
        }

        if (player.alive) {
            // 敌方子弹 → 玩家
            for (EnemyBullet b : enemyBullets) {
                if (!b.alive) {
                    continue;
                }
                if (MathUtils.circlesHit(b.x, b.y, b.radius * 0.8, player.x, player.y, player.radius)) {
                    b.alive = false;
                    particles.spark(b.x, b.y, 8, b.color, 220, 0.32, 2.4);
                    player.hit(this, 1);
                    break;
                }
            }

            // 敌机撞击玩家
            for (Enemy e : enemies) {
                if (!e.alive) {
                    continue;
                }
                if (MathUtils.circlesHit(e.x, e.y, e.radius * 0.85, player.x, player.y, player.radius)) {
                    e.damage(this, 9999);
                    player.hit(this, e.contactDamage);
                    break;
                }
            }

            // Boss 本体撞击
            if (boss != null && boss.isFightable()
                    && MathUtils.circlesHit(boss.x, boss.y, boss.radius * 0.85, player.x, player.y, player.radius)) {
                player.hit(this, 1);
            }

            // 拾取道具
            for (PowerUp p : powerups) {
                if (!p.alive) {
                    continue;
                }
                if (MathUtils.circlesHit(p.x, p.y, p.radius, player.x, player.y, player.radius + 8)) {
                    p.alive = false;
                    applyPowerUp(p);
                }
            }
        }
    }

    private void hitFeedback(double x, double y, Enemy e) {
        particles.spark(x, y, 3, Palette.ICE, 180, 0.18, 1.8);
        if (e != null) {
            e.hitFlash = 1;
        }
        if (hitSoundCooldown <= 0) {
            audio.play(Sfx.HIT, 0.22f, 1.1f);
            hitSoundCooldown = 0.08;
        }
    }

    private void applyPowerUp(PowerUp p) {
        powerupsCollected++;
        audio.play(Sfx.POWERUP);
        texts.spawn(p.type.label(), p.x, p.y - 34, p.type.color(), 22, true, 1.1);
        switch (p.type) {
            case WEAPON -> {
                if (player.weaponLevel >= Config.MAX_WEAPON_LEVEL) {
                    addScore(150, p.x, p.y);
                } else {
                    player.addWeaponLevel(this);
                }
            }
            case SHIELD -> {
                player.shieldTime = 9.0;
                texts.spawn("护盾 9 秒", player.x, player.y - 64, Palette.ICE, 20, true, 1.0);
            }
            case BOMB -> player.bombs = Math.min(5, player.bombs + 1);
            case HEAL -> {
                if (player.lives < Config.PLAYER_LIVES + 1) {
                    player.lives++;
                    texts.spawn("机体修复", player.x, player.y - 64, Palette.LIME, 20, true, 1.0);
                } else {
                    addScore(200, p.x, p.y);
                }
            }
            case SCORE -> scoreBoost = 12.0;
        }
        particles.ring(player.x, player.y, p.type.color(), 16, 90, 0.4);
    }

    // ------------------------------------------------------------------ 生成

    public PlayerBullet spawnPlayerBullet(double x, double y, double vx, double vy, int damage, double size) {
        PlayerBullet b = playerBulletPool.poll();
        if (b == null) {
            b = new PlayerBullet();
        }
        b.reset(x, y, vx, vy, damage, size);
        playerBullets.add(b);
        return b;
    }

    public EnemyBullet spawnEnemyBullet(double x, double y, double angle, double speed, double radius, Color color) {
        EnemyBullet b = enemyBulletPool.poll();
        if (b == null) {
            b = new EnemyBullet();
        }
        b.reset(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed, radius, color);
        enemyBullets.add(b);
        return b;
    }

    public Enemy spawnEnemy(EnemyType type, double x, double y, double entryY, int pattern) {
        Enemy e = new Enemy().init(type, x, y, entryY, pattern, difficulty.enemySpeed());
        enemies.add(e);
        return e;
    }

    public void spawnDrone(double x, double y) {
        spawnEnemy(EnemyType.DRONE, x, y, y + 90, Enemy.PATTERN_HOVER);
    }

    public void spawnBoss(String name, boolean finalBoss, int hp) {
        boss = new Boss(name, finalBoss, hp);
        boss.targetY = 430;
        boss.y = -220;
        audio.startMusic(3);
    }

    public void spawnExplosion(double x, double y, double size, boolean big) {
        Explosion e = explosionPool.poll();
        if (e == null) {
            e = new Explosion();
        }
        e.init(x, y, size, big);
        explosions.add(e);
    }

    public void spawnPowerUp(double x, double y, PowerUpType type) {
        powerups.add(new PowerUp().init(type, x, y));
    }

    public void dropPowerUp(Enemy e) {
        if (rng.nextDouble() > e.type.dropChance()) {
            return;
        }
        PowerUpType type = switch (rng.nextInt(100)) {
            case 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29 -> PowerUpType.WEAPON;
            case 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49 -> PowerUpType.SHIELD;
            case 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64 -> PowerUpType.BOMB;
            case 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79 -> PowerUpType.HEAL;
            default -> PowerUpType.SCORE;
        };
        spawnPowerUp(e.x, e.y, type);
    }

    // ------------------------------------------------------------------ 结算

    public void killEnemy(Enemy e) {
        if (!e.alive) {
            return;
        }
        e.alive = false;
        e.hp = 0;
        kills++;
        combo++;
        maxCombo = Math.max(maxCombo, combo);
        comboTimer = Config.COMBO_WINDOW;

        int base = e.type.score();
        addScore(base, e.x, e.y);

        double scale = switch (e.type) {
            case ELITE -> 1.8;
            case TANK -> 1.3;
            default -> 1.0;
        };
        spawnExplosion(e.x, e.y, switch (e.type) {
            case ELITE -> 128;
            case TANK -> 128;
            default -> 64;
        }, e.type == EnemyType.ELITE);
        particles.explosion(e.x, e.y, scale, Palette.ORANGE);
        particles.debris(e.x, e.y, 8, Palette.CYAN, 240, 0.7, 3.2);
        fx.shake(4 * scale);
        audio.play(e.type == EnemyType.ELITE ? Sfx.BIG_EXPLOSION : Sfx.EXPLOSION);
        dropPowerUp(e);

        if (combo > 0 && combo % Config.COMBO_STEP == 0) {
            texts.spawn("COMBO x" + comboMultiplier(), e.x, e.y - 40, Palette.MAGENTA, 26, true, 1.1);
        }
    }

    public int comboMultiplier() {
        return (int) Math.min(Config.COMBO_MAX_MULTIPLIER, 1 + combo / Config.COMBO_STEP);
    }

    public void addScore(int base, double x, double y) {
        int pts = (int) Math.round(base * comboMultiplier() * difficulty.score() * (scoreBoost > 0 ? 2 : 1));
        score += pts;
        if (pts >= 40 || kills % 4 == 0) {
            texts.spawn("+" + pts, x, y - 18, Palette.AMBER, 18, true, 0.85);
        }
    }

    public void clearEnemyBulletsAround(double x, double y, double radius) {
        double r2 = radius * radius;
        for (EnemyBullet b : enemyBullets) {
            if (!b.alive) {
                continue;
            }
            if (MathUtils.distanceSq(b.x, b.y, x, y) <= r2) {
                b.alive = false;
                particles.spark(b.x, b.y, 2, b.color, 120, 0.2, 1.6);
            }
        }
    }

    public void clearAllEnemyBullets() {
        for (EnemyBullet b : enemyBullets) {
            b.alive = false;
        }
    }

    public void useBomb() {
        bombsUsed++;
        audio.play(Sfx.BOMB);
        fx.flash(Palette.WHITE, 0.8);
        fx.shake(28);
        fx.hitstop(0.14);
        texts.spawn("清屏", player.x, player.y - 96, Palette.WHITE, 34, true, 1.0);
        particles.ring(player.x, player.y, Palette.CYAN, 20, Config.BOMB_RADIUS, 0.65);
        particles.ring(player.x, player.y, Palette.WHITE, 10, Config.BOMB_RADIUS * 0.75, 0.5);
        particles.spark(player.x, player.y, 50, Palette.CYAN, 620, 0.8, 3.2);

        double r2 = Config.BOMB_RADIUS * Config.BOMB_RADIUS;
        for (EnemyBullet b : enemyBullets) {
            if (b.alive && MathUtils.distanceSq(b.x, b.y, player.x, player.y) <= r2) {
                b.alive = false;
                score += 2;
            }
        }
        for (Enemy e : new ArrayList<>(enemies)) {
            if (!e.alive) {
                continue;
            }
            if (MathUtils.distanceSq(e.x, e.y, player.x, player.y) <= (Config.BOMB_RADIUS + e.radius) * (Config.BOMB_RADIUS + e.radius)) {
                e.damage(this, Config.BOMB_DAMAGE);
            }
        }
        if (boss != null && boss.isFightable()
                && MathUtils.distanceSq(boss.x, boss.y, player.x, player.y)
                <= (Config.BOMB_RADIUS + boss.radius) * (Config.BOMB_RADIUS + boss.radius)) {
            boss.damage(this, Config.BOMB_DAMAGE);
        }
    }

    private void removeBossAndAdvance() {
        boolean wasFinal = boss.finalBoss;
        double bx = boss.x;
        double by = boss.y;
        boss = null;
        spawnExplosion(bx, by, 320, true);
        spawnExplosion(bx - 120, by + 40, 200, true);
        spawnExplosion(bx + 120, by + 40, 200, true);
        particles.explosion(bx, by, 4.0, Palette.ORANGE);
        particles.ring(bx, by, Palette.WHITE, 30, 460, 0.9);
        fx.flash(Palette.WHITE, 1.0);
        fx.shake(46);
        fx.hitstop(0.2);
        audio.play(Sfx.BIG_EXPLOSION);
        addScore(wasFinal ? 5000 : 2000, bx, by);
        for (Enemy e : new ArrayList<>(enemies)) {
            killEnemy(e);
        }
        director.onBossDefeated(this);
    }

    public void onPlayerDestroyed() {
        gameOver = true;
        audio.stopMusic();
        audio.play(Sfx.DEFEAT);
        spawnExplosion(player.x, player.y, 200, true);
        particles.explosion(player.x, player.y, 2.6, Palette.RED);
        fx.flash(Palette.RED, 0.6);
        fx.shake(34);
    }

    public void onVictory() {
        victory = true;
        audio.stopMusic();
        audio.play(Sfx.VICTORY);
        score += 5000;
        fx.flash(Palette.AMBER, 0.7);
    }

    public void announce(String title, String sub) {
        bannerTitle = title;
        bannerSub = sub;
        bannerTimer = 2.6;
    }

    // ------------------------------------------------------------------ 绘制

    public void render(Renderer r) {
        drawBackground(r);

        for (Enemy e : enemies) {
            if (e.alive) {
                e.render(r);
            }
        }
        if (boss != null) {
            boss.render(this, r);
        }
        for (PowerUp p : powerups) {
            if (p.alive) {
                p.render(r);
            }
        }
        if (player.alive) {
            player.render(r);
        }
        for (PlayerBullet b : playerBullets) {
            if (b.alive) {
                b.render(r);
            }
        }
        for (EnemyBullet b : enemyBullets) {
            if (b.alive) {
                b.render(r);
            }
        }
        for (Explosion e : explosions) {
            if (e.alive) {
                e.render(r);
            }
        }
        particles.render(r);
        texts.render(r);
    }

    private void drawBackground(Renderer r) {
        // 无缝滚动的岛屿地图：上下两张图交替
        double off = bgScroll % Config.HEIGHT;
        r.sprite(Assets.BG, Config.WIDTH / 2.0, off - Config.HEIGHT / 2.0, Config.WIDTH, Config.HEIGHT);
        r.sprite(Assets.BG, Config.WIDTH / 2.0, off + Config.HEIGHT / 2.0, Config.WIDTH, Config.HEIGHT);

        if (director.stageIndex() == 1) {
            r.verticalGradient(0, 0, Config.WIDTH, Config.HEIGHT,
                    new Color(40, 20, 80, 40), new Color(10, 0, 30, 70), 1f);
        } else if (director.stageIndex() >= 2) {
            r.verticalGradient(0, 0, Config.WIDTH, Config.HEIGHT,
                    new Color(90, 20, 20, 55), new Color(30, 0, 0, 85), 1f);
        }
    }
}
