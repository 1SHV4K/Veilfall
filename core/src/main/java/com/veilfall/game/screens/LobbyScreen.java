package com.veilfall.game.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.veilfall.game.VeilfallGame;
import com.veilfall.game.data.CharacterData;
import com.veilfall.game.data.MobData;
import com.veilfall.game.data.PlayerData;
import com.veilfall.game.data.WeaponData;

import java.sql.SQLException;
import java.util.List;

public final class LobbyScreen extends AbstractGameScreen {
    private final Table content = new Table();
    private final Label playerLabel;
    private final Label characterLabel;
    private PlayerData player;
    private List<CharacterData> characters;
    private List<MobData> mobs;
    private List<WeaponData> weapons;

    public LobbyScreen(VeilfallGame game) {
        super(game);
        loadDatabaseData();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(18f);
        stage.addActor(root);

        Label title = game.getUiTheme().label("VEILFALL — LOBBY");
        title.setFontScale(1.45f);
        root.add(title).left().expandX().row();
        playerLabel = game.getUiTheme().label("");
        characterLabel = game.getUiTheme().label("");
        root.add(playerLabel).left().padTop(12f).row();
        root.add(characterLabel).left().padTop(4f).row();

        Table navigation = new Table();
        addNavigationButton(navigation, "PROFILE", this::showProfile);
        addNavigationButton(navigation, "CHARACTERS", this::showCharacters);
        addNavigationButton(navigation, "MOBS", this::showMobs);
        addNavigationButton(navigation, "WEAPONS", this::showWeapons);
        root.add(navigation).left().padTop(16f).row();

        content.top().left().pad(12f);
        root.add(content).fill().expand().padTop(8f).row();
        TextButton startButton = game.getUiTheme().button("START");
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showArenaSelect();
            }
        });
        root.add(startButton).right().width(150f).height(42f).padTop(8f);
        refreshHeader();
        showProfile();
    }

    private void loadDatabaseData() {
        try {
            player = game.getPlayerRepository().getPlayerProfile()
                    .orElseThrow(() -> new SQLException("Player profile is missing"));
            characters = game.getCharacterRepository().getAll();
            mobs = game.getMobRepository().getAll();
            weapons = game.getWeaponRepository().getAll();
            if (characters.isEmpty()) {
                throw new SQLException("No characters are available");
            }
            if (game.getSelectedCharacter() == null
                    || characters.stream().noneMatch(character ->
                    character.getId() == game.getSelectedCharacter().getId())) {
                game.setSelectedCharacter(characters.getFirst());
            }
        } catch (SQLException exception) {
            throw new GdxRuntimeException("Failed to load lobby data from SQLite", exception);
        }
    }

    private void refreshHeader() {
        CharacterData selected = game.getSelectedCharacter();
        playerLabel.setText("Player: " + player.getName() + "    Level: " + player.getLevel());
        characterLabel.setText("Selected character: " + selected.getName()
                + "    HP: " + selected.getHp() + "    Attack: " + selected.getAttack());
    }

    private void addNavigationButton(Table navigation, String title, Runnable action) {
        TextButton button = game.getUiTheme().button(title);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        navigation.add(button).width(112f).height(38f).padRight(8f);
    }

    private void showProfile() {
        content.clearChildren();
        addContentLine("PROFILE");
        addContentLine("Name: " + player.getName());
        addContentLine("Level: " + player.getLevel());
        addContentLine("Experience: " + player.getExperience());
        addContentLine("Wins: " + player.getWins() + "    Losses: " + player.getLosses()
                + "    Battles: " + player.getBattles());
    }

    private void showCharacters() {
        content.clearChildren();
        addContentLine("CHARACTERS — select one to use in battle");
        for (CharacterData character : characters) {
            TextButton button = game.getUiTheme().button(character.getName() + " — HP " + character.getHp()
                    + " — Attack " + character.getAttack());
            button.setChecked(character.getId() == game.getSelectedCharacter().getId());
            button.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    game.setSelectedCharacter(character);
                    refreshHeader();
                    showCharacters();
                }
            });
            content.add(button).left().width(340f).height(34f).padBottom(6f).row();
        }
    }

    private void showMobs() {
        content.clearChildren();
        addContentLine("MOBS");
        for (MobData mob : mobs) {
            addContentLine(mob.getName() + " — " + mob.getType() + " — HP " + mob.getHp()
                    + " — Damage " + mob.getDamage());
        }
    }

    private void showWeapons() {
        content.clearChildren();
        addContentLine("WEAPONS");
        for (WeaponData weapon : weapons) {
            addContentLine(weapon.getName() + " — Damage " + weapon.getDamage()
                    + " — Speed " + weapon.getAttackSpeed() + " — Range " + weapon.getRange());
        }
    }

    private void addContentLine(String text) {
        Label label = game.getUiTheme().label(text);
        label.setAlignment(Align.left);
        content.add(label).left().padBottom(7f).row();
    }
}
