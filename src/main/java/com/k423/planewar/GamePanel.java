package com.k423.planewar;

import com.k423.planewar.core.Config;
import com.k423.planewar.core.GameContext;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * 游戏面板。
 * 更新和绘制都在一条独立线程里按固定步长跑，绘制结果先画进离屏图，再交给 EDT 贴图，
 * 这样既不会阻塞 UI 线程，也不会有并发改集合的问题。
 */
public final class GamePanel extends JPanel implements Runnable {

    private static final long FRAME_NANOS = 16_666_666L;

    private final GameContext ctx;
    private final Renderer renderer = new Renderer();
    private final Object frameLock = new Object();

    private BufferedImage canvas;
    private Thread thread;
    private volatile boolean running;

    private volatile double scale = 1;
    private volatile double offsetX;
    private volatile double offsetY;

    public GamePanel(GameContext ctx) {
        this.ctx = ctx;
        setBackground(Palette.VOID);
        setOpaque(true);
        setFocusable(true);
        setPreferredSize(new Dimension(Config.WIDTH, Config.HEIGHT));
        installListeners();
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        thread = new Thread(this, "plane-war-loop");
        thread.setDaemon(false);
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            thread = null;
        }
    }

    @Override
    public void run() {
        double accumulator = 0;
        long last = System.nanoTime();
        double smoothFps = 60;

        while (running) {
            long start = System.nanoTime();
            double frame = (start - last) / 1e9;
            last = start;
            if (frame > 0.25) {
                frame = 0.25;
            }
            accumulator += frame;

            int steps = 0;
            while (accumulator >= Config.FIXED_DT && steps < Config.MAX_CATCH_UP) {
                try {
                    ctx.scenes.update(ctx, Config.FIXED_DT);
                } catch (RuntimeException e) {
                    System.err.println("[loop] 更新异常: " + e);
                    e.printStackTrace();
                }
                ctx.input.clearEdges();
                accumulator -= Config.FIXED_DT;
                steps++;
                if (ctx.quitRequested) {
                    break;
                }
            }
            if (steps == Config.MAX_CATCH_UP) {
                accumulator = 0;
            }

            smoothFps = smoothFps * 0.9 + (1.0 / Math.max(frame, 1e-4)) * 0.1;
            ctx.fps = smoothFps;

            try {
                renderFrame();
            } catch (RuntimeException e) {
                System.err.println("[loop] 绘制异常: " + e);
                e.printStackTrace();
            }

            if (ctx.quitRequested) {
                break;
            }

            long spent = System.nanoTime() - start;
            long sleepNanos = FRAME_NANOS - spent;
            if (sleepNanos > 1_000_000L) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (ctx.quitRequested) {
            ctx.save.save();
            ctx.audio.close();
            System.exit(0);
        }
    }

    private void renderFrame() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        synchronized (frameLock) {
            if (canvas == null || canvas.getWidth() != w || canvas.getHeight() != h) {
                canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g = canvas.createGraphics();
            try {
                renderer.begin(g, w, h);
                scale = renderer.scale();
                offsetX = renderer.offsetX();
                offsetY = renderer.offsetY();
                ctx.scenes.render(ctx, renderer);
                renderer.end();
            } finally {
                g.dispose();
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized (frameLock) {
            if (canvas != null) {
                g.drawImage(canvas, 0, 0, null);
            }
        }
    }

    private void installListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                ctx.input.keyDown(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                ctx.input.keyUp(e.getKeyCode());
            }
        });
        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateMouse(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateMouse(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                updateMouse(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    ctx.input.pressMouse();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    ctx.input.releaseMouse();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                ctx.input.clearAll();
            }
        });
    }

    private void updateMouse(MouseEvent e) {
        double s = scale <= 0 ? 1 : scale;
        ctx.input.mouseMoved((e.getX() - offsetX) / s, (e.getY() - offsetY) / s);
    }
}
