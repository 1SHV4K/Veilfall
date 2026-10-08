package com.veilfall.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.veilfall.game.VeilfallGame;
import com.veilfall.game.data.ArenaData;
import com.veilfall.game.data.CharacterData;
import com.veilfall.game.data.WeaponData;
import com.veilfall.game.enemies.EnemyManager;
import com.veilfall.game.player.Player;
import com.veilfall.game.player.PlayerCombat;
import com.veilfall.game.player.PlayerController;

import java.sql.SQLException;

public final class BattleScreen extends AbstractGameScreen {
    private final ArenaData arena;
    private final CharacterData character;
    private final Player player;
    private final PlayerCombat playerCombat;
    private final PlayerController playerController;
    private final EnemyManager enemyManager;
    private final Label healthLabel;
    private final Label enemiesLabel;
    private final Label shieldBashLabel;
    private final Label groundSlamLabel;
    private final Label veilGuardLabel;
    private final Label dodgeLabel;
    private final Cell<Image> healthFillCell;
    private final Array<ModelInstance> renderInstances = new Array<>();
    private final float healthBarWidth = 200f;
    private float elapsedSeconds;
    private boolean resultShown;

    public BattleScreen(VeilfallGame game, ArenaData arena, CharacterData character) {
        super(game);
        this.arena = arena;
        this.character = character;
        try {
            WeaponData weapon = game.getWeaponRepository().getAll().stream()
                    .filter(candidate -> candidate.getCharacterId() == character.getId())
                    .findFirst()
                    .orElseThrow(() -> new SQLException("No weapon is assigned to " + character.getName()));
            var skills = game.getSkillRepository().getSkillsForCharacter(character.getId());
            player = new Player(character, weapon);
            playerCombat = new PlayerCombat(player, skills);
            enemyManager = new EnemyManager(game.getMobRepository());
        } catch (SQLException | IllegalArgumentException exception) {
            throw new GdxRuntimeException("Failed to load battle definitions from SQLite", exception);
        }
        playerController = new PlayerController(player, playerCombat, enemyManager);

        Table hud = new Table();
        hud.setFillParent(true);
        hud.top().left().pad(14f);
        stage.addActor(hud);
        Label title = game.getUiTheme().label(arena.getName());
        title.setFontScale(1.25f);
        hud.add(title).left().padBottom(7f).row();
        healthLabel = game.getUiTheme().label("");
        hud.add(healthLabel).left().padBottom(4f).row();

        TextureRegion white = new TextureRegion(game.getUiTheme().getSkin()
                .get("white-texture", com.badlogic.gdx.graphics.Texture.class));
        Table healthBar = new Table();
        healthBar.setBackground(new TextureRegionDrawable(white).tint(new Color(0.10f, 0.10f, 0.12f, 1f)));
        Image healthFill = new Image(new TextureRegionDrawable(white)
                .tint(new Color(0.25f, 0.78f, 0.38f, 1f)));
        healthFillCell = healthBar.add(healthFill).left().width(healthBarWidth).height(10f);
        hud.add(healthBar).width(healthBarWidth).height(10f).left().padBottom(8f).row();

        enemiesLabel = game.getUiTheme().label("");
        hud.add(enemiesLabel).left().padBottom(8f).row();
        TextButton back = game.getUiTheme().button("BACK TO ARENA SELECT");
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showArenaSelect();
            }
        });
        hud.add(back).left().width(190f).height(34f).padTop(3f);

        Table abilityHud = new Table();
        abilityHud.setFillParent(true);
        abilityHud.bottom().left().pad(14f);
        stage.addActor(abilityHud);
        shieldBashLabel = game.getUiTheme().label("");
        groundSlamLabel = game.getUiTheme().label("");
        veilGuardLabel = game.getUiTheme().label("");
        dodgeLabel = game.getUiTheme().label("");
        abilityHud.add(shieldBashLabel).left().padRight(14f);
        abilityHud.add(groundSlamLabel).left().padRight(14f);
        abilityHud.add(veilGuardLabel).left().padRight(14f);
        abilityHud.add(dodgeLabel).left();
        updateHud();
    }

    @Override
    public void show() {
        super.show();
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, playerController,
                game.getRenderer().getCameraController()));
    }

    @Override
    public void render(float delta) {
        if (resultShown) return;
        float frameDelta = Math.min(delta, 0.05f);
        elapsedSeconds += frameDelta;
        playerCombat.update(frameDelta);
        playerController.update(frameDelta);
        enemyManager.update(player, frameDelta);
        game.getRenderer().getCameraController().setTargetPosition(player.getX(), player.getZ());
        updateHud();

        if (player.isDead()) {
            showResult(false);
            return;
        }
        if (enemyManager.isCleared()) {
            showResult(true);
            return;
        }

        renderInstances.clear();
        for (ModelInstance instance : player.getInstances()) {
            renderInstances.add(instance);
        }
        enemyManager.appendInstances(renderInstances);
        game.getRenderer().render(renderInstances);
        drawStage(frameDelta);
    }

    private void updateHud() {
        healthLabel.setText(character.getName() + " HP: " + player.getCurrentHealth()
                + " / " + player.getMaxHealth());
        enemiesLabel.setText("Enemies Remaining: " + enemyManager.getAliveCount());
        healthFillCell.width(healthBarWidth * player.getCurrentHealth() / player.getMaxHealth());
        updateAbilityLabel(shieldBashLabel, "Q Shield Bash", playerCombat.getShieldBashCooldownRemaining());
        updateAbilityLabel(groundSlamLabel, "E Ground Slam", playerCombat.getGroundSlamCooldownRemaining());
        updateAbilityLabel(veilGuardLabel,
                player.isGuarding() ? String.format("R Veil Guard %.1fs", player.getGuardRemaining()) : "R Veil Guard",
                playerCombat.getVeilGuardCooldownRemaining());
        updateAbilityLabel(dodgeLabel, "SPACE Dodge", playerCombat.getDodgeCooldownRemaining());
    }

    private static void updateAbilityLabel(Label label, String name, float cooldownRemaining) {
        if (cooldownRemaining > 0f) {
            label.setText(String.format("%s (%.1f)", name, cooldownRemaining));
            label.setColor(Color.GRAY);
        } else {
            label.setText(name);
            label.setColor(Color.WHITE);
        }
    }

    private void showResult(boolean victory) {
        resultShown = true;
        game.showResult(new BattleOutcome(victory, character.getName(), arena.getName(),
                victory ? 1 : 0, enemyManager.getEnemiesDefeated(), 0, elapsedSeconds));
    }

    @Override
    public void dispose() {
        super.dispose();
        player.dispose();
        enemyManager.dispose();
    }
}
