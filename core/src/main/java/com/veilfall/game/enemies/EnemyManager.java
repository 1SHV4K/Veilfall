package com.veilfall.game.enemies;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.veilfall.game.data.MobData;
import com.veilfall.game.data.WaveData;
import com.veilfall.game.data.WaveEnemyData;
import com.veilfall.game.database.repository.MobRepository;
import com.veilfall.game.player.Player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EnemyManager implements Disposable {
    private static final float[][] SPAWN_POSITIONS = {
            {-12f, 0f}, {12f, 0f}, {0f, -12f}, {0f, 12f},
            {-9f, -9f}, {9f, -9f}, {-9f, 9f}, {9f, 9f}
    };

    private final Array<Enemy> enemies = new Array<>();
    private final EnemyAI enemyAI = new EnemyAI();
    private final Map<Integer, MobData> mobsById = new HashMap<>();
    private int enemiesDefeated;
    private int bossesDefeated;
    private int nextSpawnIndex;

    public EnemyManager(MobRepository mobRepository) throws SQLException {
        for (MobData mob : mobRepository.getAll()) {
            mobsById.put(mob.getId(), mob);
        }
        if (mobsById.isEmpty()) throw new SQLException("No mob definitions are available");
    }

    public void spawnWave(List<WaveEnemyData> enemyDefinitions, WaveData wave) throws SQLException {
        clearEnemies();
        for (WaveEnemyData definition : enemyDefinitions) {
            MobData baseMob = mobsById.get(definition.getMobId());
            if (baseMob == null) {
                throw new SQLException("Missing mob definition " + definition.getMobId()
                        + " referenced by wave " + wave.getWaveNumber());
            }
            MobData scaledMob = scaleMob(baseMob, wave);
            for (int count = 0; count < definition.getQuantity(); count++) {
                float[] position = nextSpawnPosition();
                enemies.add(new Enemy(scaledMob, position[0], position[1]));
            }
        }
        if (enemies.isEmpty()) {
            throw new SQLException("Wave " + wave.getWaveNumber() + " did not spawn any enemies");
        }
    }

    public void update(Player player, float delta) {
        for (int index = enemies.size - 1; index >= 0; index--) {
            Enemy enemy = enemies.get(index);
            enemyAI.update(enemy, player, delta);
            if (enemy.isRemoved()) {
                enemy.dispose();
                enemies.removeIndex(index);
            }
        }
    }

    public int damageInRange(float x, float z, float range, int damage) {
        return damageInRange(x, z, range, damage, 0f);
    }

    public int damageInRange(float x, float z, float range, int damage, float knockbackForce) {
        int hits = 0;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            float dx = enemy.getX() - x;
            float dz = enemy.getZ() - z;
            if (dx * dx + dz * dz <= range * range) {
                enemy.takeDamage(damage, dx, dz, knockbackForce);
                recordDefeat(enemy);
                hits++;
            }
        }
        return hits;
    }

    public int damageInFront(float x, float z, float facingX, float facingZ,
                             float range, int damage, float knockbackForce) {
        int hits = 0;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            float dx = enemy.getX() - x;
            float dz = enemy.getZ() - z;
            float distanceSquared = dx * dx + dz * dz;
            if (distanceSquared > range * range || distanceSquared <= 0.0001f) continue;
            float distance = (float) Math.sqrt(distanceSquared);
            float facingDot = (dx / distance) * facingX + (dz / distance) * facingZ;
            if (facingDot >= 0.35f) {
                enemy.takeDamage(damage, dx, dz, knockbackForce);
                recordDefeat(enemy);
                hits++;
            }
        }
        return hits;
    }

    public int getAliveCount() {
        int alive = 0;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) alive++;
        }
        return alive;
    }

    public int getEnemiesDefeated() { return enemiesDefeated; }
    public int getBossesDefeated() { return bossesDefeated; }
    public boolean isCleared() { return getAliveCount() == 0 && enemies.size == 0; }

    public void appendInstances(Array<ModelInstance> destination) {
        for (Enemy enemy : enemies) {
            for (ModelInstance instance : enemy.getInstances()) {
                destination.add(instance);
            }
        }
    }

    private void clearEnemies() {
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        enemies.clear();
    }

    private void recordDefeat(Enemy enemy) {
        if (enemy.isAlive()) return;
        enemiesDefeated++;
        if (enemy.getData().isBoss()) bossesDefeated++;
    }

    private float[] nextSpawnPosition() {
        int spawnIndex = nextSpawnIndex++;
        float[] base = SPAWN_POSITIONS[spawnIndex % SPAWN_POSITIONS.length];
        int ring = spawnIndex / SPAWN_POSITIONS.length;
        if (ring == 0) return base;
        float angle = spawnIndex * 2.3999632f;
        float offset = ring * 1.4f;
        return new float[]{base[0] + (float) Math.cos(angle) * offset,
                base[1] + (float) Math.sin(angle) * offset};
    }

    private static MobData scaleMob(MobData mob, WaveData wave) {
        int health = Math.max(1, (int) Math.round(mob.getHp() * wave.getHealthMultiplier()));
        int damage = Math.max(0, (int) Math.round(mob.getDamage() * wave.getDamageMultiplier()));
        return new MobData(mob.getId(), mob.getName(), mob.getType(), health, damage,
                mob.getSpeed(), mob.getRange(), mob.isBoss());
    }

    @Override
    public void dispose() {
        clearEnemies();
        mobsById.clear();
    }
}
