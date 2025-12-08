package com.k423.planewar.core;

import com.k423.planewar.audio.AudioEngine;
import com.k423.planewar.data.SaveData;

/** 全局服务容器：随机数、输入、存档、音频、场景栈都挂在这里，场景之间靠它通信。 */
public final class GameContext {

    public final Rng rng;
    public final Input input;
    public final SaveData save;
    public final AudioEngine audio;
    public final SceneManager scenes = new SceneManager();

    public volatile boolean quitRequested;
    /** 最近一次渲染的帧率，只用于 HUD 显示。 */
    public volatile double fps = 60;

    public GameContext(Input input, SaveData save, AudioEngine audio) {
        this(new Rng(), input, save, audio);
    }

    public GameContext(Rng rng, Input input, SaveData save, AudioEngine audio) {
        this.rng = rng;
        this.input = input;
        this.save = save;
        this.audio = audio;
    }

    public void requestQuit() {
        quitRequested = true;
    }

    public void applyVolumes() {
        audio.setMusicVolume(save.musicVolume());
        audio.setSfxVolume(save.sfxVolume());
    }
}
