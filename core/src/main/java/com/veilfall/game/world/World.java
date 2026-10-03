package com.veilfall.game.world;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public abstract class World implements Disposable {
    private final Array<Model> models = new Array<>();
    private final Array<ModelInstance> instances = new Array<>();

    protected ModelInstance addModel(Model model, float x, float y, float z) {
        ModelInstance instance = new ModelInstance(model);
        instance.transform.setToTranslation(x, y, z);
        models.add(model);
        instances.add(instance);
        return instance;
    }

    public Iterable<ModelInstance> getInstances() {
        return instances;
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