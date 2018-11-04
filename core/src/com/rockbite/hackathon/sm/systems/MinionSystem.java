package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.utils.Array;
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

    public Array<Entity> playerSlots = new Array<Entity>();
    public Array<Entity> opponentSlots = new Array<Entity>();

    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(MinionComponent.class).get());
    }

    public void createSlots() {
        Viewport viewport = Comm.get().gameLogic.getEngine().getSystem(RenderSystem.class).viewport;

        createSlot(-200f - 60f,   - viewport.getWorldHeight() / 2f + 20f + 360f, playerSlots);
        createSlot(-60f,          - viewport.getWorldHeight() / 2f + 20f + 360f, playerSlots);
        createSlot(200f - 60f,    - viewport.getWorldHeight() / 2f + 20f + 360f, playerSlots);
        createSlot(-200f - 60f,   - viewport.getWorldHeight() / 2f + 20f + 190f, playerSlots);
        createSlot(200f - 60f,    - viewport.getWorldHeight() / 2f + 20f + 190f, playerSlots);

        createSlot(-200f - 60f,   viewport.getWorldHeight() / 2f + 20f - 450f, opponentSlots);
        createSlot(-60f,          viewport.getWorldHeight() / 2f + 20f - 450f, opponentSlots);
        createSlot(200f - 60f,    viewport.getWorldHeight() / 2f + 20f - 450f, opponentSlots);
        createSlot(-200f - 60f,   viewport.getWorldHeight() / 2f + 20f - 280f, opponentSlots);
        createSlot(200f - 60f,    viewport.getWorldHeight() / 2f + 20f - 280f, opponentSlots);
    }

    public void createSlot(float x, float y, Array<Entity> array) {
        Entity entity = new Entity();
        TransformComponent transform = new TransformComponent();
        transform.x = x;
        transform.y = y;
        transform.width = 120f;
        transform.height = 120f * 1.31f;
        entity.add(transform);
        getEngine().addEntity(entity);
        array.add(entity);
    }

    public void positionMinion(TransformComponent transform, MinionComponent minion) {
        int currPlayerId = Comm.get().gameLogic.uniqueUserId;
        int opponentPlayerId = Comm.get().gameLogic.opponentUserId;

        if(!transform.actor.hasActions()) {
            if (minion.user_id == currPlayerId) {
                transform.x = playerSlots.get(minion.slot).getComponent(TransformComponent.class).x;
                transform.y = playerSlots.get(minion.slot).getComponent(TransformComponent.class).y;
            } else {
                transform.x = opponentSlots.get(minion.slot).getComponent(TransformComponent.class).x;
                transform.y = opponentSlots.get(minion.slot).getComponent(TransformComponent.class).y;
            }
        }
    }

    public void update(float deltaTime) {
        int currPlayerId = Comm.get().gameLogic.uniqueUserId;
        int opponentPlayerId = Comm.get().gameLogic.opponentUserId;


        for (int i = 0; i < entities.size(); ++i) {
            MinionComponent minion = mainComponentMapper.get(entities.get(i));
            TransformComponent transform = tcMapper.get(entities.get(i));

            Viewport viewport = Comm.get().gameLogic.getEngine().getSystem(RenderSystem.class).viewport;


            //positionMinion(transform, minion);


            minion.cooldown -= deltaTime;
            if( minion.cooldown < 0)  minion.cooldown = 0;
            float cooldownAlpha = (Comm.get().gameLogic.MAX_COOLDOWN -  minion.cooldown)/Comm.get().gameLogic.MAX_COOLDOWN;

            // cooldown tint
            transform.tint.r = cooldownAlpha;
            transform.tint.g = cooldownAlpha;
            transform.tint.b = cooldownAlpha;
            transform.tint.a = 0.5f + cooldownAlpha/2f;

        }
    }

    public void updateMinionData(MinionUpdate minionUpdate) {
        for (int i = 0; i < entities.size(); ++i) {
            MinionComponent minion = mainComponentMapper.get(entities.get(i));

            if(minion.user_id == minionUpdate.user_id && minion.slot == minionUpdate.slot_id) {
                if(minionUpdate.destroyed != true) {
                    minion.atk = minionUpdate.atk;
                    minion.hp = minionUpdate.hp;
                    minion.maxHP = minionUpdate.maxHP;
                    minion.maxATK = minionUpdate.maxATK;
                    minion.cooldown = minionUpdate.cooldown;
                } else {
                    getEngine().removeEntity(entities.get(i));
                    //todo: probably should do it more good looking and with actions
                }
            }
        }
    }


    public void shiftSlots(int user_id, int slot) {
        /*
        for (int i = 0; i < entities.size(); i++) {
            MinionComponent minion = mainComponentMapper.get(entities.get(i));
            if(minion.slot > slot && minion.user_id == user_id) {
                minion.slot--;
            }
        }*/
    }

    public Entity getFor(int user_id, int slot) {

        for (int i = 0; i < entities.size(); ++i) {
            MinionComponent minion = mainComponentMapper.get(entities.get(i));
            if(minion.user_id == user_id && minion.slot == slot) {
                return entities.get(i);
            }
        }
        return null;
    }

    public Integer hitPlayerSlot(TransformComponent tc) {
        // not comparing with opponent slots here.
        float maxIntersectArea = 0;
        int bestInteresectingSlot = -1;

        for(int i = 0; i < playerSlots.size; i++) {
            float area = tc.hit(playerSlots.get(i).getComponent(TransformComponent.class));
            if(area > 10) {
                if(maxIntersectArea < area) {
                    maxIntersectArea = area;
                    bestInteresectingSlot = i;
                }
            }
        }

        if(bestInteresectingSlot >= 0) {
            return bestInteresectingSlot;
        }

        return null;
    }

    public boolean isSlotEmpty(int slotId) {
        for (int i = 0; i < entities.size(); i++) {
            MinionComponent minion = mainComponentMapper.get(entities.get(i));
            if(minion.slot == slotId && minion.user_id == Comm.get().gameLogic.uniqueUserId) {
                return false;
            }
        }

        return true;
    }
}
