package com.veilfall.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.veilfall.game.database.DatabaseInitializer;
import com.veilfall.game.database.DatabaseManager;
import com.veilfall.game.rendering.Renderer;

import java.io.IOException;
import java.sql.SQLException;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class VeilfallGame extends ApplicationAdapter {
	private Renderer renderer;
	private DatabaseManager databaseManager;

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
		Gdx.input.setInputProcessor(renderer.getCameraController());
	}

	@Override
	public void render() {
		renderer.render();
	}

	@Override
	public void resize(int width, int height) {
		if (renderer != null) {
			renderer.resize(width, height);
		}
	}

	@Override
	public void dispose() {
		if (renderer != null) {
			if (Gdx.input.getInputProcessor() == renderer.getCameraController()) {
				Gdx.input.setInputProcessor(null);
			}
			renderer.dispose();
		}
		if (databaseManager != null) {
			try {
				databaseManager.close();
			} catch (SQLException exception) {
				Gdx.app.error("VeilfallGame", "Failed to close the local Veilfall database", exception);
			}
		}
	}
}