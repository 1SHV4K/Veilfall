package com.veilfall.game.player;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.veilfall.game.data.CharacterData;
import com.veilfall.game.data.WeaponData;

public final class Player implements Disposable {
    private static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    private static final float ARENA_LIMIT = 14f;
    private static final Color ARMOR_COLOR = new Color(0.22f, 0.34f, 0.48f, 1f);
    private static final Color GUARD_COLOR = new Color(0.30f, 0.58f, 0.92f, 1f);
    private static final Color DODGE_COLOR = new Color(0.82f, 0.88f, 1f, 1f);

    private final CharacterData character;
    private final WeaponData weapon;
    private final int maxHealth;
    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private final Vector3 position = new Vector3(0f, 0f, 0f);
    private final Vector3 facing = new Vector3(1f, 0f, 0f);
    private final Vector3 dodgeDirection = new Vector3();
    private int currentHealth;
    private float attackEffectRemaining;
    private float guardRemaining;
    private float invulnerabilityRemaining;
    private float dodgeMovementRemaining;
    private float dodgeDuration;
    private float slamEffectRemaining;

    public Player(CharacterData character, WeaponData weapon) {
        this.character = character;
        this.weapon = weapon;
        maxHealth = character.getHp();
        currentHealth = maxHealth;
        createModel();
        updateModel();
    }

    public CharacterData getCharacter() { return character; }
    public WeaponData getWeapon() { return weapon; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public float getX() { return position.x; }
    public float getZ() { return position.z; }
    public Vector3 getPosition() { return position; }
    public boolean isDead() { return currentHealth <= 0; }
    public boolean isGuarding() { return guardRemaining > 0f; }
    public boolean isInvulnerable() { return invulnerabilityRemaining > 0f; }
    public boolean isDodging() { return dodgeMovementRemaining > 0f; }
    public float getGuardRemaining() { return guardRemaining; }
    public Vector3 getFacing() { return facing; }

    public Iterable<ModelInstance> getInstances() {
        return instances;
    }

    public void move(float x, float z) {
        if (isDodging()) return;
        if (x != 0f || z != 0f) {
            facing.set(x, 0f, z).nor();
        }
        position.x = MathUtils.clamp(position.x + x, -ARENA_LIMIT, ARENA_LIMIT);
        position.z = MathUtils.clamp(position.z + z, -ARENA_LIMIT, ARENA_LIMIT);
        updateModel();
    }

    public void update(float delta) {
        attackEffectRemaining = Math.max(0f, attackEffectRemaining - delta);
        guardRemaining = Math.max(0f, guardRemaining - delta);
        invulnerabilityRemaining = Math.max(0f, invulnerabilityRemaining - delta);
        slamEffectRemaining = Math.max(0f, slamEffectRemaining - delta);
        if (dodgeMovementRemaining > 0f) {
            float step = Math.min(2.8f * delta / dodgeDuration,
                    2.8f * dodgeMovementRemaining / dodgeDuration);
            position.x = MathUtils.clamp(position.x + dodgeDirection.x * step, -ARENA_LIMIT, ARENA_LIMIT);
            position.z = MathUtils.clamp(position.z + dodgeDirection.z * step, -ARENA_LIMIT, ARENA_LIMIT);
            dodgeMovementRemaining = Math.max(0f, dodgeMovementRemaining - delta);
        }
        updateModel();
    }

    public void triggerAttackEffect() {
        attackEffectRemaining = 0.18f;
        updateModel();
    }

    public void activateGuard(float duration) {
        guardRemaining = duration;
    }

    public void triggerGroundSlam(float duration) {
        slamEffectRemaining = duration;
    }

    public void startDodge(float directionX, float directionZ, float duration) {
        dodgeDirection.set(directionX, 0f, directionZ);
        if (dodgeDirection.isZero(0.001f)) {
            dodgeDirection.set(facing);
        } else {
            dodgeDirection.nor();
            facing.set(dodgeDirection);
        }
        dodgeMovementRemaining = duration;
        dodgeDuration = duration;
        invulnerabilityRemaining = 0.35f;
    }

    public boolean takeDamage(int rawDamage) {
        if (isDead() || isInvulnerable()) {
            return false;
        }
        float guardMultiplier = isGuarding() ? 0.25f : 1f;
        int guardedDamage = Math.max(1, Math.round(rawDamage * guardMultiplier));
        int mitigatedDamage = Math.max(1, guardedDamage - character.getDefense());
        currentHealth = MathUtils.clamp(currentHealth - mitigatedDamage, 0, maxHealth);
        updateModel();
        return true;
    }

    private void createModel() {
        ModelBuilder builder = new ModelBuilder();
        Model torso = box(builder, 0.75f, 1.05f, 0.42f, new Color(0.22f, 0.34f, 0.48f, 1f));
        Model head = box(builder, 0.48f, 0.48f, 0.48f, new Color(0.74f, 0.59f, 0.43f, 1f));
        Model limb = box(builder, 0.24f, 0.78f, 0.28f, new Color(0.25f, 0.29f, 0.34f, 1f));
        Model arm = box(builder, 0.23f, 0.72f, 0.25f, new Color(0.74f, 0.59f, 0.43f, 1f));
        Model sword = box(builder, 0.12f, 0.95f, 0.12f, new Color(0.76f, 0.80f, 0.84f, 1f));
        Model effect = builder.createCylinder(2f, 0.06f, 2f, 20,
                new Material(ColorAttribute.createDiffuse(new Color(0.25f, 0.7f, 1f, 0.55f))),
                ATTRIBUTES);
        models.addAll(torso, head, limb, arm, sword, effect);
        instances.add(new ModelInstance(torso));
        instances.add(new ModelInstance(head));
        instances.add(new ModelInstance(limb));
        instances.add(new ModelInstance(limb));
        instances.add(new ModelInstance(arm));
        instances.add(new ModelInstance(arm));
        instances.add(new ModelInstance(sword));
        instances.add(new ModelInstance(effect));
    }

    private static Model box(ModelBuilder builder, float width, float height, float depth, Color color) {
        return builder.createBox(width, height, depth,
                new Material(ColorAttribute.createDiffuse(color)),
                ATTRIBUTES);
    }

    private void updateModel() {
        instances.get(0).transform.setToTranslation(position.x, position.y + 1.45f, position.z);
        instances.get(1).transform.setToTranslation(position.x, position.y + 2.25f, position.z);
        instances.get(2).transform.setToTranslation(position.x - 0.22f, position.y + 0.42f, position.z);
        instances.get(3).transform.setToTranslation(position.x + 0.22f, position.y + 0.42f, position.z);
        instances.get(4).transform.setToTranslation(position.x - 0.53f, position.y + 1.45f, position.z);
        instances.get(5).transform.setToTranslation(position.x + 0.53f, position.y + 1.45f, position.z);
        instances.get(6).transform.setToTranslation(position.x + 0.67f, position.y + 1.45f, position.z - 0.12f)
                .rotate(Vector3.Y, attackEffectRemaining > 0f ? 65f : 0f);
        float effectScale = slamEffectRemaining > 0f
                ? 1f + 2.3f * (1f - slamEffectRemaining / 0.28f)
                : isGuarding() ? 0.95f : isInvulnerable() ? 0.72f : 0.001f;
        instances.get(7).transform.setToTranslation(position.x, 0.08f, position.z)
                .scale(effectScale, 1f, effectScale);
        setArmorColor(isGuarding()
                ? GUARD_COLOR
                : isInvulnerable() ? DODGE_COLOR : ARMOR_COLOR);
    }

    private void setArmorColor(Color color) {
        ColorAttribute diffuse = (ColorAttribute) instances.get(0).materials.first()
                .get(ColorAttribute.Diffuse);
        diffuse.color.set(color);
    }

    @Override
    public void dispose() {
        for (Model model : models) {
            model.dispose();
        }
        models.clear();
        instances.clear();
    }
}
