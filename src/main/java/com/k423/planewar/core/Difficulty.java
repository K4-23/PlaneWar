package com.k423.planewar.core;

/**
 * 难度。数值只做乘法叠加，保持三档之间的手感差异明显但都可通关。
 *
 * @param label         菜单显示名
 * @param enemyHp       敌机血量倍率
 * @param fireInterval  敌方开火间隔倍率（越小越密）
 * @param enemySpeed    敌机速度倍率
 * @param score         得分倍率
 * @param spawn         刷怪数量倍率
 */
public enum Difficulty {

    EASY("简单", 0.72, 1.35, 0.88, 0.8, 0.8),
    NORMAL("普通", 1.0, 1.0, 1.0, 1.0, 1.0),
    HARD("困难", 1.35, 0.8, 1.1, 1.5, 1.15);

    private final String label;
    private final double enemyHp;
    private final double fireInterval;
    private final double enemySpeed;
    private final double score;
    private final double spawn;

    Difficulty(String label, double enemyHp, double fireInterval, double enemySpeed, double score, double spawn) {
        this.label = label;
        this.enemyHp = enemyHp;
        this.fireInterval = fireInterval;
        this.enemySpeed = enemySpeed;
        this.score = score;
        this.spawn = spawn;
    }

    public String label() {
        return label;
    }

    public double enemyHp() {
        return enemyHp;
    }

    public double fireInterval() {
        return fireInterval;
    }

    public double enemySpeed() {
        return enemySpeed;
    }

    public double score() {
        return score;
    }

    public double spawn() {
        return spawn;
    }

    public Difficulty next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public Difficulty previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }

    public static Difficulty fromName(String name) {
        for (Difficulty d : values()) {
            if (d.name().equalsIgnoreCase(name)) {
                return d;
            }
        }
        return NORMAL;
    }
}
