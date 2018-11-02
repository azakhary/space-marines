package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rockbite.hackathon.sm.components.EmojiComponent;

public class EmojiSystem extends EntitySystem {

    private ImmutableArray<Entity> entities;

    private ComponentMapper<EmojiComponent> mainComponentMapper = ComponentMapper.getFor(EmojiComponent.class);


    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(EmojiComponent.class).get());
    }

    public void update(float deltaTime) {
        for (int i = 0; i < entities.size(); ++i) {

            EmojiComponent emojiComponent = mainComponentMapper.get(entities.get(i));

            if(emojiComponent.currShowTime < emojiComponent.maxShowTime) {
                emojiComponent.currShowTime += deltaTime;
            } else {
                emojiComponent.currShowTime = emojiComponent.maxShowTime;
                emojiComponent.action.setDoneDisplaying(true);
                getEngine().removeEntity(entities.get(i));
            }
        }
    }
}
