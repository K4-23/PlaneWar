package com.k423.planewar.core;

/**
 * 全局常量。分辨率沿用原始素材的 720x1280 竖屏比例，所有逻辑坐标都在这套虚拟画布上，
 * 窗口大小变化只影响缩放，不影响玩法数值。
 */
public final class Config {

    private Config() {
    }

    // ---------- 画布 ----------
    public static final int WIDTH = 720;
    public static final int HEIGHT = 1280;

    /** 固定步长：逻辑每秒更新 60 次，与渲染帧率解耦。 */
    public static final double FIXED_DT = 1.0 / 60.0;
    /** 单帧最多补算几次，防止卡顿后一次性补算导致的"瞬移"。 */
    public static final int MAX_CATCH_UP = 3;

    // ---------- 玩家 ----------
    public static final double PLAYER_SPEED = 380;
    public static final double PLAYER_FOCUS_SPEED = 205;
    public static final double PLAYER_RADIUS = 15;
    public static final int PLAYER_LIVES = 4;
    public static final double RESPAWN_INVULN = 2.0;
    public static final double START_FIRE_INTERVAL = 0.14;
    public static final double FIRE_INTERVAL_PER_LEVEL = 0.008;
    public static final double MIN_FIRE_INTERVAL = 0.07;

    // ---------- 子弹 ----------
    public static final double PLAYER_BULLET_SPEED = 1080;
    public static final int PLAYER_BULLET_DAMAGE = 10;
    public static final double ENEMY_BULLET_SPEED = 260;

    // ---------- 战斗节奏 ----------
    public static final double COMBO_WINDOW = 2.0;
    public static final int COMBO_STEP = 6;
    public static final int COMBO_MAX_MULTIPLIER = 5;
    public static final double BOMB_RADIUS = 400;
    public static final int BOMB_DAMAGE = 220;

    // ---------- 道具 ----------
    public static final double POWERUP_FALL_SPEED = 90;
    public static final double POWERUP_MAGNET_RADIUS = 120;
    public static final int MAX_WEAPON_LEVEL = 4;

    // ---------- 特效 ----------
    public static final int MAX_PARTICLES = 2000;
    public static final int MAX_FLOATING_TEXT = 64;
    public static final double SHAKE_DECAY = 4.5;

    // ---------- 存档 ----------
    public static final String SAVE_DIR = ".planewar";
    public static final String SAVE_FILE = "save.properties";

    public static final String TITLE = "Plane War";
    public static final String VERSION = "2.0.0";

    /** 把 x 限制在可玩区域内（留出机体半径）。 */
    public static double clampX(double x, double radius) {
        return Math.max(radius, Math.min(WIDTH - radius, x));
    }

    /** 把 y 限制在可玩区域内（留出机体半径）。 */
    public static double clampY(double y, double radius) {
        return Math.max(radius, Math.min(HEIGHT - radius, y));
    }
}
