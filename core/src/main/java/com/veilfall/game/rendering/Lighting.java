package com.veilfall.game.rendering;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

public final class Lighting {
    private final Environment environment = new Environment();

    public Lighting() {
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.48f, 0.51f, 0.56f, 1f));
        environment.add(new DirectionalLight().set(0.92f, 0.84f, 0.70f, -0.5f, -1f, -0.35f));
    }

    public Environment getEnvironment() {
        return environment;
    }
}