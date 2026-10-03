package com.veilfall.game.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public final class TestWorld extends World {
    private static final long VERTEX_ATTRIBUTES = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal;

    public TestWorld() {
        ModelBuilder modelBuilder = new ModelBuilder();

        addBox(modelBuilder, 34f, 0.4f, 34f, color(0.19f, 0.28f, 0.27f), 0f, -0.2f, 0f);

        addBuilding(modelBuilder, 4f, 2.8f, 3.5f, color(0.39f, 0.43f, 0.45f), -6f, 1.4f, -5f);
        addBuilding(modelBuilder, 3.2f, 4.8f, 3.2f, color(0.48f, 0.39f, 0.34f), -1f, 2.4f, -5.5f);
        addBuilding(modelBuilder, 4.2f, 3.4f, 3.8f, color(0.31f, 0.39f, 0.44f), 5f, 1.7f, -4f);
        addBuilding(modelBuilder, 3.4f, 5.4f, 3.4f, color(0.43f, 0.44f, 0.39f), -5f, 2.7f, 2.5f);
        addBuilding(modelBuilder, 4.4f, 2.2f, 3.6f, color(0.50f, 0.42f, 0.34f), 1f, 1.1f, 2f);
        addBuilding(modelBuilder, 3.2f, 4f, 3.2f, color(0.36f, 0.40f, 0.47f), 6f, 2f, 4.5f);

        addBox(modelBuilder, 1f, 1.6f, 1f, color(0.57f, 0.48f, 0.34f), -9f, 0.8f, 6f);
        addBox(modelBuilder, 1.2f, 2.2f, 1.2f, color(0.46f, 0.51f, 0.43f), 9f, 1.1f, -8f);
        addBox(modelBuilder, 1.5f, 0.7f, 1.5f, color(0.58f, 0.40f, 0.34f), 8f, 0.35f, 7f);
        addBox(modelBuilder, 1f, 1.1f, 1f, color(0.40f, 0.52f, 0.51f), -9f, 0.55f, -9f);
    }

    private void addBuilding(ModelBuilder modelBuilder, float width, float height, float depth,
                             Color wallColor, float x, float centerY, float z) {
        addBox(modelBuilder, width, height, depth, wallColor, x, centerY, z);
        addBox(modelBuilder, width + 0.28f, 0.28f, depth + 0.28f,
                color(0.27f, 0.30f, 0.33f), x, centerY + height * 0.5f + 0.14f, z);
    }

    private void addBox(ModelBuilder modelBuilder, float width, float height, float depth,
                        Color diffuse, float x, float y, float z) {
        Material material = new Material(
                ColorAttribute.createDiffuse(diffuse),
                FloatAttribute.createShininess(8f));
        addModel(modelBuilder.createBox(width, height, depth, material, VERTEX_ATTRIBUTES), x, y, z);
    }

    private static Color color(float red, float green, float blue) {
        return new Color(red, green, blue, 1f);
    }
}