package com.k423.planewar.gfx;

import com.k423.planewar.core.Config;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 素材加载。整套使用 Kenney 的 Pixel Shmup（CC0）：
 * 32x32 的飞机、16x16 的地形与 UI 瓦片，全部从 classpath 读取。
 */
public final class Assets {

    private Assets() {
    }

    // 飞机
    public static BufferedImage PLAYER;
    public static BufferedImage ENEMY_SCOUT;
    public static BufferedImage ENEMY_FIGHTER;
    public static BufferedImage ENEMY_TANK;
    public static BufferedImage ENEMY_ELITE;
    public static BufferedImage ENEMY_DRONE;
    public static BufferedImage BOSS;

    // 背景
    public static BufferedImage BG;

    // 爆炸帧（星芒图标）
    public static BufferedImage[] EXPLOSION = new BufferedImage[0];

    // 像素数字 0-9
    public static BufferedImage[] DIGITS = new BufferedImage[0];

    // UI 图标
    public static BufferedImage ICON_LIFE;
    public static BufferedImage ICON_POWER;
    public static BufferedImage ICON_SHIELD;
    public static BufferedImage ICON_BOMB;
    public static BufferedImage ICON_SCORE;

    private static final Map<BufferedImage, BufferedImage> WHITE_CACHE = new HashMap<>();
    private static final Map<BufferedImage, BufferedImage> SHADOW_CACHE = new HashMap<>();
    private static boolean loaded;

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        PLAYER = ship(0);
        ENEMY_SCOUT = ship(5);
        ENEMY_FIGHTER = ship(9);
        ENEMY_TANK = ship(1);
        ENEMY_ELITE = ship(12);
        ENEMY_DRONE = ship(18);
        BOSS = ship(13);
        BG = read("pixel/bg_islands.png");

        EXPLOSION = new BufferedImage[]{tile(4), tile(5), tile(6), tile(7), tile(8)};
        DIGITS = new BufferedImage[10];
        int[] digitTiles = {19, 20, 21, 22, 23, 31, 32, 33, 34, 35};
        for (int i = 0; i < 10; i++) {
            DIGITS[i] = tile(digitTiles[i]);
        }
        ICON_LIFE = tile(24);
        ICON_POWER = tile(25);
        ICON_SHIELD = tile(26);
        ICON_BOMB = tile(17);
        ICON_SCORE = tile(4);
        loaded = true;
    }

    /** 受击闪白：保留 alpha，把颜色整体换成白色，缓存复用。 */
    public static BufferedImage white(BufferedImage src) {
        return WHITE_CACHE.computeIfAbsent(src, s -> tint(s, 0xFFFFFF));
    }

    /** 投影：同一张剪影压成黑色，垫在精灵下面提升可读性。 */
    public static BufferedImage shadow(BufferedImage src) {
        return SHADOW_CACHE.computeIfAbsent(src, s -> tint(s, 0x101820));
    }

    private static BufferedImage tint(BufferedImage src, int rgb) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = (src.getRGB(x, y) >>> 24) & 0xFF;
                out.setRGB(x, y, (a << 24) | rgb);
            }
        }
        return out;
    }

    private static BufferedImage ship(int index) {
        return read(String.format("pixel/ships/ship_%04d.png", index));
    }

    private static BufferedImage tile(int index) {
        return read(String.format("pixel/tiles/tile_%04d.png", index));
    }

    private static BufferedImage read(String path) {
        try (InputStream in = Assets.class.getResourceAsStream("/" + path)) {
            if (in == null) {
                System.err.println("[assets] 缺少素材: /" + path);
                return placeholder();
            }
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                System.err.println("[assets] 无法解码: /" + path);
                return placeholder();
            }
            return img;
        } catch (IOException e) {
            System.err.println("[assets] 读取失败: /" + path + " -> " + e.getMessage());
            return placeholder();
        }
    }

    private static BufferedImage placeholder() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                boolean border = x < 2 || y < 2 || x > 13 || y > 13;
                img.setRGB(x, y, border ? 0xFFFF00FF : 0x60FF00FF);
            }
        }
        return img;
    }

    /** 像素数字的宽度（含 2 像素间距）。 */
    public static int digitWidth(int scale) {
        return 16 * scale;
    }

    /** 画一串数字，返回绘制后的右边界。 */
    public static double drawNumber(Renderer r, long value, double x, double baselineY, int scale, Color tint) {
        String s = Long.toString(value);
        double cursor = x;
        for (int i = 0; i < s.length(); i++) {
            int d = s.charAt(i) - '0';
            if (d >= 0 && d <= 9) {
                r.sprite(DIGITS[d], cursor + 8 * scale, baselineY - 8 * scale, 16 * scale, 16 * scale, 1f);
            }
            cursor += 14 * scale;
        }
        return cursor;
    }

    public static int canvasWidth() {
        return Config.WIDTH;
    }
}
