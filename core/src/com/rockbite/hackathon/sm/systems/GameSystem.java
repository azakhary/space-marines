package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rockbite.hackathon.sm.components.GameComponent;
import com.rockbite.hackathon.sm.components.render.DrawableComponent;
import com.rockbite.hackathon.sm.components.render.TransformComponent;

public class GameSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;

    private ComponentMapper<GameComponent> mainComponentMapper = ComponentMapper.getFor(GameComponent.class);
    private Entity manaEntity;

    public GameSystem() {

    }

    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(GameComponent.class).get());
    }

    public void update(float deltaTime) {
        for (int i = 0; i < entities.size(); ++i) {

            GameComponent gameComponent = mainComponentMapper.get(entities.get(i));

            gameComponent.mana += deltaTime * 0.1f;

            if(gameComponent.mana > gameComponent.maxMana) {
                gameComponent.mana = gameComponent.maxMana;
            }

            if(manaEntity != null) {
                manaEntity.getComponent(TransformComponent.class).width = (gameComponent.mana/gameComponent.maxMana) * 430f;
            }

            gameComponent.timePassed += deltaTime;
            if(gameComponent.timePassed > gameComponent.gameDuration) gameComponent.timePassed = gameComponent.gameDuration;

        }
    }

    public void setManaEntity(Entity manaEntity) {
        this.manaEntity = manaEntity;
    }
}

