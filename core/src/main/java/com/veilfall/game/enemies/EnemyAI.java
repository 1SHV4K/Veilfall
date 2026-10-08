package com.veilfall.game.enemies;

import com.veilfall.game.player.Player;

public final class EnemyAI {
    public void update(Enemy enemy, Player player, float delta) {
        enemy.updateTimers(delta);
        if (!enemy.isAlive() || player.isDead()) return;

        float dx = player.getX() - enemy.getX();
        float dz = player.getZ() - enemy.getZ();
        float distance = (float) Math.sqrt(dx * dx + dz * dz);
        if (distance <= enemy.getData().getRange()) {
            enemy.attack(player);
        } else {
            enemy.moveToward(player.getX(), player.getZ(), delta);
        }
    }
}
