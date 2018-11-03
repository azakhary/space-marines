package com.rockbite.hackathon.sm;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.components.render.DrawableComponent;
import com.rockbite.hackathon.sm.components.render.TransformComponent;

public class SpriteUtils {

    public static Entity createSprite(PooledEngine engine, String region, float x, float y, float width, float height, int index) {
        Entity entity = engine.createEntity();
        DrawableComponent drawableComponent = engine.createComponent(DrawableComponent.class);
        drawableComponent.sprite = new Sprite(Comm.get().gameLogic.getAssets().atlas.findRegion(region));
        drawableComponent.ninePatch = null;
        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.x = x;
        transformComponent.y = y;
        transformComponent.width = width;
        transformComponent.height = height;
        entity.add(drawableComponent);
        entity.add(transformComponent);
        engine.addEntity(entity);

        return entity;
    }

    public static Entity createNinePatch(PooledEngine engine, String region, float x, float y, float width, float height, float scl, int index) {
        Entity entity = engine.createEntity();
        DrawableComponent drawableComponent = engine.createComponent(DrawableComponent.class);
        TextureAtlas.AtlasRegion tReg = Comm.get().gameLogic.getAssets().atlas.findRegion(region);
        drawableComponent.ninePatch = new NinePatch(tReg, tReg.splits[0], tReg.splits[1], tReg.splits[2], tReg.splits[3]);
        drawableComponent.ninePatch.scale(scl, scl);
        drawableComponent.sprite = null;
        TransformComponent transformComponent = engine.createComponent(TransformComponent.class);
        transformComponent.x = x;
        transformComponent.y = y;
        transformComponent.width = width;
        transformComponent.height = height;
        entity.add(drawableComponent);
        entity.add(transformComponent);
        engine.addEntity(entity);

        return entity;
    }
}
