package com.veilfall.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.veilfall.game.rendering.Renderer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class VeilfallGame extends ApplicationAdapter {
	private Renderer renderer;

	@Override
	public void create() {
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
	}
}