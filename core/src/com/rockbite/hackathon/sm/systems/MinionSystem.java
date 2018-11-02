package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.components.CardComponent;
import com.rockbite.hackathon.sm.components.MinionComponent;
import com.rockbite.hackathon.sm.components.render.TransformComponent;

public class MinionSystem extends EntitySystem {


    private ImmutableArray<Entity> entities;

    private ComponentMapper<MinionComponent> mainComponentMapper = ComponentMapper.getFor(MinionComponent.class);
    private ComponentMapper<TransformComponent> tcMapper = ComponentMapper.getFor(TransformComponent.class);

    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(MinionComponent.class).get());
    }

    public void update(float deltaTime) {
        int currPlayerId = Comm.get().gameLogic.uniqueUserId;
        int opponentPlayerId = Comm.get().gameLogic.opponentUserId;


        for (int i = 0; i < entities.size(); ++i) {
            MinionComponent minion = mainComponentMapper.get(entities.get(i));
            TransformComponent transform = tcMapper.get(entities.get(i));

            Viewport viewport = Comm.get().gameLogic.getEngine().getSystem(RenderSystem.class).viewport;


            if(minion.user_id == currPlayerId) {
                // current player
                if(minion.slot == 0) {
                    transform.x  =  - 2f - 0.6f;
                    transform.y = 0  - viewport.getWorldHeight()/2f + 0.2f + 3.7f;
                }
                if(minion.slot == 1) {
                    transform.x  = - 0.6f;
                    transform.y = 0  - viewport.getWorldHeight()/2f + 0.2f + 3.7f;
                }
                if(minion.slot == 2) {
                    transform.x  = + 2f - 0.6f;
                    transform.y = 0  - viewport.getWorldHeight()/2f + 0.2f + 3.7f;
                }
                if(minion.slot == 3) {
                    transform.x  = - 2f - 0.6f;
                    transform.y = 0  - viewport.getWorldHeight()/2f + 0.2f + 2f;
                }
                if(minion.slot == 4) {
                    transform.x  = + 2f - 0.6f;
                    transform.y = 0  - viewport.getWorldHeight()/2f + 0.2f + 2f;
                }

            } else {
                if(minion.slot == 0) {
                    transform.x  =  - 2f - 0.6f;
                    transform.y = 0  + viewport.getWorldHeight()/2f + 0.2f - 3.7f;
                }
                if(minion.slot == 1) {
                    transform.x  = - 0.6f;
                    transform.y = 0  + viewport.getWorldHeight()/2f + 0.2f - 3.7f;
                }
                if(minion.slot == 2) {
                    transform.x  = + 2f - 0.6f;
                    transform.y = 0  + viewport.getWorldHeight()/2f + 0.2f - 3.7f;
                }
                if(minion.slot == 3) {
                    transform.x  = - 2f - 0.6f;
                    transform.y = 0  + viewport.getWorldHeight()/2f + 0.2f - 2f;
                }
                if(minion.slot == 4) {
                    transform.x  = + 2f - 0.6f;
                    transform.y = 0  + viewport.getWorldHeight()/2f + 0.2f - 2f;
                }
            }


        }
    }

}
