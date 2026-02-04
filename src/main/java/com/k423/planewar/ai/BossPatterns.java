package com.k423.planewar.ai;

import com.k423.planewar.core.MathUtils;
import com.k423.planewar.entity.World;

import java.awt.Color;

/**
 * 弹幕形状工厂。Boss 和敌机都用这些静态方法，保证弹幕逻辑集中在一处、方便调参。
 * 角度约定：0 表示正右方，PI/2 表示正下方（屏幕坐标系 y 向下）。
 */
public final class BossPatterns {

    private BossPatterns() {
    }

    /** 朝向玩家的角度。 */
    public static double aimAtPlayer(World world, double x, double y) {
        if (world.player == null) {
            return Math.PI / 2;
        }
        return Math.atan2(world.player.y - y, world.player.x - x);
    }

    /** 扇形：以 baseAngle 为中心，均匀铺开 count 发。 */
    public static void fan(World world, double x, double y, double baseAngle, int count,
                           double spreadDeg, double speed, double radius, Color color) {
        if (count <= 0) {
            return;
        }
        double spread = Math.toRadians(spreadDeg);
        for (int i = 0; i < count; i++) {
            double t = count == 1 ? 0.5 : i / (double) (count - 1);
            double a = baseAngle - spread / 2 + spread * t;
            world.spawnEnemyBullet(x, y, a, speed, radius, color);
        }
    }

    /** 环形/螺旋：arms 条等角分布的子弹，调用方自己递增 angle 就成螺旋。 */
    public static void radial(World world, double x, double y, double angle, int arms,
                              double speed, double radius, Color color) {
        for (int i = 0; i < arms; i++) {
            double a = angle + i * MathUtils.TAU / arms;
            world.spawnEnemyBullet(x, y, a, speed, radius, color);
        }
    }

    /** 瞄准玩家的连发，count 发之间隔 gapDeg 度。 */
    public static void aimedBurst(World world, double x, double y, int count, double gapDeg,
                                  double speed, double radius, Color color) {
        double a = aimAtPlayer(world, x, y);
        double gap = Math.toRadians(gapDeg);
        for (int i = 0; i < count; i++) {
            double offset = (i - (count - 1) / 2.0) * gap;
            world.spawnEnemyBullet(x, y, a + offset, speed, radius, color);
        }
    }

    /** 弹幕雨：从屏幕顶部指定高度横向排开，中间留出缺口让玩家有路可走。 */
    public static void curtain(World world, double spawnY, int count, double speed, double radius, Color color) {
        int gapIndex = world.rng().nextInt(Math.max(1, count));
        double step = (double) com.k423.planewar.core.Config.WIDTH / (count + 1);
        for (int i = 0; i < count; i++) {
            if (Math.abs(i - gapIndex) <= 1) {
                continue;
            }
            world.spawnEnemyBullet(step * (i + 1), spawnY, Math.PI / 2, speed, radius, color);
        }
    }
}
