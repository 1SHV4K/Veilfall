package com.veilfall.game.enemies;

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
import com.veilfall.game.data.MobData;
import com.veilfall.game.player.Player;

public final class Enemy implements Disposable {
    private static final long ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;
    private static final float ATTACK_COOLDOWN = 1.1f;
    private static final float DEATH_DURATION = 0.45f;
    private static final Color NORMAL_COLOR = new Color(0.48f, 0.22f, 0.28f, 1f);
    private static final Color HIT_COLOR = new Color(1f, 0.78f, 0.68f, 1f);

    private final MobData data;
    private final int maxHealth;
    private final Vector3 position = new Vector3();
    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();
    private int currentHealth;
    private float attackCooldown;
    private float hitFlash;
    private float deathTimer = -1f;
    private float knockbackRemaining;
    private float knockbackX;
    private float knockbackZ;

    public Enemy(MobData data, float x, float z) {
        this.data = data;
        maxHealth = data.getHp();
        currentHealth = maxHealth;
        position.set(x, 0f, z);
        createModel();
        updateModel();
    }

    public MobData getData() { return data; }
    public float getX() { return position.x; }
    public float getZ() { return position.z; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isAlive() { return currentHealth > 0; }
    public boolean isRemoved() { return deathTimer >= DEATH_DURATION; }
    public Vector3 getPosition() { return position; }
    public float getAttackCooldown() { return attackCooldown; }
    public boolean canAttack() { return attackCooldown <= 0f && isAlive(); }
    public boolean isKnockedBack() { return knockbackRemaining > 0f; }

    public Iterable<ModelInstance> getInstances() {
        return instances;
    }

    public void updateTimers(float delta) {
        if (attackCooldown > 0f) attackCooldown -= delta;
        if (knockbackRemaining > 0f) {
            float step = Math.min(knockbackRemaining, delta);
            position.x += knockbackX * step;
            position.z += knockbackZ * step;
            knockbackRemaining = Math.max(0f, knockbackRemaining - delta);
            updateModel();
        }
        if (hitFlash > 0f) {
            hitFlash -= delta;
            if (hitFlash <= 0f) {
                setBodyColor(NORMAL_COLOR);
            }
        }
        if (!isAlive() && deathTimer >= 0f) {
            deathTimer += delta;
            float scale = Math.max(0.02f, 1f - deathTimer / DEATH_DURATION);
            for (int i = 0; i < 2; i++) {
                instances.get(i).transform.setToTranslation(position.x, (i == 0 ? 0.75f : 1.38f) * scale,
                        position.z).scale(scale, scale, scale);
            }
            instances.get(2).transform.setToTranslation(position.x, 1.8f * scale, position.z)
                    .scale(scale, scale, scale);
            instances.get(3).transform.setToTranslation(position.x, 1.8f * scale, position.z)
                    .scale(scale, scale, scale);
        }
    }

    public void moveToward(float targetX, float targetZ, float delta) {
        if (!isAlive()) return;
        float dx = targetX - position.x;
        float dz = targetZ - position.z;
        float distance = (float) Math.sqrt(dx * dx + dz * dz);
        if (distance <= 0.001f) return;
        float step = Math.min((float) data.getSpeed() * delta, distance);
        position.x += dx / distance * step;
        position.z += dz / distance * step;
        updateModel();
    }

    public void takeDamage(int amount, float directionX, float directionZ, float knockbackForce) {
        if (!isAlive()) return;
        currentHealth = MathUtils.clamp(currentHealth - amount, 0, maxHealth);
        if (!isAlive()) {
            deathTimer = 0f;
        } else {
            hitFlash = 0.14f;
            setBodyColor(HIT_COLOR);
            float directionLength = (float) Math.sqrt(directionX * directionX + directionZ * directionZ);
            if (directionLength > 0.001f && knockbackForce > 0f) {
                knockbackX = directionX / directionLength * knockbackForce;
                knockbackZ = directionZ / directionLength * knockbackForce;
                knockbackRemaining = 0.12f;
            }
        }
        updateHealthBar();
    }

    public void attack(Player player) {
        if (!canAttack()) return;
        attackCooldown = ATTACK_COOLDOWN;
        player.takeDamage(data.getDamage());
    }

    private void createModel() {
        ModelBuilder builder = new ModelBuilder();
        Color enemyColor = new Color(NORMAL_COLOR);
        models.add(builder.createBox(0.82f, 1.15f, 0.6f,
                new Material(ColorAttribute.createDiffuse(enemyColor)), ATTRIBUTES));
        models.add(builder.createBox(0.58f, 0.58f, 0.58f,
                new Material(ColorAttribute.createDiffuse(new Color(0.23f, 0.16f, 0.20f, 1f))), ATTRIBUTES));
        models.add(builder.createBox(1.05f, 0.12f, 0.12f,
                new Material(ColorAttribute.createDiffuse(new Color(0.12f, 0.12f, 0.14f, 1f))), ATTRIBUTES));
        models.add(builder.createBox(1f, 0.08f, 0.08f,
                new Material(ColorAttribute.createDiffuse(new Color(0.25f, 0.82f, 0.35f, 1f))), ATTRIBUTES));
        for (Model model : models) {
            instances.add(new ModelInstance(model));
        }
    }

    private void updateModel() {
        instances.get(0).transform.setToTranslation(position.x, 0.72f, position.z);
        instances.get(1).transform.setToTranslation(position.x, 1.55f, position.z);
        updateHealthBar();
    }

    private void updateHealthBar() {
        float ratio = currentHealth / (float) maxHealth;
        float width = 0.95f * ratio;
        instances.get(2).transform.setToTranslation(position.x, 2.05f, position.z);
        instances.get(3).transform.setToTranslation(position.x - 0.475f + width * 0.5f,
                2.05f, position.z + 0.01f).scale(width, 1f, 1f);
    }

    private void setBodyColor(Color color) {
        for (int i = 0; i < 2; i++) {
            ColorAttribute diffuse = (ColorAttribute) instances.get(i).materials.first().get(ColorAttribute.Diffuse);
            diffuse.color.set(color);
        }
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
