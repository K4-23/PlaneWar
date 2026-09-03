package com.k423.planewar.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathUtilsTest {

    @Test
    void circlesHitDetectsOverlapAndTouching() {
        assertTrue(MathUtils.circlesHit(0, 0, 10, 15, 0, 10));
        assertTrue(MathUtils.circlesHit(0, 0, 10, 20, 0, 10), "刚好相切也算碰撞");
        assertFalse(MathUtils.circlesHit(0, 0, 10, 20.001, 0, 10));
        assertFalse(MathUtils.circlesHit(0, 0, 1, 100, 100, 1));
    }

    @Test
    void clampAndLerpBehave() {
        assertEquals(5, MathUtils.clamp(9, 0, 5), 1e-9);
        assertEquals(0, MathUtils.clamp(-3, 0, 5), 1e-9);
        assertEquals(2.5, MathUtils.lerp(0, 5, 0.5), 1e-9);
        assertEquals(0.5, MathUtils.invLerp(0, 10, 5), 1e-9);
        assertEquals(1, MathUtils.invLerp(0, 10, 99), 1e-9, "超出上界要裁到 1");
    }

    @Test
    void easingStartsAtZeroEndsAtOne() {
        for (double t : new double[]{0, 0.25, 0.5, 0.75, 1}) {
            assertEquals(t == 0 ? 0 : 1, MathUtils.smoothstep(t) == 0 ? 0 : 1, 1e-9);
        }
        assertEquals(0, MathUtils.easeOutCubic(0), 1e-9);
        assertEquals(1, MathUtils.easeOutCubic(1), 1e-9);
        assertTrue(MathUtils.easeOutCubic(0.5) > 0.5);
    }

    @Test
    void wrapAngleKeepsRange() {
        assertEquals(-Math.PI, MathUtils.wrapAngle(Math.PI), 1e-9);
        assertEquals(0, MathUtils.wrapAngle(MathUtils.TAU), 1e-9);
        double a = MathUtils.wrapAngle(Math.PI * 5);
        assertTrue(a >= -Math.PI && a < Math.PI);
    }
}
