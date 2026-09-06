package com.k423.planewar.entity;

import com.k423.planewar.core.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BossTest {

    @Test
    void phaseFollowsHpRatio() {
        Boss boss = new Boss("测试 Boss", false, 1000);
        boss.state = Boss.State.FIGHT;
        assertEquals(1, boss.phase);
    }

    @Test
    void hpRatioIsClamped() {
        Boss boss = new Boss("测试 Boss", false, 1000);
        assertEquals(1.0, boss.hpRatio(), 1e-9);
        boss.hp = -500;
        assertEquals(0.0, boss.hpRatio(), 1e-9);
    }

    @Test
    void enterStateIsInvulnerable() {
        Boss boss = new Boss("测试 Boss", false, 1000);
        assertEquals(Boss.State.ENTER, boss.state);
        assertEquals(1000, boss.hp);
    }

    @Test
    void finalBossFlagSurvives() {
        Boss boss = new Boss("深渊母舰", true, 5400);
        assertTrue(boss.finalBoss);
        assertEquals(Config.WIDTH / 2.0, boss.x, 1e-9);
    }
}
