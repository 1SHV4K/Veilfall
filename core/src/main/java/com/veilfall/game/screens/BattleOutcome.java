package com.veilfall.game.screens;

public record BattleOutcome(
        boolean victory,
        String characterName,
        String arenaName,
        int wavesCompleted,
        int enemiesDefeated,
        int bossesDefeated,
        float durationSeconds) {
}
