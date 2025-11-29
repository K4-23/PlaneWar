package com.k423.planewar.core;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * 输入状态。GUI 线程写入，游戏循环线程读取，所有公开方法都做了同步，
 * 每帧开头调用一次 {@link #beginFrame()} 把"本帧新按下/新抬起"的事件快照下来。
 */
public final class Input {

    private final Set<Integer> down = new HashSet<>();
    private final Set<Integer> pressed = new HashSet<>();
    private final Set<Integer> released = new HashSet<>();

    private double mouseX = Config.WIDTH / 2.0;
    private double mouseY = Config.HEIGHT * 0.75;
    private boolean mouseDown;
    private boolean mousePressed;
    private boolean mouseReleased;

    // ---- GUI 线程 ----
    public synchronized void keyDown(int code) {
        if (down.add(code)) {
            pressed.add(code);
        }
    }

    public synchronized void keyUp(int code) {
        if (down.remove(code)) {
            released.add(code);
        }
    }

    public synchronized void mouseMoved(double canvasX, double canvasY) {
        mouseX = canvasX;
        mouseY = canvasY;
    }

    public synchronized void pressMouse() {
        mouseDown = true;
        mousePressed = true;
    }

    public synchronized void releaseMouse() {
        mouseDown = false;
        mouseReleased = true;
    }

    public synchronized void clearEdges() {
        pressed.clear();
        released.clear();
        mousePressed = false;
        mouseReleased = false;
    }

    /** 清空全部状态（切换窗口焦点、进入新场景时调用）。 */
    public synchronized void clearAll() {
        down.clear();
        pressed.clear();
        released.clear();
        mouseDown = false;
        mousePressed = false;
        mouseReleased = false;
    }

    /** 每帧开头调用：把边沿事件交给本帧读取。 */
    public synchronized void beginFrame() {
        // 边沿集合本来就是"自上一帧以来"累积的，这里无需额外处理，
        // 但保留方法名让调用点语义清晰。
    }

    // ---- 游戏线程 ----
    public synchronized boolean isDown(int code) {
        return down.contains(code);
    }

    public synchronized boolean justPressed(int code) {
        return pressed.contains(code);
    }

    public synchronized boolean justReleased(int code) {
        return released.contains(code);
    }

    public synchronized double mouseX() {
        return mouseX;
    }

    public synchronized double mouseY() {
        return mouseY;
    }

    public synchronized boolean mouseDown() {
        return mouseDown;
    }

    public synchronized boolean mouseJustPressed() {
        return mousePressed;
    }

    public synchronized boolean mouseJustReleased() {
        return mouseReleased;
    }

    // ---- 语义按键 ----
    public boolean up() {
        return isDown(KeyEvent.VK_W) || isDown(KeyEvent.VK_UP);
    }

    public boolean down() {
        return isDown(KeyEvent.VK_S) || isDown(KeyEvent.VK_DOWN);
    }

    public boolean left() {
        return isDown(KeyEvent.VK_A) || isDown(KeyEvent.VK_LEFT);
    }

    public boolean right() {
        return isDown(KeyEvent.VK_D) || isDown(KeyEvent.VK_RIGHT);
    }

    public boolean focus() {
        return isDown(KeyEvent.VK_SHIFT);
    }

    public boolean bomb() {
        boolean edge = justPressed(KeyEvent.VK_X) || justPressed(KeyEvent.VK_K);
        clearEdge(KeyEvent.VK_X);
        clearEdge(KeyEvent.VK_K);
        return edge;
    }

    public boolean pause() {
        return justPressed(KeyEvent.VK_ESCAPE) || justPressed(KeyEvent.VK_P);
    }

    public boolean confirm() {
        return justPressed(KeyEvent.VK_ENTER) || justPressed(KeyEvent.VK_SPACE);
    }

    public boolean back() {
        return justPressed(KeyEvent.VK_ESCAPE) || justPressed(KeyEvent.VK_BACK_SPACE);
    }

    /** 消费掉某个按键的"刚按下"状态，避免同一帧被多个场景重复处理。 */
    public synchronized void clearEdge(int code) {
        pressed.remove(code);
    }
}
