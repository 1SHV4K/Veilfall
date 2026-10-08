package com.veilfall.game.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.veilfall.game.enemies.EnemyManager;

public final class PlayerController extends InputAdapter {
    private final Player player;
    private final PlayerCombat combat;
    private final EnemyManager enemies;

    public PlayerController(Player player, PlayerCombat combat, EnemyManager enemies) {
        this.player = player;
        this.combat = combat;
        this.enemies = enemies;
    }

    public void update(float delta) {
        float horizontal = 0f;
        float depth = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) horizontal -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) horizontal += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) depth -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) depth += 1f;

        if (horizontal != 0f || depth != 0f) {
            float length = (float) Math.sqrt(horizontal * horizontal + depth * depth);
            float step = (float) player.getCharacter().getSpeed() * delta / length;
            player.move(horizontal * step, depth * step);
        }
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            combat.attack(enemies);
        }
    }
}
