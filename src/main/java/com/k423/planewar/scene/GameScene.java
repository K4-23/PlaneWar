package com.k423.planewar.scene;

import com.k423.planewar.audio.Sfx;
import com.k423.planewar.core.Config;
import com.k423.planewar.core.GameContext;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.core.Scene;
import com.k423.planewar.entity.World;
import com.k423.planewar.gfx.Fonts;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;
import com.k423.planewar.ui.Hud;

import java.awt.Color;

/**
 * 战斗场景。本身不碰细节玩法，只负责：驱动 World、暂停、结算面板、场景切换。
 */
public final class GameScene implements Scene {

    private final GameContext ctx;
    private final World world;
    private final Hud hud = new Hud();

    private boolean paused;
    private int pauseIndex;
    private boolean resultSaved;
    private boolean showResult;
    private boolean newRecord;
    private double resultTimer;
    private int resultIndex;
    private double fadeIn;

    public GameScene(GameContext ctx) {
        this.ctx = ctx;
        this.world = new World(ctx.rng, ctx.save.difficulty(), ctx.input, ctx.audio);
    }

    @Override
    public void enter(GameContext ctx) {
        ctx.audio.startMusic(2);
        ctx.input.clearAll();
        world.announce("关卡 1", world.director.stageName());
        fadeIn = 0;
    }

    @Override
    public void update(GameContext ctx, double dt) {
        fadeIn = Math.min(1, fadeIn + dt * 2.2);

        if (world.gameOver || world.victory) {
            handleResult(dt);
            world.update(dt);
            return;
        }

        if (paused) {
            updatePause();
            return;
        }

        if (ctx.input.pause()) {
            paused = true;
            pauseIndex = 0;
            ctx.audio.play(Sfx.UI_CLICK, 0.6f, 1f);
            return;
        }

        world.update(dt * world.fx.timeScale());
    }

    private void updatePause() {
        if (ctx.input.up()) {
            pauseIndex = (pauseIndex + 2) % 3;
            ctx.audio.play(Sfx.UI_HOVER, 0.5f, 1f);
            ctx.input.clearEdges();
        }
        if (ctx.input.down()) {
            pauseIndex = (pauseIndex + 1) % 3;
            ctx.audio.play(Sfx.UI_HOVER, 0.5f, 1f);
            ctx.input.clearEdges();
        }
        if (ctx.input.pause() || ctx.input.justPressed(java.awt.event.KeyEvent.VK_ENTER)) {
            paused = false;
            ctx.audio.play(Sfx.UI_CLICK, 0.6f, 1f);
            return;
        }
        if (ctx.input.confirm()) {
            switch (pauseIndex) {
                case 0 -> paused = false;
                case 1 -> ctx.scenes.set(new GameScene(ctx));
                default -> ctx.scenes.set(new MenuScene(ctx));
            }
        }
    }

    private void handleResult(double dt) {
        resultTimer += dt;
        if (!resultSaved && world.endDelay > 0.4) {
            newRecord = ctx.save.submitResult(world.score, world.director.stageNumber(), world.maxCombo,
                    world.kills, world.runTime);
            resultSaved = true;
        }
        if (resultTimer > 1.4) {
            showResult = true;
        }
        if (!showResult) {
            return;
        }
        if (ctx.input.up()) {
            resultIndex = (resultIndex + 1) % 2;
            ctx.audio.play(Sfx.UI_HOVER, 0.5f, 1f);
        }
        if (ctx.input.down()) {
            resultIndex = (resultIndex + 1) % 2;
            ctx.audio.play(Sfx.UI_HOVER, 0.5f, 1f);
        }
        if (ctx.input.justPressed(java.awt.event.KeyEvent.VK_R)) {
            ctx.scenes.set(new GameScene(ctx));
            return;
        }
        if (ctx.input.justPressed(java.awt.event.KeyEvent.VK_ENTER)) {
            ctx.scenes.set(new GameScene(ctx));
            return;
        }
        if (ctx.input.justPressed(java.awt.event.KeyEvent.VK_ESCAPE)) {
            ctx.scenes.set(new MenuScene(ctx));
            return;
        }
        if (ctx.input.mouseJustPressed()) {
            double mx = ctx.input.mouseX();
            double my = ctx.input.mouseY();
            if (my > 850 && my < 910) {
                ctx.scenes.set(new GameScene(ctx));
            } else if (my > 930 && my < 990) {
                ctx.scenes.set(new MenuScene(ctx));
            }
            if (mx > 0) {
                ctx.audio.play(Sfx.UI_CLICK, 0.6f, 1f);
            }
        }
    }

    @Override
    public void render(GameContext ctx, Renderer r) {
        // 世界
        r.push();
        r.translate(world.fx.shakeX(), world.fx.shakeY());
        world.render(r);
        r.pop();

        // 屏幕效果
        world.fx.render(r);

        // HUD
        hud.render(r, world, ctx.save.highScore(), ctx.save.showFps(), ctx.fps);

        if (ctx.save.crtEnabled()) {
            r.scanlines(0.10f);
        }

        if (paused) {
            renderPause(r);
        }
        if (showResult) {
            renderResult(r);
        }
        if (fadeIn < 1) {
            r.flash(Color.BLACK, 1f - (float) MathUtils.smoothstep(fadeIn));
        }
    }

    private void renderPause(Renderer r) {
        r.flash(Palette.alpha(Palette.VOID, 0.72f));
        r.textGlow("暂停", Config.WIDTH / 2.0, 430, Fonts.bold(64), Palette.WHITE, Palette.CYAN,
                Renderer.Align.CENTER, 1f);
        r.text("PAUSED", Config.WIDTH / 2.0, 470, Fonts.plain(18), Palette.GREY,
                Renderer.Align.CENTER, 0.8f);

        String[] items = {"继续战斗", "重新开始", "返回主菜单"};
        for (int i = 0; i < items.length; i++) {
            double y = 570 + i * 70;
            boolean sel = i == pauseIndex;
            if (sel) {
                r.fillRect(Config.WIDTH / 2.0 - 170, y - 34, 340, 52, Palette.alpha(Palette.CYAN, 0.12f), 1f);
                r.fillRect(Config.WIDTH / 2.0 - 170, y - 34, 4, 52, Palette.CYAN, 1f);
            }
            r.text(items[i], Config.WIDTH / 2.0, y, Fonts.bold(sel ? 28 : 25),
                    sel ? Palette.CYAN : Palette.GREY, Renderer.Align.CENTER, sel ? 1f : 0.75f);
        }
        r.text("↑↓ 选择 · Enter 确认 · ESC 继续", Config.WIDTH / 2.0, 830, Fonts.plain(18),
                Palette.GREY, Renderer.Align.CENTER, 0.7f);
    }

    private void renderResult(Renderer r) {
        double k = MathUtils.smoothstep(Math.min(1, (resultTimer - 1.4) / 0.5));
        r.flash(Palette.alpha(Palette.VOID, (float) (0.82 * k)));
        double slide = (1 - k) * 40;

        String title = world.victory ? "任务完成" : "任务失败";
        Color titleColor = world.victory ? Palette.AMBER : Palette.RED;
        r.textGlow(title, Config.WIDTH / 2.0, 300 + slide, Fonts.bold(72), titleColor,
                titleColor, Renderer.Align.CENTER, (float) k);
        r.text(world.victory ? "VICTORY" : "GAME OVER", Config.WIDTH / 2.0, 336 + slide,
                Fonts.plain(18), Palette.GREY, Renderer.Align.CENTER, (float) (k * 0.8));

        int best = ctx.save.highScore();
        drawStat(r, "得分", String.format("%07d", world.score), 420, k);
        drawStat(r, "击坠", world.kills + " 架", 470, k);
        drawStat(r, "最高连击", world.maxCombo + "", 520, k);
        drawStat(r, "抵达关卡", world.director.stageNumber() + " / 3", 570, k);
        drawStat(r, "用时", String.format("%02d:%02d", (int) (world.runTime / 60), (int) (world.runTime % 60)), 620, k);

        if (newRecord) {
            double pulse = 0.5 + 0.5 * Math.sin(world.time * 8);
            r.textGlow("新纪录！", Config.WIDTH / 2.0, 700, Fonts.bold(34), Palette.MAGENTA,
                    Palette.MAGENTA, Renderer.Align.CENTER, (float) (k * (0.7 + pulse * 0.3)));
        } else {
            r.text("历史最高 " + String.format("%07d", best), Config.WIDTH / 2.0, 700, Fonts.plain(20),
                    Palette.GREY, Renderer.Align.CENTER, (float) (k * 0.85));
        }

        drawButton(r, "再来一局  (Enter)", 880, resultIndex == 0, k);
        drawButton(r, "返回主菜单  (ESC)", 960, resultIndex == 1, k);
    }

    private void drawStat(Renderer r, String label, String value, double y, double k) {
        r.text(label, 190, y, Fonts.plain(22), Palette.alpha(Palette.GREY, (float) (k * 0.9)),
                Renderer.Align.LEFT, (float) k);
        r.text(value, 530, y, Fonts.bold(24), Palette.alpha(Palette.WHITE, (float) k),
                Renderer.Align.RIGHT, (float) k);
    }

    private void drawButton(Renderer r, String label, double y, boolean sel, double k) {
        if (sel) {
            r.fillRect(Config.WIDTH / 2.0 - 200, y - 30, 400, 46, Palette.alpha(Palette.CYAN, 0.12f * (float) k), 1f);
        }
        r.text(label, Config.WIDTH / 2.0, y, Fonts.bold(24),
                Palette.alpha(sel ? Palette.CYAN : Palette.GREY, (float) k),
                Renderer.Align.CENTER, (float) k);
    }
}
