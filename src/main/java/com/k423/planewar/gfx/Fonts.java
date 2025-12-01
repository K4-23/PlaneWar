package com.k423.planewar.gfx;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import java.util.Map;

/**
 * 字体。优先用系统的中文字体，找不到就退回逻辑字体，保证任何机器上中文都能显示。
 * 字体对象创建有开销，这里做缓存。
 */
public final class Fonts {

    private static final String FAMILY;
    private static final Map<Integer, Font> BOLD_CACHE = new HashMap<>();
    private static final Map<Integer, Font> PLAIN_CACHE = new HashMap<>();

    static {
        String[] wanted = {"Microsoft YaHei UI", "Microsoft YaHei", "微软雅黑", "PingFang SC", "Noto Sans CJK SC", "SansSerif"};
        String found = "SansSerif";
        try {
            String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            outer:
            for (String w : wanted) {
                for (String a : available) {
                    if (a.equalsIgnoreCase(w)) {
                        found = a;
                        break outer;
                    }
                }
            }
        } catch (Throwable ignored) {
            // 无图形环境时退回逻辑字体
        }
        FAMILY = found;
    }

    private Fonts() {
    }

    public static String family() {
        return FAMILY;
    }

    public static Font bold(int size) {
        return BOLD_CACHE.computeIfAbsent(size, s -> new Font(FAMILY, Font.BOLD, s));
    }

    public static Font plain(int size) {
        return PLAIN_CACHE.computeIfAbsent(size, s -> new Font(FAMILY, Font.PLAIN, s));
    }
}
