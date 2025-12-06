package com.k423.planewar.gfx;

import com.k423.planewar.core.Config;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * 渲染器。对外只暴露"虚拟画布坐标"（720x1280），窗口怎么缩放由它内部处理：
 * 等比缩放 + 上下留黑边，保证任何窗口尺寸下画面都不变形。
 */
public final class Renderer {

    public enum Align {LEFT, CENTER, RIGHT}

    private Graphics2D g;
    private final Deque<AffineTransform> stack = new ArrayDeque<>();
    private final Map<Font, FontMetrics> metricsCache = new HashMap<>();
    private final Map<Integer, BufferedImage> orbCache = new HashMap<>();
    private final Map<Integer, BufferedImage> glowCache = new HashMap<>();
    private int screenW;
    private int screenH;
    private double scale = 1;
    private double offsetX;
    private double offsetY;
    private float alpha = 1f;
    private Composite baseComposite;

    private static BufferedImage vignette;
    private static BufferedImage scanlines;

    public void begin(Graphics2D target, int width, int height) {
        screenW = Math.max(1, width);
        screenH = Math.max(1, height);
        scale = Math.min(screenW / (double) Config.WIDTH, screenH / (double) Config.HEIGHT);
        offsetX = (screenW - Config.WIDTH * scale) / 2.0;
        offsetY = (screenH - Config.HEIGHT * scale) / 2.0;

        g = (Graphics2D) target.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g.setComposite(AlphaComposite.Src);
        g.setColor(Palette.VOID);
        g.fillRect(0, 0, screenW, screenH);
        g.setComposite(AlphaComposite.SrcOver);
        baseComposite = g.getComposite();

        g.translate(offsetX, offsetY);
        g.scale(scale, scale);
        g.clipRect(0, 0, Config.WIDTH, Config.HEIGHT);
        stack.clear();
        alpha = 1f;
    }

    public void end() {
        if (g != null) {
            g.dispose();
            g = null;
        }
    }

    public Graphics2D g() {
        return g;
    }

    public double scale() {
        return scale;
    }

    public double offsetX() {
        return offsetX;
    }

    public double offsetY() {
        return offsetY;
    }

    /** 把屏幕像素坐标换算成画布坐标（鼠标输入要用）。 */
    public double toCanvasX(double screenX) {
        return (screenX - offsetX) / scale;
    }

    public double toCanvasY(double screenY) {
        return (screenY - offsetY) / scale;
    }

    public void push() {
        stack.push(g.getTransform());
    }

    public void pop() {
        if (!stack.isEmpty()) {
            g.setTransform(stack.pop());
        }
    }

    public void translate(double x, double y) {
        g.translate(x, y);
    }

    public void rotate(double radians) {
        g.rotate(radians);
    }

    public void rotateAt(double radians, double cx, double cy) {
        g.rotate(radians, cx, cy);
    }

    public void setAlpha(float a) {
        float clamped = Math.max(0f, Math.min(1f, a));
        if (Math.abs(clamped - alpha) > 0.001f) {
            alpha = clamped;
            g.setComposite(alpha >= 1f ? baseComposite
                    : AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
    }

    public void resetAlpha() {
        setAlpha(1f);
    }

    public void clipRect(double x, double y, double w, double h) {
        g.clip(new Rectangle2D.Double(x, y, w, h));
    }

    public void clearClip() {
        g.setClip(null);
        g.clipRect(0, 0, Config.WIDTH, Config.HEIGHT);
    }

    // ---------------------------------------------------------------- 图片

    public void sprite(BufferedImage img, double cx, double cy, double w, double h, float a) {
        if (img == null) {
            return;
        }
        setAlpha(a);
        g.drawImage(img, (int) Math.round(cx - w / 2), (int) Math.round(cy - h / 2),
                (int) Math.round(w), (int) Math.round(h), null);
        resetAlpha();
    }

    public void sprite(BufferedImage img, double cx, double cy, double w, double h) {
        sprite(img, cx, cy, w, h, 1f);
    }

    public void spriteRotated(BufferedImage img, double cx, double cy, double w, double h, double radians, float a) {
        if (img == null) {
            return;
        }
        push();
        g.translate(cx, cy);
        g.rotate(radians);
        setAlpha(a);
        g.drawImage(img, (int) Math.round(-w / 2), (int) Math.round(-h / 2),
                (int) Math.round(w), (int) Math.round(h), null);
        resetAlpha();
        pop();
    }

    /** 在精灵下方垫一层模糊剪影，做出霓虹辉光。 */
    public void glow(BufferedImage silhouette, double cx, double cy, double w, double h, float a) {
        if (silhouette == null) {
            return;
        }
        setAlpha(a);
        g.drawImage(silhouette, (int) Math.round(cx - w / 2), (int) Math.round(cy - h / 2),
                (int) Math.round(w), (int) Math.round(h), null);
        resetAlpha();
    }

    // ---------------------------------------------------------------- 图形

    public void fillRect(double x, double y, double w, double h, Color c, float a) {
        setAlpha(a);
        g.setColor(c);
        g.fill(new Rectangle2D.Double(x, y, w, h));
        resetAlpha();
    }

    public void fillCircle(double cx, double cy, double r, Color c, float a) {
        setAlpha(a);
        g.setColor(c);
        g.fill(new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        resetAlpha();
    }

    public void ring(double cx, double cy, double r, double thickness, Color c, float a) {
        setAlpha(a);
        g.setColor(c);
        g.setStroke(new BasicStroke((float) thickness));
        g.draw(new java.awt.geom.Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        resetAlpha();
    }

    public void line(double x1, double y1, double x2, double y2, Color c, float thickness, float a) {
        setAlpha(a);
        g.setColor(c);
        g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new java.awt.geom.Line2D.Double(x1, y1, x2, y2));
        resetAlpha();
    }

    /** 缓存的径向光斑：粒子、光晕、道具都用它，避免每帧创建渐变对象。 */
    public void orb(double cx, double cy, double radius, Color c, float a) {
        int key = c.getRGB();
        BufferedImage orb = orbCache.get(key);
        if (orb == null) {
            orb = makeOrb(c);
            orbCache.put(key, orb);
        }
        setAlpha(a);
        g.drawImage(orb, (int) Math.round(cx - radius), (int) Math.round(cy - radius),
                (int) Math.round(radius * 2), (int) Math.round(radius * 2), null);
        resetAlpha();
    }

    private static BufferedImage makeOrb(Color c) {
        int size = 64;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gg = img.createGraphics();
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RadialGradientPaint paint = new RadialGradientPaint(
                new Point2D.Float(size / 2f, size / 2f), size / 2f,
                new float[]{0f, 0.35f, 0.7f, 1f},
                new Color[]{Palette.alpha(c, 1f), Palette.alpha(c, 0.55f),
                        Palette.alpha(c, 0.16f), Palette.alpha(c, 0f)});
        gg.setPaint(paint);
        gg.fillOval(0, 0, size, size);
        gg.dispose();
        return img;
    }

    public void verticalGradient(double x, double y, double w, double h, Color top, Color bottom, float a) {
        setAlpha(a);
        g.setPaint(new java.awt.GradientPaint((float) x, (float) y, top, (float) x, (float) (y + h), bottom));
        g.fill(new Rectangle2D.Double(x, y, w, h));
        resetAlpha();
    }

    // ---------------------------------------------------------------- 文字

    private FontMetrics metrics(Font f) {
        return metricsCache.computeIfAbsent(f, g::getFontMetrics);
    }

    public int textWidth(String s, Font f) {
        return metrics(f).stringWidth(s);
    }

    public void text(String s, double x, double baselineY, Font f, Color c, float a) {
        setAlpha(a);
        g.setFont(f);
        g.setColor(c);
        g.drawString(s, (float) x, (float) baselineY);
        resetAlpha();
    }

    public void text(String s, double x, double baselineY, Font f, Color c, Align align, float a) {
        double dx = switch (align) {
            case CENTER -> -textWidth(s, f) / 2.0;
            case RIGHT -> -textWidth(s, f);
            default -> 0;
        };
        text(s, x + dx, baselineY, f, c, a);
    }

    /** 带描边/辉光的文字：先铺几层偏移的同色文字，再画主体。 */
    public void textGlow(String s, double x, double baselineY, Font f, Color color, Color glowColor,
                         Align align, float a) {
        double dx = switch (align) {
            case CENTER -> -textWidth(s, f) / 2.0;
            case RIGHT -> -textWidth(s, f);
            default -> 0;
        };
        double bx = x + dx;
        g.setFont(f);
        setAlpha(a * 0.30f);
        g.setColor(glowColor);
        for (int ox = -2; ox <= 2; ox += 2) {
            for (int oy = -2; oy <= 2; oy += 2) {
                if (ox != 0 || oy != 0) {
                    g.drawString(s, (float) (bx + ox), (float) (baselineY + oy));
                }
            }
        }
        setAlpha(a);
        g.setColor(color);
        g.drawString(s, (float) bx, (float) baselineY);
        resetAlpha();
    }

    public void textOutlined(String s, double x, double baselineY, Font f, Color color, Color outline,
                             Align align, float a) {
        double dx = switch (align) {
            case CENTER -> -textWidth(s, f) / 2.0;
            case RIGHT -> -textWidth(s, f);
            default -> 0;
        };
        g.setFont(f);
        setAlpha(a);
        g.setColor(outline);
        for (int ox = -2; ox <= 2; ox++) {
            for (int oy = -2; oy <= 2; oy++) {
                if (ox != 0 || oy != 0) {
                    g.drawString(s, (float) (x + dx + ox), (float) (baselineY + oy));
                }
            }
        }
        g.setColor(color);
        g.drawString(s, (float) (x + dx), (float) baselineY);
        resetAlpha();
    }

    // ---------------------------------------------------------------- 后期

    /** 暗角，压住画面四角，让视线集中在中间。 */
    public void vignette(float strength) {
        if (vignette == null) {
            vignette = makeVignette();
        }
        setAlpha(strength);
        g.drawImage(vignette, 0, 0, Config.WIDTH, Config.HEIGHT, null);
        resetAlpha();
    }

    private static BufferedImage makeVignette() {
        BufferedImage img = new BufferedImage(Config.WIDTH, Config.HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gg = img.createGraphics();
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float r = (float) Math.hypot(Config.WIDTH, Config.HEIGHT) * 0.55f;
        RadialGradientPaint paint = new RadialGradientPaint(
                new Point2D.Float(Config.WIDTH / 2f, Config.HEIGHT * 0.45f), r,
                new float[]{0f, 0.62f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 40), new Color(0, 0, 0, 190)});
        gg.setPaint(paint);
        gg.fillRect(0, 0, Config.WIDTH, Config.HEIGHT);
        gg.dispose();
        return img;
    }

    /** CRT 扫描线，可通过设置开关。 */
    public void scanlines(float strength) {
        if (scanlines == null) {
            scanlines = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gg = scanlines.createGraphics();
            gg.setColor(new Color(0, 0, 0, 70));
            gg.fillRect(0, 0, 4, 2);
            gg.dispose();
        }
        TexturePaint paint = new TexturePaint(scanlines, new Rectangle2D.Double(0, 0, 4, 4));
        setAlpha(strength);
        g.setPaint(paint);
        g.fillRect(0, 0, Config.WIDTH, Config.HEIGHT);
        resetAlpha();
    }

    public void flash(Color c, float a) {
        fillRect(0, 0, Config.WIDTH, Config.HEIGHT, c, a);
    }

    public void flash(Color c) {
        fillRect(0, 0, Config.WIDTH, Config.HEIGHT, c, 1f);
    }

    public int screenWidth() {
        return screenW;
    }

    public int screenHeight() {
        return screenH;
    }

    /** 取一张按颜色缓存的柔光贴图（给需要自定义光斑尺寸的地方用）。 */
    public BufferedImage glowSprite(Color c) {
        return glowCache.computeIfAbsent(c.getRGB(), k -> makeOrb(c));
    }
}
