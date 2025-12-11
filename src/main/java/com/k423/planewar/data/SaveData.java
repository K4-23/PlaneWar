package com.k423.planewar.data;

import com.k423.planewar.core.Config;
import com.k423.planewar.core.Difficulty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 本地存档：最高分、最高波次、累计击杀、音量、画面开关、难度。
 * 存在用户目录下，删掉也不影响游戏启动。
 */
public final class SaveData {

    private final Path file;
    private final Properties props = new Properties();

    private int highScore;
    private int bestWave;
    private int bestCombo;
    private int totalKills;
    private int totalPlays;
    private long totalPlaySeconds;
    private float musicVolume = 0.55f;
    private float sfxVolume = 0.95f;
    private boolean crtEnabled = true;
    private boolean showFps;
    private Difficulty difficulty = Difficulty.NORMAL;

    public SaveData() {
        this(Paths.get(System.getProperty("user.home", "."), Config.SAVE_DIR, Config.SAVE_FILE));
    }

    public SaveData(Path file) {
        this.file = file;
        load();
    }

    public void load() {
        if (file == null || !Files.isRegularFile(file)) {
            return;
        }
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("[save] 读取存档失败: " + e.getMessage());
            return;
        }
        highScore = intOf("highScore", 0);
        bestWave = intOf("bestWave", 0);
        bestCombo = intOf("bestCombo", 0);
        totalKills = intOf("totalKills", 0);
        totalPlays = intOf("totalPlays", 0);
        totalPlaySeconds = intOf("totalPlaySeconds", 0);
        musicVolume = Math.max(0f, Math.min(1f, floatOf("musicVolume", musicVolume)));
        sfxVolume = Math.max(0f, Math.min(1f, floatOf("sfxVolume", sfxVolume)));
        crtEnabled = Boolean.parseBoolean(props.getProperty("crtEnabled", "true"));
        showFps = Boolean.parseBoolean(props.getProperty("showFps", "false"));
        difficulty = Difficulty.fromName(props.getProperty("difficulty", "NORMAL"));
    }

    public void save() {
        props.setProperty("highScore", Integer.toString(highScore));
        props.setProperty("bestWave", Integer.toString(bestWave));
        props.setProperty("bestCombo", Integer.toString(bestCombo));
        props.setProperty("totalKills", Integer.toString(totalKills));
        props.setProperty("totalPlays", Integer.toString(totalPlays));
        props.setProperty("totalPlaySeconds", Long.toString(totalPlaySeconds));
        props.setProperty("musicVolume", Float.toString(musicVolume));
        props.setProperty("sfxVolume", Float.toString(sfxVolume));
        props.setProperty("crtEnabled", Boolean.toString(crtEnabled));
        props.setProperty("showFps", Boolean.toString(showFps));
        props.setProperty("difficulty", difficulty.name());
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "Plane War save data");
            }
        } catch (IOException e) {
            System.err.println("[save] 写入存档失败: " + e.getMessage());
        }
    }

    private int intOf(String key, int fallback) {
        try {
            return Integer.parseInt(props.getProperty(key, Integer.toString(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private float floatOf(String key, float fallback) {
        try {
            return Float.parseFloat(props.getProperty(key, Float.toString(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // ---------------- getters / setters ----------------

    public int highScore() {
        return highScore;
    }

    /** 记录一局结果，返回是否刷新了最高分。 */
    public boolean submitResult(int score, int wave, int combo, int kills, double seconds) {
        boolean record = score > highScore;
        highScore = Math.max(highScore, score);
        bestWave = Math.max(bestWave, wave);
        bestCombo = Math.max(bestCombo, combo);
        totalKills += kills;
        totalPlays++;
        totalPlaySeconds += Math.max(0, Math.round(seconds));
        save();
        return record;
    }

    public int bestWave() {
        return bestWave;
    }

    public int bestCombo() {
        return bestCombo;
    }

    public int totalKills() {
        return totalKills;
    }

    public int totalPlays() {
        return totalPlays;
    }

    public long totalPlaySeconds() {
        return totalPlaySeconds;
    }

    public float musicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float v) {
        musicVolume = Math.max(0f, Math.min(1f, v));
    }

    public float sfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float v) {
        sfxVolume = Math.max(0f, Math.min(1f, v));
    }

    public boolean crtEnabled() {
        return crtEnabled;
    }

    public void setCrtEnabled(boolean v) {
        crtEnabled = v;
    }

    public boolean showFps() {
        return showFps;
    }

    public void setShowFps(boolean v) {
        showFps = v;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty d) {
        difficulty = d;
    }
}
