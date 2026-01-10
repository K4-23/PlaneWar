package com.k423.planewar.entity;

import com.k423.planewar.core.Config;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.gfx.Fonts;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

import java.awt.Color;
import java.awt.Font;

/** 掉落道具的类型。 */
public enum PowerUpType {

    WEAPON("火力提升", "W", Palette.CYAN),
    SHIELD("能量护盾", "S", Palette.ICE),
    BOMB("清屏炸弹", "B", Palette.AMBER),
    HEAL("修复机体", "+", Palette.LIME),
    SCORE("双倍得分", "x2", Palette.MAGENTA);

    private final String label;
    private final String letter;
    private final Color color;

    PowerUpType(String label, String letter, Color color) {
        this.label = label;
        this.letter = letter;
        this.color = color;
    }

    public String label() {
        return label;
    }

    public String letter() {
        return letter;
    }

    public Color color() {
        return color;
    }
}
