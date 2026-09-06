package com.k423.planewar.gfx;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 素材必须能从 classpath 读出来（打包成 jar 之后同样成立）。 */
class AssetsTest {

    @BeforeAll
    static void load() {
        Assets.load();
    }

    @Test
    void coreSpritesExist() {
        assertNotNull(Assets.PLAYER);
        assertNotNull(Assets.ENEMY_SCOUT);
        assertNotNull(Assets.ENEMY_FIGHTER);
        assertNotNull(Assets.ENEMY_TANK);
        assertNotNull(Assets.ENEMY_ELITE);
        assertNotNull(Assets.ENEMY_DRONE);
        assertNotNull(Assets.BOSS);
        assertNotNull(Assets.BG);
    }

    @Test
    void pixelSpritesUseExpectedSize() {
        assertTrue(Assets.PLAYER.getWidth() == 32 && Assets.PLAYER.getHeight() == 32,
                "像素飞机素材是 32x32");
        assertTrue(Assets.EXPLOSION.length == 5, "爆炸用 5 张星芒帧");
        assertTrue(Assets.DIGITS.length == 10, "数字 0-9");
        assertTrue(Assets.DIGITS[7].getWidth() == 16, "数字瓦片是 16x16");
    }

    @Test
    void tintedVariantsAreCached() {
        var a = Assets.white(Assets.PLAYER);
        var b = Assets.white(Assets.PLAYER);
        var s = Assets.shadow(Assets.PLAYER);
        assertNotNull(a);
        assertTrue(a == b, "同一张图应复用缓存");
        assertTrue(a != s, "闪白和投影不能共用");
    }
}
