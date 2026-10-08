package com.veilfall.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.veilfall.game.data.ArenaData;
import com.veilfall.game.data.CharacterData;
import com.veilfall.game.database.repository.ArenaRepository;
import com.veilfall.game.database.repository.CharacterRepository;
import com.veilfall.game.database.repository.MobRepository;
import com.veilfall.game.database.repository.PlayerRepository;
import com.veilfall.game.database.repository.SkillRepository;
import com.veilfall.game.database.repository.WeaponRepository;
import com.veilfall.game.database.DatabaseInitializer;
import com.veilfall.game.database.DatabaseManager;
import com.veilfall.game.rendering.Renderer;
import com.veilfall.game.screens.ArenaSelectScreen;
import com.veilfall.game.screens.BattleOutcome;
import com.veilfall.game.screens.BattleScreen;
import com.veilfall.game.screens.LobbyScreen;
import com.veilfall.game.screens.LoaderScreen;
import com.veilfall.game.screens.ResultScreen;
import com.veilfall.game.screens.UiTheme;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class VeilfallGame extends Game {
	private Renderer renderer;
	private DatabaseManager databaseManager;
	private UiTheme uiTheme;
	private PlayerRepository playerRepository;
	private SkillRepository skillRepository;
	private CharacterRepository characterRepository;
	private WeaponRepository weaponRepository;
	private MobRepository mobRepository;
	private ArenaRepository arenaRepository;
	private CharacterData selectedCharacter;

	@Override
	public void create() {
		try {
			databaseManager = new DatabaseManager();
			new DatabaseInitializer(databaseManager).initialize();
		} catch (SQLException | IOException exception) {
			if (databaseManager != null) {
				try {
					databaseManager.close();
				} catch (SQLException closeException) {
					exception.addSuppressed(closeException);
				}
			}
			throw new GdxRuntimeException("Failed to initialize the local Veilfall database", exception);
		}

		renderer = new Renderer(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		playerRepository = new PlayerRepository(databaseManager);
		skillRepository = new SkillRepository(databaseManager);
		characterRepository = new CharacterRepository(databaseManager);
		weaponRepository = new WeaponRepository(databaseManager);
		mobRepository = new MobRepository(databaseManager);
		arenaRepository = new ArenaRepository(databaseManager);
		uiTheme = new UiTheme();
		setScreen(new LoaderScreen(this));
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		if (renderer != null) renderer.resize(width, height);
	}

	public Renderer getRenderer() { return renderer; }
	public UiTheme getUiTheme() { return uiTheme; }
	public PlayerRepository getPlayerRepository() { return playerRepository; }
	public SkillRepository getSkillRepository() { return skillRepository; }
	public CharacterRepository getCharacterRepository() { return characterRepository; }
	public WeaponRepository getWeaponRepository() { return weaponRepository; }
	public MobRepository getMobRepository() { return mobRepository; }
	public ArenaRepository getArenaRepository() { return arenaRepository; }
	public CharacterData getSelectedCharacter() { return selectedCharacter; }

	public void setSelectedCharacter(CharacterData character) {
		selectedCharacter = character;
	}

	public void showLobby() {
		transitionTo(new LobbyScreen(this));
	}

	public void showArenaSelect() {
		transitionTo(new ArenaSelectScreen(this));
	}

	public void startBattle(ArenaData arena) {
		try {
			selectedCharacter = characterRepository.getAll().stream()
					.filter(character -> character.getName().equals("Warden"))
					.findFirst()
					.orElseThrow(() -> new SQLException("Warden character definition is missing"));
		} catch (SQLException exception) {
			throw new GdxRuntimeException("Failed to load Warden from SQLite", exception);
		}
		Objects.requireNonNull(arena, "Selected arena must not be null");
		transitionTo(new BattleScreen(this, arena, selectedCharacter));
	}

	public void showResult(BattleOutcome outcome) {
		transitionTo(new ResultScreen(this, outcome));
	}

	private void transitionTo(Screen nextScreen) {
		Screen previousScreen = getScreen();
		setScreen(nextScreen);
		if (previousScreen != null) {
			previousScreen.dispose();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (renderer != null) renderer.dispose();
		if (uiTheme != null) uiTheme.dispose();
		if (databaseManager != null) {
			try {
				databaseManager.close();
			} catch (SQLException exception) {
				Gdx.app.error("VeilfallGame", "Failed to close the local Veilfall database", exception);
			}
		}
	}
}