package com.veilfall.game.enemies;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.veilfall.game.data.MobData;
import com.veilfall.game.database.repository.MobRepository;
import com.veilfall.game.player.Player;

import java.sql.SQLException;
import java.util.List;

public final class EnemyManager implements Disposable {
    private static final float[][] SPAWN_POSITIONS = {
            {-10f, -7f}, {-5f, 10f}, {1f, -11f}, {10f, -6f}, {9f, 9f}
    };

    private final Array<Enemy> enemies = new Array<>();
    private final EnemyAI enemyAI = new EnemyAI();
    private int enemiesDefeated;

    public EnemyManager(MobRepository mobRepository) throws SQLException {
        MobData wretch = mobRepository.getAll().stream()
                .filter(mob -> mob.getName().equals("Veil Wretch"))
                .findFirst()
                .orElseThrow(() -> new SQLException("Veil Wretch definition is missing"));
        for (float[] position : SPAWN_POSITIONS) {
            enemies.add(new Enemy(wretch, position[0], position[1]));
        }
    }

    public void update(Player player, float delta) {
        for (int index = enemies.size - 1; index >= 0; index--) {
            Enemy enemy = enemies.get(index);
            boolean aliveBefore = enemy.isAlive();
            enemyAI.update(enemy, player, delta);
            if (aliveBefore && !enemy.isAlive()) {
                enemiesDefeated++;
            }
            if (enemy.isRemoved()) {
                enemy.dispose();
                enemies.removeIndex(index);
            }
        }
    }

    public int damageInRange(float x, float z, float range, int damage) {
        int hits = 0;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            float dx = enemy.getX() - x;
            float dz = enemy.getZ() - z;
            if (dx * dx + dz * dz <= range * range) {
                enemy.damage(damage);
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
    public boolean isCleared() { return enemies.size == 0; }

    public void appendInstances(List<ModelInstance> destination) {
        for (Enemy enemy : enemies) {
            for (ModelInstance instance : enemy.getInstances()) {
                destination.add(instance);
            }
        }
    }

    @Override
    public void dispose() {
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        enemies.clear();
    }
}
