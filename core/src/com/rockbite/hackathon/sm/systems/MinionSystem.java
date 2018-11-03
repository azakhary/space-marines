package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.actions.MinionUpdate;
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
                    transform.x  =  - 200f - 60f;
                    transform.y = 0  - viewport.getWorldHeight()/2f + 20f + 360f;
                }
                if(minion.slot == 1) {
                    transform.x  = - 60f;
                    transform.y = 0  - viewport.getWorldHeight()/2f + 20f + 360f;
                }
                if(minion.slot == 2) {
                    transform.x  = + 200f - 60f;
                    transform.y = 0  - viewport.getWorldHeight()/2f + 20f + 360f;
                }
                if(minion.slot == 3) {
                    transform.x  = - 200f - 60f;
                    transform.y = 0  - viewport.getWorldHeight()/2f + 20f + 190f;
                }
                if(minion.slot == 4) {
                    transform.x  = + 200f - 60f;
                    transform.y = 0  - viewport.getWorldHeight()/2f + 20f + 190f;
                }

            } else {
                if(minion.slot == 0) {
                    transform.x  =  - 200f - 60f;
                    transform.y = 0  + viewport.getWorldHeight()/2f + 20f -450f;
                }
                if(minion.slot == 1) {
                    transform.x  = - 60f;
                    transform.y = 0  + viewport.getWorldHeight()/2f + 20f - 450f;
                }
                if(minion.slot == 2) {
                    transform.x  = + 200f - 60f;
                    transform.y = 0  + viewport.getWorldHeight()/2f + 20f - 450f;
                }
                if(minion.slot == 3) {
                    transform.x  = - 200f - 60f;
                    transform.y = 0  + viewport.getWorldHeight()/2f + 20f - 280f;
                }
                if(minion.slot == 4) {
                    transform.x  = + 200f - 60f;
                    transform.y = 0  + viewport.getWorldHeight()/2f + 20f - 258f;
                }
            }


        }
    }

    public void updateMinionData(MinionUpdate minionUpdate) {
        for (int i = 0; i < entities.size(); ++i) {
            MinionComponent minion = mainComponentMapper.get(entities.get(i));

            if(minion.user_id == minionUpdate.user_id && minion.slot == minionUpdate.slot_id) {
                if(minionUpdate.destroyed != true) {
                    minion.atk = minionUpdate.atk;
                    minion.hp = minionUpdate.hp;
                } else {
                    getEngine().removeEntity(entities.get(i));
                    shiftSlots(minionUpdate.user_id, minionUpdate.slot_id);
                }
            }
        }
    }


    public void shiftSlots(int user_id, int slot) {
        for (int i = 0; i < entities.size(); i++) {
            MinionComponent minion = mainComponentMapper.get(entities.get(i));
            if(minion.slot > slot && minion.user_id == user_id) {
                minion.slot--;
            }
        }
    }
}
