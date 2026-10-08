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

    private final CharacterData character;
    private final WeaponData weapon;
    private final int maxHealth;
    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private final Vector3 position = new Vector3(0f, 0f, 0f);
    private int currentHealth;
    private float attackEffectRemaining;

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

    public Iterable<ModelInstance> getInstances() {
        return instances;
    }

    public void move(float x, float z) {
        position.x = MathUtils.clamp(position.x + x, -ARENA_LIMIT, ARENA_LIMIT);
        position.z = MathUtils.clamp(position.z + z, -ARENA_LIMIT, ARENA_LIMIT);
        updateModel();
    }

    public void update(float delta) {
        attackEffectRemaining = Math.max(0f, attackEffectRemaining - delta);
        updateModel();
    }

    public void triggerAttackEffect() {
        attackEffectRemaining = 0.18f;
        updateModel();
    }

    public void receiveDamage(int rawDamage) {
        int mitigatedDamage = Math.max(1, rawDamage - character.getDefense());
        currentHealth = MathUtils.clamp(currentHealth - mitigatedDamage, 0, maxHealth);
    }

    private void createModel() {
        ModelBuilder builder = new ModelBuilder();
        Model torso = box(builder, 0.75f, 1.05f, 0.42f, new Color(0.22f, 0.34f, 0.48f, 1f));
        Model head = box(builder, 0.48f, 0.48f, 0.48f, new Color(0.74f, 0.59f, 0.43f, 1f));
        Model limb = box(builder, 0.24f, 0.78f, 0.28f, new Color(0.25f, 0.29f, 0.34f, 1f));
        Model arm = box(builder, 0.23f, 0.72f, 0.25f, new Color(0.74f, 0.59f, 0.43f, 1f));
        Model sword = box(builder, 0.12f, 0.95f, 0.12f, new Color(0.76f, 0.80f, 0.84f, 1f));
        models.addAll(torso, head, limb, arm, sword);
        instances.add(new ModelInstance(torso));
        instances.add(new ModelInstance(head));
        instances.add(new ModelInstance(limb));
        instances.add(new ModelInstance(limb));
        instances.add(new ModelInstance(arm));
        instances.add(new ModelInstance(arm));
        instances.add(new ModelInstance(sword));
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
