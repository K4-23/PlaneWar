package com.k423.planewar.core;

import com.k423.planewar.gfx.Renderer;

/** 场景：标题、战斗、结算、设置…… 每个场景自己管更新和绘制。 */
public interface Scene {

    default void enter(GameContext ctx) {
    }

    default void exit(GameContext ctx) {
    }

    void update(GameContext ctx, double dt);

    void render(GameContext ctx, Renderer r);
}
