package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.components.CardComponent;
import com.rockbite.hackathon.sm.components.render.TransformComponent;

public class CardSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;

    private ComponentMapper<CardComponent> mainComponentMapper = ComponentMapper.getFor(CardComponent.class);
    private ComponentMapper<TransformComponent> tcMapper = ComponentMapper.getFor(TransformComponent.class);

    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(CardComponent.class).get());
    }

    public void update(float deltaTime) {
        for (int i = 0; i < entities.size(); i++) {
            CardComponent card = mainComponentMapper.get(entities.get(i));
            TransformComponent transform = tcMapper.get(entities.get(i));

            Viewport viewport = Comm.get().gameLogic.getEngine().getSystem(RenderSystem.class).viewport;

            transform.x  = (card.slot * 1.1f) - viewport.getWorldWidth()/2f + 0.2f;
            transform.y = 0  - viewport.getWorldHeight()/2f + 0.2f;
        }
    }

    public void shiftSlots(int slot) {
        for (int i = 0; i < entities.size(); i++) {
            CardComponent card = mainComponentMapper.get(entities.get(i));
            if(card.slot > slot) {
                card.slot--;
            }
        }
    }
}

