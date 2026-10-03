package com.veilfall.game.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.veilfall.game.world.TestWorld;
import com.veilfall.game.world.World;

public final class Renderer implements com.badlogic.gdx.utils.Disposable {
    private final CameraController cameraController;
    private final Lighting lighting;
    private final ModelBatch modelBatch;
    private final World world;

    public Renderer(int screenWidth, int screenHeight) {
        cameraController = new CameraController(screenWidth, screenHeight);
        lighting = new Lighting();
        modelBatch = new ModelBatch();
        world = new TestWorld();
    }

    public CameraController getCameraController() {
        return cameraController;
    }

    public void render() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glClearColor(0.055f, 0.075f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cameraController.getCamera());
        for (ModelInstance instance : world.getInstances()) {
            modelBatch.render(instance, lighting.getEnvironment());
        }
        modelBatch.end();
    }

    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        Gdx.gl.glViewport(0, 0, width, height);
        cameraController.resize(width, height);
    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        world.dispose();
    }
}