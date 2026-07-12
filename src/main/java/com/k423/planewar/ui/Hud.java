package com.k423.planewar.ui;

import com.k423.planewar.core.Config;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.entity.Boss;
import com.k423.planewar.entity.Player;
import com.k423.planewar.entity.World;
import com.k423.planewar.gfx.Assets;
import com.k423.planewar.gfx.Fonts;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

import java.awt.Color;

/**
 * HUD。像素素材只有数字，标签统一用英文；所有文字都描边，压在海面背景上也看得清。
 */
public final class Hud {

    private static final Color PANEL = new Color(10, 14, 26, 170);

    public void render(Renderer r, World world, int highScore, boolean showFps, double fps) {
        Player p = world.player;

        // 顶部信息条
        r.fillRect(0, 0, Config.WIDTH, 104, PANEL, 1f);
        r.textOutlined("SCORE", 20, 28, Fonts.bold(15), Palette.AMBER, Palette.DARK,
                Renderer.Align.LEFT, 1f);
        Assets.drawNumber(r, world.score, 16, 62, 2, null);
        r.textOutlined("BEST " + highScore, 20, 90, Fonts.plain(14), Palette.WHITE, Palette.DARK,
                Renderer.Align.LEFT, 0.95f);

        r.textOutlined("STAGE " + world.director.stageNumber(), Config.WIDTH - 20, 28,
                Fonts.bold(16), Palette.WHITE, Palette.DARK, Renderer.Align.RIGHT, 1f);
        r.textOutlined(world.director.stageName(), Config.WIDTH - 20, 48,
                Fonts.plain(14), Palette.AMBER, Palette.DARK, Renderer.Align.RIGHT, 0.9f);

        // 生命 / 炸弹
        for (int i = 0; i < p.lives; i++) {
            r.sprite(Assets.PLAYER, Config.WIDTH - 34 - i * 30, 74, 26, 26, 1f);
        }
        for (int i = 0; i < p.bombs; i++) {
            r.sprite(Assets.ICON_BOMB, Config.WIDTH - 34 - i * 28, 98, 20, 20, 1f);
        }

        // 底部：火力 / 护盾
        r.fillRect(0, Config.HEIGHT - 62, Config.WIDTH, 62, PANEL, 1f);
        r.textOutlined("POWER", 20, Config.HEIGHT - 36, Fonts.bold(13), Palette.WHITE, Palette.DARK,
                Renderer.Align.LEFT, 0.95f);
        for (int i = 0; i < Config.MAX_WEAPON_LEVEL; i++) {
            boolean on = i < p.weaponLevel;
            r.fillRect(78 + i * 18, Config.HEIGHT - 46, 14, 10,
                    on ? Palette.AMBER : new Color(60, 70, 90), 1f);
        }
        if (p.shieldTime > 0) {
            double ratio = MathUtils.clamp(p.shieldTime / 9.0, 0, 1);
            r.sprite(Assets.ICON_SHIELD, 210, Config.HEIGHT - 36, 24, 24, 1f);
            r.fillRect(226, Config.HEIGHT - 42, 110, 10, new Color(30, 40, 60), 1f);
            r.fillRect(226, Config.HEIGHT - 42, 110 * ratio, 10, Palette.CYAN, 1f);
        }
        if (world.scoreBoost > 0) {
            r.textOutlined("x2  " + Math.round(world.scoreBoost) + "s", 360, Config.HEIGHT - 30,
                    Fonts.bold(15), Palette.MAGENTA, Palette.DARK, Renderer.Align.LEFT, 1f);
        }

        // 连击
        if (world.combo > 1) {
            double a = Math.min(1, world.comboTimer / 0.8);
            String txt = world.combo + " COMBO";
            r.textOutlined(txt, Config.WIDTH - 20, Config.HEIGHT - 156, Fonts.bold(22),
                    Palette.alpha(Palette.WHITE, (float) a), Palette.DARK, Renderer.Align.RIGHT, (float) a);
            r.textOutlined("x" + world.comboMultiplier(), Config.WIDTH - 20, Config.HEIGHT - 122,
                    Fonts.bold(26), Palette.alpha(Palette.AMBER, (float) a), Palette.DARK,
                    Renderer.Align.RIGHT, (float) a);
        }

        renderBossBar(r, world);
        if (world.bossWarning > 0) {
            renderBossWarning(r, world.bossWarning);
        }
        renderBanner(r, world);

        if (showFps) {
            r.text(String.format("%.0f FPS", fps), Config.WIDTH - 14, Config.HEIGHT - 8,
                    Fonts.plain(12), Palette.WHITE, Renderer.Align.RIGHT, 0.7f);
        }
    }

    private void renderBossBar(Renderer r, World world) {
        Boss boss = world.boss;
        if (boss == null || boss.state == Boss.State.DEAD) {
            return;
        }
        double w = 440;
        double x = (Config.WIDTH - w) / 2.0;
        double y = 150;
        r.fillRect(x - 6, y - 24, w + 12, 54, new java.awt.Color(10, 14, 26, 210), 1f);
        r.textOutlined(boss.name, Config.WIDTH / 2.0, y - 6, Fonts.bold(20), Palette.WHITE, Palette.DARK,
                Renderer.Align.CENTER, 1f);

        double ratio = boss.hpRatio();
        r.fillRect(x - 2, y + 2, w + 4, 16, new Color(20, 26, 40), 1f);
        r.fillRect(x, y + 4, w * ratio, 12,
                boss.phase >= 3 ? Palette.RED : boss.phase == 2 ? Palette.ORANGE : Palette.MAGENTA, 1f);
        for (double mark : new double[]{0.33, 0.66}) {
            r.fillRect(x + w * mark, y + 2, 2, 16, Palette.WHITE, 0.6f);
        }
        r.text("PHASE " + boss.phase, x + w, y - 6, Fonts.plain(12), Palette.AMBER,
                Renderer.Align.RIGHT, 0.9f);
    }

    private void renderBossWarning(Renderer r, double timer) {
        double pulse = 0.5 + 0.5 * Math.sin(timer * 18);
        float a = (float) Math.min(1, timer);
        r.fillRect(0, 420, Config.WIDTH, 120, Palette.alpha(Palette.RED, 0.16f * (float) pulse), 1f);
        r.textOutlined("WARNING", Config.WIDTH / 2.0, 500, Fonts.bold(64),
                Palette.alpha(Palette.RED, a), Palette.DARK, Renderer.Align.CENTER, a);
        r.textOutlined("大型敌机接近", Config.WIDTH / 2.0, 540, Fonts.bold(24),
                Palette.alpha(Palette.WHITE, a * 0.95f), Palette.DARK, Renderer.Align.CENTER, a * 0.95f);
    }

    private void renderBanner(Renderer r, World world) {
        if (world.bannerTimer <= 0) {
            return;
        }
        double t = world.bannerTimer;
        double total = 2.6;
        double in = MathUtils.smoothstep(Math.min(1, (total - t) / 0.35));
        double out = MathUtils.smoothstep(Math.min(1, t / 0.5));
        float a = (float) (in * out);
        double y = 300 - (1 - in) * 24;

        r.textOutlined(world.bannerTitle, Config.WIDTH / 2.0, y, Fonts.bold(52),
                Palette.alpha(Palette.WHITE, a), Palette.DARK, Renderer.Align.CENTER, a);
        r.textOutlined(world.bannerSub, Config.WIDTH / 2.0, y + 44, Fonts.bold(22),
                Palette.alpha(Palette.AMBER, a), Palette.DARK, Renderer.Align.CENTER, a);
    }
}
