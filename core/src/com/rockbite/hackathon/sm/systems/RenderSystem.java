package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class RenderSystem extends EntitySystem {

    Viewport viewport;
    PolygonSpriteBatch batch;

    public RenderSystem() {
        viewport = new FitViewport(6f, 10f);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new PolygonSpriteBatch();
    }

    @Override
    public void update (float deltaTime) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.end();
    }

    public void removedFromEngine (Engine engine) {
        batch.dispose();
    }
}
