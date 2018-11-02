package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rockbite.hackathon.sm.components.GameComponent;

public class GameSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;

    private ComponentMapper<GameComponent> mainComponentMapper = ComponentMapper.getFor(GameComponent.class);

    public GameSystem() {

    }

    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(GameComponent.class).get());
    }

    public void update(float deltaTime) {
        for (int i = 0; i < entities.size(); ++i) {

            //GameComponent gameComponent = mainComponentMapper.get(entities.get(i));

        }
    }
}

