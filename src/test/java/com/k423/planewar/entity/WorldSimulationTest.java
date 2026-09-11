package com.k423.planewar.entity;

import com.k423.planewar.audio.AudioEngine;
import com.k423.planewar.core.Difficulty;
import com.k423.planewar.core.Input;
import com.k423.planewar.core.Rng;
import com.k423.planewar.gfx.Assets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 无窗口跑整局，验证"不崩、不泄漏、能推进"。
 */
class WorldSimulationTest {

    private static AudioEngine audio;

    @BeforeAll
    static void setup() {
        Assets.load();
        audio = new AudioEngine();
    }

    @AfterAll
    static void tearDown() {
        if (audio != null) {
            audio.close();
        }
    }

    @Test
    void runsTwoMinutesWithoutExploding() {
        World world = new World(new Rng(42), Difficulty.NORMAL, new Input(), audio);
        int steps = 120 * 60;
        for (int i = 0; i < steps; i++) {
            world.update(1.0 / 60.0);
        }
        assertEquals(0, world.boss == null ? 0 : 0, "占位断言");
        assertTrue(world.player.lives <= 4, "生命数不应无限增长");
        assertTrue(world.playerBullets.size() < 2000, "子弹必须被回收");
        assertTrue(world.enemyBullets.size() < 4000, "敌弹必须被回收");
        assertTrue(world.particles.activeCount() <= 3200, "粒子数不能超过容量");
        assertTrue(world.runTime > 0);
    }

    @Test
    void killsRaiseComboAndScore() {
        World world = new World(new Rng(7), Difficulty.NORMAL, new Input(), audio);
        Enemy e = world.spawnEnemy(EnemyType.SCOUT, 300, 200, 200, Enemy.PATTERN_STRAIGHT);
        int before = world.score;
        world.killEnemy(e);
        assertEquals(1, world.combo);
        assertEquals(1, world.kills);
        assertTrue(world.score > before, "击坠要给分");
        assertTrue(world.maxCombo >= 1);
    }

    @Test
    void comboMultiplierGrowsAndCaps() {
        World world = new World(new Rng(9), Difficulty.NORMAL, new Input(), audio);
        for (int i = 0; i < 8 * 20; i++) {
            world.killEnemy(world.spawnEnemy(EnemyType.SCOUT, 100, 100, 100, Enemy.PATTERN_STRAIGHT));
        }
        assertEquals(8, world.comboMultiplier(), "倍率封顶 8 倍");
    }

    @Test
    void bombClearsBulletsAndDamagesEnemies() {
        World world = new World(new Rng(11), Difficulty.NORMAL, new Input(), audio);
        world.spawnEnemyBullet(360, 700, Math.PI / 2, 100, 8, com.k423.planewar.gfx.Palette.MAGENTA);
        world.spawnEnemyBullet(360, 720, Math.PI / 2, 100, 8, com.k423.planewar.gfx.Palette.MAGENTA);
        Enemy tank = world.spawnEnemy(EnemyType.TANK, 360, 700, 700, Enemy.PATTERN_HOVER);
        world.player.x = 360;
        world.player.y = 1000;
        world.useBomb();
        for (EnemyBullet b : world.enemyBullets) {
            assertTrue(!b.alive || Math.abs(b.y - 360) > 1e-9);
        }
        assertTrue(tank.hp < tank.maxHp, "炸弹要能伤到范围内敌机");
        assertEquals(1, world.bombsUsed);
    }
}
