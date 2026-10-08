package com.veilfall.game.data;

public final class MatchData {
    private final int id;
    private final String result;
    private final int arenaId;
    private final int characterId;
    private final int wavesReached;
    private final int enemiesDefeated;
    private final int bossesDefeated;
    private final double durationSeconds;
    private final String playedAt;

    public MatchData(int id, String result, int arenaId, int characterId, int wavesReached,
                     int enemiesDefeated, int bossesDefeated, double durationSeconds, String playedAt) {
        this.id = id;
        this.result = result;
        this.arenaId = arenaId;
        this.characterId = characterId;
        this.wavesReached = wavesReached;
        this.enemiesDefeated = enemiesDefeated;
        this.bossesDefeated = bossesDefeated;
        this.durationSeconds = durationSeconds;
        this.playedAt = playedAt;
    }

    public int getId() { return id; }
    public String getResult() { return result; }
    public int getArenaId() { return arenaId; }
    public int getCharacterId() { return characterId; }
    public int getWavesReached() { return wavesReached; }
    public int getEnemiesDefeated() { return enemiesDefeated; }
    public int getBossesDefeated() { return bossesDefeated; }
    public double getDurationSeconds() { return durationSeconds; }
    public String getPlayedAt() { return playedAt; }
}
