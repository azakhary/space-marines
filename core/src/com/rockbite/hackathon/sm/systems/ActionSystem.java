package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rockbite.hackathon.sm.components.GameComponent;
import com.rockbite.hackathon.sm.components.render.TransformComponent;

public class ActionSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;

    private ComponentMapper<TransformComponent> mainComponentMapper = ComponentMapper.getFor(TransformComponent.class);


    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class).get());
    }

    public void update(float deltaTime) {
        for (int i = 0; i < entities.size(); ++i) {
            TransformComponent transform = mainComponentMapper.get(entities.get(i));

            if(transform.actor.hasActions()) {
                transform.x = transform.actor.getX();
                transform.y = transform.actor.getY();
                transform.scaleX = transform.actor.getScaleX();
                transform.scaleY = transform.actor.getScaleY();
                transform.rotation = transform.actor.getRotation();
                transform.alpha = transform.actor.getColor().a;
            }
        }
    }
}
