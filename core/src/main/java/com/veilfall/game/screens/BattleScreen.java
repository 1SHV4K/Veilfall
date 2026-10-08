package com.veilfall.game.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.veilfall.game.VeilfallGame;
import com.veilfall.game.data.ArenaData;
import com.veilfall.game.data.CharacterData;

public final class BattleScreen extends AbstractGameScreen {
    private final ArenaData arena;
    private final CharacterData character;
    private float elapsedSeconds;

    public BattleScreen(VeilfallGame game, ArenaData arena, CharacterData character) {
        super(game);
        this.arena = arena;
        this.character = character;

        Table root = new Table();
        root.setFillParent(true);
        root.top().left().pad(22f);
        stage.addActor(root);
        Label title = game.getUiTheme().label("BATTLE");
        title.setFontScale(1.5f);
        root.add(title).left().padBottom(18f).row();
        addLine(root, "Arena: " + arena.getName());
        addLine(root, "Wave: 1 / 5");
        addLine(root, "Player: " + character.getName());
        addLine(root, "Enemies Remaining: 0");

        Table actions = new Table();
        addAction(actions, "SIMULATE WIN", true);
        addAction(actions, "SIMULATE LOSS", false);
        TextButton back = game.getUiTheme().button("BACK");
        back.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showArenaSelect();
            }
        });
        actions.add(back).width(130f).height(40f).padRight(8f);
        root.add(actions).left().padTop(18f);
    }

    private void addLine(Table root, String text) {
        root.add(game.getUiTheme().label(text)).left().padBottom(8f).row();
    }

    private void addAction(Table actions, String title, boolean victory) {
        TextButton button = game.getUiTheme().button(title);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.showResult(new BattleOutcome(victory, character.getName(), arena.getName(),
                        victory ? 5 : 2, victory ? 31 : 11, victory ? 1 : 0, elapsedSeconds));
            }
        });
        actions.add(button).width(150f).height(40f).padRight(8f);
    }

    @Override
    public void render(float delta) {
        elapsedSeconds += delta;
        super.render(delta);
    }
}
