package com.k423.planewar.entity;

/**
 * 敌机类型表。数值集中放这里，方便平衡性调参和单元测试。
 */
public enum EnemyType {

    /** 侦察机：脆、快，直线俯冲，偶尔点射。 */
    SCOUT("侦察机", 26, 15, 10, 46, 38, 32, 2.6, 120),
    /** 战斗机：中等血量，进入后横向游走，三连扇形。 */
    FIGHTER("战斗机", 62, 20, 24, 60, 76, 42, 2.2, 96),
    /** 重装机：慢、厚、抗揍，发射大颗慢弹。 */
    TANK("重装机", 150, 27, 46, 84, 92, 55, 2.4, 68),
    /** 精英机：绕圈游走，螺旋弹幕。 */
    ELITE("精英机", 230, 29, 88, 74, 84, 60, 1.35, 74),
    /** 无人机：Boss 召唤的小怪，贴身骚扰。 */
    DRONE("无人机", 18, 12, 6, 34, 40, 22, 2.2, 138);

    private final String label;
    private final int hp;
    private final double radius;
    private final int score;
    private final double spriteW;
    private final double spriteH;
    private final int contactDamage;
    private final double fireInterval;
    private final double speed;

    EnemyType(String label, int hp, double radius, int score, double spriteW, double spriteH,
              int contactDamage, double fireInterval, double speed) {
        this.label = label;
        this.hp = hp;
        this.radius = radius;
        this.score = score;
        this.spriteW = spriteW;
        this.spriteH = spriteH;
        this.contactDamage = contactDamage;
        this.fireInterval = fireInterval;
        this.speed = speed;
    }

    public String label() {
        return label;
    }

    public int hp() {
        return hp;
    }

    public double radius() {
        return radius;
    }

    public int score() {
        return score;
    }

    public double spriteW() {
        return spriteW;
    }

    public double spriteH() {
        return spriteH;
    }

    public int contactDamage() {
        return contactDamage;
    }

    public double fireInterval() {
        return fireInterval;
    }

    public double speed() {
        return speed;
    }

    /** 血量高的机体掉率也高。 */
    public double dropChance() {
        return switch (this) {
            case SCOUT -> 0.055;
            case DRONE -> 0.03;
            case FIGHTER -> 0.16;
            case TANK -> 0.42;
            case ELITE -> 0.75;
        };
    }
}
