package com.k423.planewar.core;

import com.k423.planewar.gfx.Renderer;

/** 场景栈的极简实现：只保留当前场景，切换在下一帧生效，避免在更新中途换掉自己。 */
public final class SceneManager {

    private Scene current;
    private Scene pending;

    public void set(Scene scene) {
        pending = scene;
    }

    public Scene current() {
        return current;
    }

    public void update(GameContext ctx, double dt) {
        if (pending != null) {
            Scene next = pending;
            pending = null;
            if (current != null) {
                current.exit(ctx);
            }
            current = next;
            current.enter(ctx);
        }
        if (current != null) {
            current.update(ctx, dt);
        }
    }

    public void render(GameContext ctx, Renderer r) {
        if (current != null) {
            current.render(ctx, r);
        }
    }
}
