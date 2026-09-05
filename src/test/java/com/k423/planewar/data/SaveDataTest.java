package com.k423.planewar.data;

import com.k423.planewar.core.Difficulty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveDataTest {

    @Test
    void submitResultKeepsBestValues(@TempDir Path dir) {
        SaveData save = new SaveData(dir.resolve("save.properties"));

        assertTrue(save.submitResult(1200, 2, 15, 42, 95));
        assertEquals(1200, save.highScore());
        assertEquals(2, save.bestWave());
        assertEquals(42, save.totalKills());
        assertEquals(1, save.totalPlays());

        assertFalse(save.submitResult(900, 1, 30, 10, 30), "分数没超过最高分不算破纪录");
        assertEquals(1200, save.highScore(), "最高分不能被更低的分数覆盖");
        assertEquals(2, save.bestWave(), "关卡数取历史最高");
        assertEquals(30, save.bestCombo());
        assertEquals(52, save.totalKills());
        assertEquals(2, save.totalPlays());
    }

    @Test
    void settingsRoundTripThroughDisk(@TempDir Path dir) {
        Path file = dir.resolve("nested/save.properties");
        SaveData a = new SaveData(file);
        a.setMusicVolume(0.3f);
        a.setSfxVolume(0.45f);
        a.setCrtEnabled(false);
        a.setShowFps(true);
        a.setDifficulty(Difficulty.HARD);
        a.submitResult(777, 1, 9, 12, 60);

        SaveData b = new SaveData(file);
        assertEquals(0.3f, b.musicVolume(), 1e-6);
        assertEquals(0.45f, b.sfxVolume(), 1e-6);
        assertFalse(b.crtEnabled());
        assertTrue(b.showFps());
        assertEquals(Difficulty.HARD, b.difficulty());
        assertEquals(777, b.highScore());
    }

    @Test
    void volumeIsClamped(@TempDir Path dir) {
        SaveData save = new SaveData(dir.resolve("s.properties"));
        save.setMusicVolume(3f);
        save.setSfxVolume(-2f);
        assertEquals(1f, save.musicVolume(), 1e-6);
        assertEquals(0f, save.sfxVolume(), 1e-6);
    }
}
