package com.k423.planewar.ai;

import com.k423.planewar.core.Difficulty;
import com.k423.planewar.core.Rng;
import com.k423.planewar.entity.Boss;
import com.k423.planewar.entity.Enemy;
import com.k423.planewar.entity.EnemyType;
import com.k423.planewar.entity.World;

/**
 * 关卡导演：按波次刷敌机，清完一波刷 Boss，Boss 倒下进下一关。
 * 三关的节奏：峡谷空域（教学） → 风暴前线（压迫） → 深渊母舰（终局）。
 */
public final class SpawnDirector {

    private static final String[] STAGE_NAMES = {"峡谷空域", "风暴前线", "深渊母舰"};
    private static final String[] BOSS_NAMES = {"峡谷哨卫", "风暴巨兽", "深渊母舰"};
    private static final int[] GROUPS_PER_STAGE = {9, 11, 13};
    private static final double[] GROUP_INTERVAL = {3.0, 2.6, 2.2};

    private final Rng rng;
    private int stageIndex;
    private int groupsSpawned;
    private double spawnTimer = 2.0;
    private double bossWarnTimer;
    private boolean bossSpawned;
    private boolean finished;

    public SpawnDirector(Rng rng) {
        this.rng = rng;
    }

    public int stageIndex() {
        return stageIndex;
    }

    public int stageNumber() {
        return Math.min(3, stageIndex + 1);
    }

    public String stageName() {
        return STAGE_NAMES[Math.min(stageIndex, STAGE_NAMES.length - 1)];
    }

    public boolean bossActive() {
        return bossSpawned;
    }

    public boolean finished() {
        return finished;
    }

    public double bossWarnTimer() {
        return bossWarnTimer;
    }

    public void update(World world, double dt) {
        if (finished) {
            return;
        }
        if (bossSpawned) {
            return;
        }

        if (bossWarnTimer > 0) {
            bossWarnTimer -= dt;
            world.bossWarning = Math.max(world.bossWarning, bossWarnTimer);
            if (bossWarnTimer <= 0) {
                spawnBoss(world);
            }
            return;
        }

        spawnTimer -= dt;
        if (spawnTimer <= 0) {
            spawnTimer = groupInterval(world.difficulty);
            spawnGroup(world);
            groupsSpawned++;
            if (groupsSpawned >= GROUPS_PER_STAGE[Math.min(stageIndex, GROUPS_PER_STAGE.length - 1)]) {
                bossWarnTimer = 3.2;
                world.announce("警告", "大型敌机接近");
                world.audio.play(com.k423.planewar.audio.Sfx.BOSS_WARNING);
            }
        }
    }

    private double groupInterval(Difficulty d) {
        double base = GROUP_INTERVAL[Math.min(stageIndex, GROUP_INTERVAL.length - 1)];
        return base / Math.max(0.6, d.spawn());
    }

    public void onBossDefeated(World world) {
        bossSpawned = false;
        stageIndex++;
        groupsSpawned = 0;
        spawnTimer = 4.5;
        bossWarnTimer = 0;
        world.bossWarning = 0;
        if (stageIndex >= STAGE_NAMES.length) {
            finished = true;
            world.onVictory();
        } else {
            world.announce("关卡 " + (stageIndex + 1), STAGE_NAMES[stageIndex]);
            world.audio.startMusic(2);
        }
    }

    private void spawnBoss(World world) {
        bossSpawned = true;
        int baseHp = switch (stageIndex) {
            case 0 -> 2600;
            case 1 -> 3900;
            default -> 5400;
        };
        int hp = (int) Math.round(baseHp * world.difficulty.enemyHp());
        boolean finalBoss = stageIndex >= STAGE_NAMES.length - 1;
        world.spawnBoss(BOSS_NAMES[Math.min(stageIndex, BOSS_NAMES.length - 1)], finalBoss, hp);
        world.announce("BOSS", BOSS_NAMES[Math.min(stageIndex, BOSS_NAMES.length - 1)]);
        world.audio.startMusic(3);
    }

    // ------------------------------------------------------------------ 刷怪编队

    private void spawnGroup(World world) {
        int roll = rng.nextInt(100);
        if (stageIndex == 0) {
            if (roll < 45) {
                line(world, EnemyType.SCOUT, 4, Enemy.PATTERN_STRAIGHT);
            } else if (roll < 75) {
                vee(world, 5);
            } else if (roll < 92) {
                line(world, EnemyType.FIGHTER, 3, Enemy.PATTERN_HOVER);
            } else {
                world.spawnEnemy(EnemyType.TANK, com.k423.planewar.core.Config.WIDTH / 2.0, -90, 210, Enemy.PATTERN_HOVER);
            }
        } else if (stageIndex == 1) {
            if (roll < 35) {
                line(world, EnemyType.FIGHTER, 4, Enemy.PATTERN_HOVER);
            } else if (roll < 60) {
                line(world, EnemyType.SCOUT, 6, Enemy.PATTERN_DIVE);
            } else if (roll < 80) {
                world.spawnEnemy(EnemyType.ELITE, com.k423.planewar.core.Config.WIDTH / 2.0, -90, 300, Enemy.PATTERN_ORBIT);
            } else {
                world.spawnEnemy(EnemyType.TANK, 220, -90, 200, Enemy.PATTERN_HOVER);
                world.spawnEnemy(EnemyType.TANK, 500, -150, 240, Enemy.PATTERN_HOVER);
            }
        } else {
            if (roll < 30) {
                line(world, EnemyType.FIGHTER, 5, Enemy.PATTERN_HOVER);
            } else if (roll < 55) {
                line(world, EnemyType.SCOUT, 7, Enemy.PATTERN_DIVE);
            } else if (roll < 75) {
                world.spawnEnemy(EnemyType.ELITE, com.k423.planewar.core.Config.WIDTH / 2.0, -90, 300, Enemy.PATTERN_ORBIT);
                world.spawnEnemy(EnemyType.ELITE, com.k423.planewar.core.Config.WIDTH / 2.0, -260, 380, Enemy.PATTERN_ORBIT);
            } else if (roll < 90) {
                line(world, EnemyType.TANK, 2, Enemy.PATTERN_HOVER);
            } else {
                line(world, EnemyType.FIGHTER, 3, Enemy.PATTERN_HOVER);
                line(world, EnemyType.SCOUT, 4, Enemy.PATTERN_STRAIGHT);
            }
        }
    }

    private void line(World world, EnemyType type, int count, int pattern) {
        double step = com.k423.planewar.core.Config.WIDTH / (count + 1.0);
        for (int i = 0; i < count; i++) {
            world.spawnEnemy(type, step * (i + 1), -80 - i * 42, 230 + i * 18, pattern);
        }
    }

    private void vee(World world, int count) {
        double center = com.k423.planewar.core.Config.WIDTH / 2.0;
        for (int i = 0; i < count; i++) {
            double offset = (i - (count - 1) / 2.0) * 96;
            world.spawnEnemy(EnemyType.SCOUT, center + offset, -80 - Math.abs(i - (count - 1) / 2.0) * 40,
                    240, Enemy.PATTERN_STRAIGHT);
        }
    }
}
