package com.veilfall.game.database;

import com.veilfall.game.data.PlayerData;
import com.veilfall.game.database.repository.ArenaRepository;
import com.veilfall.game.database.repository.CharacterRepository;
import com.veilfall.game.database.repository.MobRepository;
import com.veilfall.game.database.repository.PlayerRepository;
import com.veilfall.game.database.repository.SkillRepository;
import com.veilfall.game.database.repository.WaveRepository;
import com.veilfall.game.database.repository.WeaponRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

public final class DatabaseSmokeTest {
    private DatabaseSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Path temporaryDirectory = Files.createTempDirectory("veilfall-database-smoke");
        Path databasePath = temporaryDirectory.resolve("saves/veilfall.db");
        try {
            verifyDatabase(databasePath);
            System.out.println("Database smoke test passed: " + databasePath);
        } finally {
            Files.deleteIfExists(databasePath);
            Files.deleteIfExists(databasePath.getParent());
            Files.deleteIfExists(temporaryDirectory);
        }
    }

    private static void verifyDatabase(Path databasePath) throws Exception {
        try (DatabaseManager manager = new DatabaseManager(databasePath)) {
            DatabaseInitializer initializer = new DatabaseInitializer(manager);
            initializer.initialize();
            initializer.initialize();

            require(Files.isRegularFile(databasePath), "database file was not created");
            require(tableCount(manager, "player_profile") == 1, "player profile table/data missing");
            require(new PlayerRepository(manager).getPlayerProfile()
                    .map(PlayerData::getName).filter("Wanderer"::equals).isPresent(), "Wanderer profile missing");
            require(new CharacterRepository(manager).getAll().size() == 3, "expected 3 characters");
            require(new WeaponRepository(manager).getAll().size() == 3, "expected 3 weapons");
            require(new SkillRepository(manager).getSkillsForCharacter(
                    new CharacterRepository(manager).getAll().stream()
                            .filter(character -> character.getName().equals("Warden"))
                            .findFirst().orElseThrow().getId()).size() == 3, "expected 3 Warden skills");
            require(new MobRepository(manager).getAll().size() == 4, "expected 4 mobs");
            require(new ArenaRepository(manager).getAll().size() == 1, "expected Mistwood arena");
            require(new WaveRepository(manager).getWavesForArena(
                    new ArenaRepository(manager).getAll().getFirst().getId()).size() == 5, "expected 5 waves");
            int waveEnemyDefinitions = manager.withConnection(connection -> {
                try (var statement = connection.prepareStatement("SELECT COUNT(*) FROM wave_enemies");
                     var result = statement.executeQuery()) {
                    if (!result.next()) {
                        throw new SQLException("Could not count wave enemy definitions");
                    }
                    return result.getInt(1);
                }
            });
            require(waveEnemyDefinitions == 8, "expected 8 wave enemy definitions");
            require(tableCount(manager, "matches") == 0, "match history should initially be empty");
        }
    }

    private static int tableCount(DatabaseManager manager, String tableName) throws SQLException {
        if (!tableName.matches("[a-z_]+")) {
            throw new IllegalArgumentException("Invalid table name");
        }
        return manager.withConnection(connection -> {
            try (var statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                if (!result.next()) {
                    throw new SQLException("Could not count rows in " + tableName);
                }
                return result.getInt(1);
            }
        });
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
