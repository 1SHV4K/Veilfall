package com.veilfall.game.player;

import com.veilfall.game.enemies.EnemyManager;

public final class PlayerCombat {
    private final Player player;
    private final float attackRange;
    private final float attackCooldown;
    private final int attackDamage;
    private float cooldownRemaining;

    public PlayerCombat(Player player) {
        this.player = player;
        attackRange = (float) Math.min(player.getCharacter().getRange(), player.getWeapon().getRange());
        attackCooldown = 1f / (float) player.getWeapon().getAttackSpeed();
        attackDamage = player.getWeapon().getDamage();
    }

    public void update(float delta) {
        cooldownRemaining = Math.max(0f, cooldownRemaining - delta);
        player.update(delta);
    }

    public boolean attack(EnemyManager enemies) {
        if (cooldownRemaining > 0f || player.isDead()) {
            return false;
        }
        cooldownRemaining = attackCooldown;
        player.triggerAttackEffect();
        return enemies.damageInRange(player.getX(), player.getZ(), attackRange, attackDamage) > 0;
    }
}
