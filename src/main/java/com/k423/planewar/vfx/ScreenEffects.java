package com.k423.planewar.vfx;

import com.k423.planewar.core.Config;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

import java.awt.Color;

/**
 * 屏幕级效果：震屏、闪白、命中暂停、低血量红边、暗角。
 * 所有效果都是"往里加"，不会互相覆盖。
 */
public final class ScreenEffects {

    private double shake;
    private double shakeX;
    private double shakeY;
    private double flash;
    private Color flashColor = Color.WHITE;
    private double hitstop;
    private double timeScale = 1;
    private double lowHp;
    private double time;

    public void update(double dt) {
        time += dt;
        if (shake > 0) {
            shake = Math.max(0, shake - Config.SHAKE_DECAY * shake * dt - 2.5 * dt);
            double a = time * 47;
            shakeX = Math.cos(a) * shake;
            shakeY = Math.sin(a * 1.7) * shake;
        } else {
            shakeX = 0;
            shakeY = 0;
        }
        flash = Math.max(0, flash - dt * 4.0);
        hitstop = Math.max(0, hitstop - dt);
        timeScale = hitstop > 0 ? 0.25 : 1;
    }

    public void shake(double amount) {
        shake = Math.max(shake, amount);
    }

    public void flash(Color color, double amount) {
        flashColor = color;
        flash = Math.min(1.2, Math.max(flash, amount));
    }

    /** 命中瞬间的短暂顿帧，打击感来源。 */
    public void hitstop(double seconds) {
        hitstop = Math.max(hitstop, seconds);
    }

    public void setLowHp(double ratio) {
        lowHp = ratio;
    }

    public double timeScale() {
        return timeScale;
    }

    public double shakeX() {
        return shakeX;
    }

    public double shakeY() {
        return shakeY;
    }

    public void render(Renderer r) {
        if (lowHp > 0.01) {
            double pulse = 0.5 + 0.5 * Math.sin(time * 6);
            r.flash(Palette.alpha(Palette.RED, 0.10f * (float) lowHp * (float) (0.5 + pulse * 0.7)));
        }
        if (flash > 0.01) {
            r.flash(Palette.alpha(flashColor, (float) Math.min(1, flash)));
        }
        r.vignette(0.42f);
    }

    public void reset() {
        shake = 0;
        flash = 0;
        hitstop = 0;
        lowHp = 0;
    }

    /** 供调试/测试用。 */
    public double currentShake() {
        return shake;
    }
}
