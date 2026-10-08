package com.veilfall.game.database.repository;

import com.veilfall.game.data.MatchData;
import com.veilfall.game.database.DatabaseManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MatchRepository {
    private final DatabaseManager databaseManager;

    public MatchRepository(DatabaseManager databaseManager) {
        this.databaseManager = Objects.requireNonNull(databaseManager);
    }

    public int insertMatch(MatchData match) throws SQLException {
        Objects.requireNonNull(match);
        return databaseManager.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO matches
                        (result, arena_id, character_id, waves_reached, enemies_defeated,
                         bosses_defeated, duration_seconds)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """)) {
                statement.setString(1, match.getResult());
                statement.setInt(2, match.getArenaId());
                statement.setInt(3, match.getCharacterId());
                statement.setInt(4, match.getWavesReached());
                statement.setInt(5, match.getEnemiesDefeated());
                statement.setInt(6, match.getBossesDefeated());
                statement.setDouble(7, match.getDurationSeconds());
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("SELECT last_insert_rowid()");
                 ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("SQLite did not return the inserted match ID");
                }
                return result.getInt(1);
            }
        });
    }

    public List<MatchData> getMatchHistory() throws SQLException {
        return databaseManager.withConnection(connection -> {
            List<MatchData> matches = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM matches ORDER BY id DESC");
                 ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    matches.add(map(result));
                }
            }
            return matches;
        });
    }

    public int countWins() throws SQLException {
        return countByResult("WIN");
    }

    public int countLosses() throws SQLException {
        return countByResult("LOSS");
    }

    private int countByResult(String resultValue) throws SQLException {
        return databaseManager.withConnection(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT COUNT(*) FROM matches WHERE result = ?")) {
                statement.setString(1, resultValue);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new SQLException("Could not count matches");
                    }
                    return result.getInt(1);
                }
            }
        });
    }

    private static MatchData map(ResultSet result) throws SQLException {
        return new MatchData(result.getInt("id"), result.getString("result"), result.getInt("arena_id"),
                result.getInt("character_id"), result.getInt("waves_reached"),
                result.getInt("enemies_defeated"), result.getInt("bosses_defeated"),
                result.getDouble("duration_seconds"), result.getString("played_at"));
    }
}
