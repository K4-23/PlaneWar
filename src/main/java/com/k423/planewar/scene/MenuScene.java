package com.k423.planewar.scene;

import com.k423.planewar.audio.Sfx;
import com.k423.planewar.core.Config;
import com.k423.planewar.core.Difficulty;
import com.k423.planewar.core.GameContext;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.core.Scene;
import com.k423.planewar.gfx.Assets;
import com.k423.planewar.gfx.Fonts;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

import java.awt.Color;
import java.awt.event.KeyEvent;

/**
 * 主菜单。标题、开始、难度、帮助、设置、退出都在这里，用键盘或鼠标操作都行。
 */
public final class MenuScene implements Scene {

    private enum Page {MAIN, HELP, SETTINGS}

    private static final String[] MAIN_ITEMS = {"开始游戏", "难度", "操作说明", "设置", "退出游戏"};

    private final GameContext ctx;
    private double bgScroll;
    private Page page = Page.MAIN;
    private int index;
    private int settingsIndex;
    private double t;
    private double shipX;
    private double shipY;
    private double lastHoverSound;

    public MenuScene(GameContext ctx) {
        this.ctx = ctx;
        this.shipX = Config.WIDTH * 0.18;
        this.shipY = 190;
    }

    @Override
    public void enter(GameContext ctx) {
        ctx.audio.startMusic(1);
        ctx.input.clearAll();
    }

    @Override
    public void update(GameContext ctx, double dt) {
        t += dt;
        bgScroll += 26 * dt;
        if (bgScroll > Config.HEIGHT) {
            bgScroll -= Config.HEIGHT;
        }
        shipX = Config.WIDTH * 0.5 + Math.sin(t * 0.55) * 160;
        shipY = 205 + Math.sin(t * 0.9) * 16;

        if (page == Page.MAIN) {
            updateMain();
        } else if (page == Page.SETTINGS) {
            updateSettings();
        } else {
            if (ctx.input.back() || ctx.input.confirm() || ctx.input.mouseJustPressed()) {
                page = Page.MAIN;
                ctx.audio.play(Sfx.UI_CLICK, 0.6f, 1f);
            }
        }
    }

    private void updateMain() {
        if (ctx.input.up()) {
            index = (index + MAIN_ITEMS.length - 1) % MAIN_ITEMS.length;
            hover();
        }
        if (ctx.input.down()) {
            index = (index + 1) % MAIN_ITEMS.length;
            hover();
        }
        if (index == 1) {
            if (ctx.input.left()) {
                ctx.save.setDifficulty(ctx.save.difficulty().previous());
                ctx.save.save();
                hover();
            }
            if (ctx.input.right()) {
                ctx.save.setDifficulty(ctx.save.difficulty().next());
                ctx.save.save();
                hover();
            }
        }

        int hovered = itemAt(ctx.input.mouseX(), ctx.input.mouseY(), 640, 68, MAIN_ITEMS.length);
        if (hovered >= 0 && hovered != index) {
            index = hovered;
            hover();
        }
        if (ctx.input.mouseJustPressed() && hovered >= 0) {
            activateMain();
            return;
        }
        if (ctx.input.confirm()) {
            activateMain();
        }
        if (ctx.input.justPressed(KeyEvent.VK_ESCAPE)) {
            ctx.requestQuit();
        }
    }

    private void activateMain() {
        ctx.audio.play(Sfx.UI_CLICK, 0.7f, 1f);
        switch (index) {
            case 0 -> ctx.scenes.set(new GameScene(ctx));
            case 1 -> {
                ctx.save.setDifficulty(ctx.save.difficulty().next());
                ctx.save.save();
            }
            case 2 -> page = Page.HELP;
            case 3 -> {
                page = Page.SETTINGS;
                settingsIndex = 0;
            }
            default -> ctx.requestQuit();
        }
    }

    private void updateSettings() {
        int hovered = itemAt(ctx.input.mouseX(), ctx.input.mouseY(), SETTINGS_START_Y, SETTINGS_STEP, 6);
        if (hovered >= 0) {
            settingsIndex = hovered;
        }

        if (ctx.input.up()) {
            settingsIndex = (settingsIndex + 5) % 6;
            hover();
        }
        if (ctx.input.down()) {
            settingsIndex = (settingsIndex + 1) % 6;
            hover();
        }
        if (ctx.input.back()) {
            page = Page.MAIN;
            ctx.audio.play(Sfx.UI_CLICK, 0.6f, 1f);
            return;
        }
        boolean left = ctx.input.left();
        boolean right = ctx.input.right();
        if (ctx.input.mouseJustPressed() && hovered >= 0) {
            left = true;
            right = true;
        }
        if (left || right) {
            int dir = left ? -1 : 1;
            switch (settingsIndex) {
                case 0 -> ctx.save.setMusicVolume(ctx.save.musicVolume() + dir * 0.1f);
                case 1 -> ctx.save.setSfxVolume(ctx.save.sfxVolume() + dir * 0.1f);
                case 2 -> ctx.save.setCrtEnabled(!ctx.save.crtEnabled());
                case 3 -> ctx.save.setShowFps(!ctx.save.showFps());
                case 4 -> ctx.save.setDifficulty(dir > 0 ? ctx.save.difficulty().next() : ctx.save.difficulty().previous());
                default -> {
                    page = Page.MAIN;
                }
            }
            ctx.save.save();
            ctx.applyVolumes();
            ctx.audio.play(Sfx.UI_HOVER, 0.5f, 1f);
            ctx.input.clearEdges();
        }
        if (ctx.input.confirm() && settingsIndex == 5) {
            page = Page.MAIN;
            ctx.audio.play(Sfx.UI_CLICK, 0.6f, 1f);
        }
    }

    private double[] settingsRows() {
        return new double[]{SETTINGS_START_Y, SETTINGS_STEP, 6};
    }

    private static final double SETTINGS_START_Y = 560;
    private static final double SETTINGS_STEP = 62;

    private int itemAt(double mx, double my, double startY, double step, int count) {
        if (mx < Config.WIDTH / 2.0 - 220 || mx > Config.WIDTH / 2.0 + 220) {
            return -1;
        }
        for (int i = 0; i < count; i++) {
            double y = startY + i * step;
            if (my > y - 34 && my < y + 38) {
                return i;
            }
        }
        return -1;
    }

    private void hover() {
        if (t - lastHoverSound < 0.04) {
            return;
        }
        lastHoverSound = t;
        ctx.audio.play(Sfx.UI_HOVER, 0.45f, 1f);
    }

    // ------------------------------------------------------------------ 绘制

    @Override
    public void render(GameContext ctx, Renderer r) {
        renderBackground(r);
        if (page == Page.MAIN) {
            renderFlyingShip(r);
        }

        if (page == Page.MAIN) {
            renderMain(r);
        } else if (page == Page.HELP) {
            renderHelp(r);
        } else {
            renderSettings(r);
        }

        if (ctx.save.crtEnabled()) {
            r.scanlines(0.18f);
        }
        r.vignette(0.7f);
    }

    private void renderBackground(Renderer r) {
        double off = bgScroll % Config.HEIGHT;
        r.sprite(Assets.BG, Config.WIDTH / 2.0, off - Config.HEIGHT / 2.0, Config.WIDTH, Config.HEIGHT);
        r.sprite(Assets.BG, Config.WIDTH / 2.0, off + Config.HEIGHT / 2.0, Config.WIDTH, Config.HEIGHT);
        r.flash(new Color(4, 8, 18, 190));
    }

    private void renderFlyingShip(Renderer r) {
        r.push();
        r.translate(shipX, shipY);
        r.rotate(Math.sin(t * 0.9) * 0.08);
        r.sprite(Assets.PLAYER, 0, 0, 96, 96, 1f);
        r.pop();
        double flame = 0.6 + 0.4 * Math.sin(t * 12);
        r.fillRect(shipX - 16, shipY + 46, 8, 12 + flame * 8, Palette.AMBER, 0.9f);
        r.fillRect(shipX + 8, shipY + 46, 8, 12 + flame * 8, Palette.AMBER, 0.9f);
    }

    private void renderMain(Renderer r) {
        double bob = Math.sin(t * 1.2) * 4;
        r.textGlow("PLANE WAR", Config.WIDTH / 2.0, 460 + bob, Fonts.bold(78), Palette.WHITE,
                Palette.CYAN, Renderer.Align.CENTER, 1f);
        r.text("峡 谷 空 域 · 重 构 版", Config.WIDTH / 2.0, 516 + bob, Fonts.plain(20),
                Palette.ICE, Renderer.Align.CENTER, 0.85f);
        r.fillRect(Config.WIDTH / 2.0 - 150, 540 + bob, 300, 2, Palette.CYAN, 0.6f);

        for (int i = 0; i < MAIN_ITEMS.length; i++) {
            double y = 640 + i * 68;
            boolean sel = i == index;
            String label = MAIN_ITEMS[i];
            if (i == 1) {
                label = label + "： " + ctx.save.difficulty().label();
            }
            if (sel) {
                double glow = 0.10 + 0.05 * Math.sin(t * 5);
                r.fillRect(Config.WIDTH / 2.0 - 190, y - 34, 380, 52, Palette.alpha(Palette.CYAN, (float) glow), 1f);
                r.fillRect(Config.WIDTH / 2.0 - 190, y - 34, 3, 52, Palette.CYAN, 0.95f);
                r.fillRect(Config.WIDTH / 2.0 + 187, y - 34, 3, 52, Palette.CYAN, 0.95f);
            }
            r.text(label, Config.WIDTH / 2.0, y, Fonts.bold(sel ? 30 : 26),
                    sel ? Palette.CYAN : Palette.GREY, Renderer.Align.CENTER, sel ? 1f : 0.78f);
            if (i == 1 && sel) {
                r.text("← / → 调整", Config.WIDTH / 2.0, y + 34, Fonts.plain(15), Palette.GREY,
                        Renderer.Align.CENTER, 0.6f);
            }
        }

        double footerY = Config.HEIGHT - 120;
        r.text("最高分  " + String.format("%07d", ctx.save.highScore()), Config.WIDTH / 2.0, footerY,
                Fonts.plain(20), Palette.AMBER, Renderer.Align.CENTER, 0.9f);
        r.text("累计击坠 " + ctx.save.totalKills() + " · 游玩 " + ctx.save.totalPlays() + " 局",
                Config.WIDTH / 2.0, footerY + 32, Fonts.plain(16), Palette.GREY, Renderer.Align.CENTER, 0.65f);
        r.text("↑↓ 选择 · Enter 确认 · 鼠标可直接点选", Config.WIDTH / 2.0, Config.HEIGHT - 36,
                Fonts.plain(15), Palette.GREY, Renderer.Align.CENTER, 0.45f);
        r.text("v" + Config.VERSION, Config.WIDTH - 20, Config.HEIGHT - 12, Fonts.plain(12),
                Palette.GREY, Renderer.Align.RIGHT, 0.35f);
    }

    private void renderHelp(Renderer r) {
        r.flash(Palette.alpha(Palette.VOID, 0.78f));
        r.textGlow("操作说明", Config.WIDTH / 2.0, 210, Fonts.bold(46), Palette.WHITE, Palette.CYAN,
                Renderer.Align.CENTER, 1f);
        r.fillRect(Config.WIDTH / 2.0 - 120, 232, 240, 2, Palette.CYAN, 0.6f);

        String[][] lines = {
                {"移动", "WASD / 方向键，或直接用鼠标拖"},
                {"射击", "自动开火，专心走位"},
                {"炸弹", "X 或 K，清空弹幕并重创敌人"},
                {"精确移动", "按住 Shift 减速，便于钻缝"},
                {"暂停", "ESC 或 P"},
                {"护盾", "挡一次伤害，拾取 S 道具获得"},
                {"连击", "持续击坠可叠加倍率，最高 x8"},
                {"Boss", "血量降到 2/3、1/3 时进入新阶段"},
        };
        for (int i = 0; i < lines.length; i++) {
            double y = 320 + i * 62;
            r.text(lines[i][0], 150, y, Fonts.bold(22), Palette.CYAN, Renderer.Align.LEFT, 0.9f);
            r.text(lines[i][1], 300, y, Fonts.plain(20), Palette.WHITE, Renderer.Align.LEFT, 0.85f);
        }
        r.text("Enter / ESC 返回", Config.WIDTH / 2.0, Config.HEIGHT - 90, Fonts.plain(18),
                Palette.GREY, Renderer.Align.CENTER, 0.6f);
    }

    private void renderSettings(Renderer r) {
        r.flash(Palette.alpha(Palette.VOID, 0.78f));
        r.textGlow("设置", Config.WIDTH / 2.0, 250, Fonts.bold(46), Palette.WHITE, Palette.CYAN,
                Renderer.Align.CENTER, 1f);

        String[] labels = new String[6];
        labels[0] = "音乐音量";
        labels[1] = "音效音量";
        labels[2] = "扫描线效果";
        labels[3] = "显示帧率";
        labels[4] = "默认难度";
        labels[5] = "返回";

        for (int i = 0; i < labels.length; i++) {
            double y = 560 + i * 62;
            boolean sel = i == settingsIndex;
            if (sel) {
                r.fillRect(Config.WIDTH / 2.0 - 210, y - 34, 420, 52, Palette.alpha(Palette.CYAN, 0.10f), 1f);
                r.fillRect(Config.WIDTH / 2.0 - 210, y - 34, 3, 52, Palette.CYAN, 0.95f);
            }
            r.text(labels[i], Config.WIDTH / 2.0 - 190, y, Fonts.plain(sel ? 24 : 22),
                    sel ? Palette.CYAN : Palette.GREY, Renderer.Align.LEFT, sel ? 1f : 0.75f);

            if (i <= 1) {
                float v = i == 0 ? ctx.save.musicVolume() : ctx.save.sfxVolume();
                drawVolumeBar(r, Config.WIDTH / 2.0 + 40, y - 10, 150, v, sel);
            } else {
                String value = switch (i) {
                    case 2 -> ctx.save.crtEnabled() ? "开" : "关";
                    case 3 -> ctx.save.showFps() ? "开" : "关";
                    case 4 -> ctx.save.difficulty().label();
                    default -> "Enter 返回";
                };
                r.text(value, Config.WIDTH / 2.0 + 190, y, Fonts.bold(22),
                        sel ? Palette.WHITE : Palette.GREY, Renderer.Align.RIGHT, sel ? 0.95f : 0.6f);
            }
        }
        r.text("↑↓ 选择 · ←→ 调整 · ESC 返回", Config.WIDTH / 2.0, Config.HEIGHT - 90,
                Fonts.plain(17), Palette.GREY, Renderer.Align.CENTER, 0.55f);
    }

    /** 音量条用矩形画，避免特殊字符在部分字体里变成方块。 */
    private void drawVolumeBar(Renderer r, double x, double y, double width, float value, boolean selected) {
        int segments = 10;
        double gap = 3;
        double segW = (width - gap * (segments - 1)) / segments;
        int filled = (int) Math.round(MathUtils.clamp(value, 0, 1) * segments);
        for (int i = 0; i < segments; i++) {
            boolean on = i < filled;
            r.fillRect(x + i * (segW + gap), y, segW, 14,
                    on ? (selected ? Palette.CYAN : Palette.CYAN_DIM) : Palette.NIGHT_SOFT,
                    on ? 0.95f : 0.75f);
        }
    }
}
