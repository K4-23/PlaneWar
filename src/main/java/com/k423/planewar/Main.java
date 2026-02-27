package com.k423.planewar;

import com.k423.planewar.audio.AudioEngine;
import com.k423.planewar.core.Config;
import com.k423.planewar.core.GameContext;
import com.k423.planewar.core.Input;
import com.k423.planewar.data.SaveData;
import com.k423.planewar.gfx.Assets;
import com.k423.planewar.scene.GameScene;
import com.k423.planewar.scene.MenuScene;

import javax.swing.SwingUtilities;
import java.util.Arrays;
import java.util.List;

/**
 * 入口。
 * <pre>
 *   java -jar plane-war-2.0.0.jar              正常启动
 *   java -jar plane-war-2.0.0.jar --headless   无窗口跑 60 秒做冒烟测试
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        List<String> argv = Arrays.asList(args);
        Assets.load();

        SaveData save = new SaveData();
        AudioEngine audio = new AudioEngine();
        Input input = new Input();
        GameContext ctx = new GameContext(input, save, audio);
        ctx.applyVolumes();

        if (argv.contains("--headless")) {
            runHeadless(ctx, argv);
            return;
        }

        ctx.scenes.set(new MenuScene(ctx));
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow(ctx);
            window.setVisible(true);
            window.startLoop();
        });
    }

    /** 无窗口冒烟测试：直接开一局，按固定步长模拟指定秒数。 */
    private static void runHeadless(GameContext ctx, List<String> argv) {
        double seconds = 60;
        int idx = argv.indexOf("--seconds");
        if (idx >= 0 && idx + 1 < argv.size()) {
            try {
                seconds = Double.parseDouble(argv.get(idx + 1));
            } catch (NumberFormatException ignored) {
                // 保持默认值
            }
        }
        ctx.scenes.set(new GameScene(ctx));
        int steps = (int) Math.round(seconds / Config.FIXED_DT);
        for (int i = 0; i < steps; i++) {
            ctx.scenes.update(ctx, Config.FIXED_DT);
            ctx.input.clearEdges();
            if (ctx.scenes.current() instanceof GameScene || ctx.scenes.current() instanceof MenuScene) {
                // 场景可能在模拟过程中切换（通关/失败），这里不断言，只是让流程继续跑
            }
        }
        System.out.println("HEADLESS OK  steps=" + steps + "  fps=" + Math.round(ctx.fps));
        ctx.audio.close();
        ctx.save.save();
    }
}
