package com.k423.planewar;

import com.k423.planewar.core.Config;
import com.k423.planewar.core.GameContext;
import com.k423.planewar.gfx.Palette;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * 主窗口。默认按屏幕高度取一个舒服的尺寸，允许自由缩放（画面等比，不会变形）。
 */
public final class GameWindow extends JFrame {

    private final GameContext ctx;
    private final GamePanel panel;

    public GameWindow(GameContext ctx) {
        super(Config.TITLE + "  v" + Config.VERSION);
        this.ctx = ctx;
        this.panel = new GamePanel(ctx);
        setContentPane(panel);
        setBackground(Palette.VOID);
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int height = (int) Math.round(Math.min(screen.height * 0.94, Config.HEIGHT * 1.1));
        int width = (int) Math.round(height * Config.WIDTH / (double) Config.HEIGHT);
        setSize(width, height);
        setMinimumSize(new Dimension(360, 640));
        setLocationRelativeTo(null);
        setIconImage(icon());
    }

    private BufferedImage icon() {
        return com.k423.planewar.gfx.Assets.PLAYER;
    }

    public void startLoop() {
        panel.requestFocusInWindow();
        panel.start();
    }

    public void shutdown() {
        panel.stop();
        ctx.audio.close();
        ctx.save.save();
        dispose();
        System.exit(0);
    }
}
