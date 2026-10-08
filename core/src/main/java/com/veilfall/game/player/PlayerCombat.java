package com.veilfall.game.player;

import com.veilfall.game.data.SkillData;
import com.veilfall.game.enemies.EnemyManager;

import java.util.List;

public final class PlayerCombat {
    private static final float SLAM_EFFECT_DURATION = 0.28f;
    private static final float DODGE_DURATION = 0.18f;
    private static final float DODGE_COOLDOWN = 1f;

    private final Player player;
    private final float attackRange;
    private final float attackCooldown;
    private final int attackDamage;
    private final SkillData shieldBash;
    private final SkillData groundSlam;
    private final SkillData veilGuard;
    private float attackCooldownRemaining;
    private float shieldBashCooldownRemaining;
    private float groundSlamCooldownRemaining;
    private float veilGuardCooldownRemaining;
    private float dodgeCooldownRemaining;

    public PlayerCombat(Player player, List<SkillData> skills) {
        this.player = player;
        attackRange = (float) Math.min(player.getCharacter().getRange(), player.getWeapon().getRange());
        attackCooldown = 1f / (float) player.getWeapon().getAttackSpeed();
        attackDamage = player.getWeapon().getDamage();
        shieldBash = requiredSkill(skills, "Q");
        groundSlam = requiredSkill(skills, "E");
        veilGuard = requiredSkill(skills, "R");
    }

    public void update(float delta) {
        attackCooldownRemaining = Math.max(0f, attackCooldownRemaining - delta);
        shieldBashCooldownRemaining = Math.max(0f, shieldBashCooldownRemaining - delta);
        groundSlamCooldownRemaining = Math.max(0f, groundSlamCooldownRemaining - delta);
        veilGuardCooldownRemaining = Math.max(0f, veilGuardCooldownRemaining - delta);
        dodgeCooldownRemaining = Math.max(0f, dodgeCooldownRemaining - delta);
        player.update(delta);
    }

    public boolean attack(EnemyManager enemies) {
        if (attackCooldownRemaining > 0f || player.isDead()) return false;
        attackCooldownRemaining = attackCooldown;
        player.triggerAttackEffect();
        return enemies.damageInRange(player.getX(), player.getZ(), attackRange, attackDamage, 1f) > 0;
    }

    public boolean shieldBash(EnemyManager enemies) {
        if (shieldBashCooldownRemaining > 0f || player.isDead()) return false;
        shieldBashCooldownRemaining = (float) shieldBash.getCooldown();
        player.triggerAttackEffect();
        float range = Math.min(attackRange * 1.5f, 2.5f);
        return enemies.damageInFront(player.getX(), player.getZ(), player.getFacing().x,
                player.getFacing().z, range, shieldBash.getDamage(), 7f) > 0;
    }

    public boolean groundSlam(EnemyManager enemies) {
        if (groundSlamCooldownRemaining > 0f || player.isDead()) return false;
        groundSlamCooldownRemaining = (float) groundSlam.getCooldown();
        player.triggerGroundSlam(SLAM_EFFECT_DURATION);
        float radius = Math.max(attackRange * 2f, 3.25f);
        return enemies.damageInRange(player.getX(), player.getZ(), radius,
                groundSlam.getDamage(), 5f) > 0;
    }

    public boolean activateVeilGuard() {
        if (veilGuardCooldownRemaining > 0f || player.isDead()) return false;
        veilGuardCooldownRemaining = (float) veilGuard.getCooldown();
        player.activateGuard(3f);
        return true;
    }

    public boolean dodge(float directionX, float directionZ) {
        if (dodgeCooldownRemaining > 0f || player.isDead()) return false;
        dodgeCooldownRemaining = DODGE_COOLDOWN;
        player.startDodge(directionX, directionZ, DODGE_DURATION);
        return true;
    }

    public float getAttackCooldownRemaining() { return attackCooldownRemaining; }
    public float getShieldBashCooldownRemaining() { return shieldBashCooldownRemaining; }
    public float getGroundSlamCooldownRemaining() { return groundSlamCooldownRemaining; }
    public float getVeilGuardCooldownRemaining() { return veilGuardCooldownRemaining; }
    public float getDodgeCooldownRemaining() { return dodgeCooldownRemaining; }

    private static SkillData requiredSkill(List<SkillData> skills, String key) {
        return skills.stream()
                .filter(skill -> skill.getKeyBinding().equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing Warden skill for key " + key));
    }
}
