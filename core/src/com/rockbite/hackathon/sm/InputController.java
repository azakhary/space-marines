package com.rockbite.hackathon.sm;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.communications.commands.MinionAttack;
import com.rockbite.hackathon.sm.communications.commands.PlayCard;
import com.rockbite.hackathon.sm.components.CardComponent;
import com.rockbite.hackathon.sm.components.GameComponent;
import com.rockbite.hackathon.sm.components.HeroComponent;
import com.rockbite.hackathon.sm.components.MinionComponent;
import com.rockbite.hackathon.sm.components.render.TransformComponent;
import com.rockbite.hackathon.sm.systems.CardSystem;
import com.rockbite.hackathon.sm.systems.RenderSystem;

public class InputController {

    private Vector2 tmp = new Vector2();

    private Family cardTransforms = Family.all(TransformComponent.class, CardComponent.class).get();
    private Family minionTransforms = Family.all(TransformComponent.class, MinionComponent.class).get();
    private Family heroFamily = Family.all(TransformComponent.class, HeroComponent.class).get();

    private ComponentMapper<TransformComponent> tcMapper = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<CardComponent> ccMapper = ComponentMapper.getFor(CardComponent.class);
    private ComponentMapper<MinionComponent> mcMapper = ComponentMapper.getFor(MinionComponent.class);

    private boolean wasTouchDown = false;
    private Vector2 firstTouch = new Vector2();
    private Vector2 currTouch = new Vector2();

    private Entity draggingEntity = null;


    public void update(double deltaTime) {
        int currPlayerId = Comm.get().gameLogic.uniqueUserId;
        int opponentPlayerId = Comm.get().gameLogic.opponentUserId;

        if(Gdx.input.isTouched() && !wasTouchDown) {
            setTouchPos(firstTouch);
            // check cards
            Entity card = getCollision(cardTransforms);
            Entity minion = getCollision(minionTransforms);

            if(card != null) {
                draggingEntity = card;
            } else if (minion != null) {
                if(minion.getComponent(MinionComponent.class).user_id == currPlayerId) {
                    draggingEntity = minion;
                }
            }

            wasTouchDown = true;
        } else {
            if(Gdx.input.isTouched()) {
                // moving?
                setTouchPos(currTouch);

                if(draggingEntity != null) {
                    TransformComponent tc = tcMapper.get(draggingEntity);

                    tc.offsetX = currTouch.x - firstTouch.x;
                    tc.offsetY = currTouch.y - firstTouch.y;
                }
            } else {
                wasTouchDown = false;
                if(draggingEntity != null) {
                    TransformComponent tc = tcMapper.get(draggingEntity);

                    System.out.println("DROP");
                    // is this card or minion
                    CardComponent cardComponent = draggingEntity.getComponent(CardComponent.class);
                    if(cardComponent != null) {
                        // it's card
                        GameComponent game =Comm.get().gameLogic.gameEntity.getComponent(GameComponent.class);
                        int mana = (int) Math.floor(game.mana);
                        if(tc.y + tc.offsetY > -230f && cardComponent.cost <= mana) {
                            playCard();
                        } else {
                            tc.offsetX = 0;
                            tc.offsetY = 0;
                        }
                    } else if(draggingEntity.getComponent(MinionComponent.class) != null) {
                        // it's minion (that was dropped, but on what?)
                        Entity collidedMinion = getCollisionWithMinion(minionTransforms, currTouch, draggingEntity);
                        Entity collidedHero = getCollisionWithHero();


                        if(collidedMinion != null) {
                            System.out.println("Targeted a Minion");
                            playMinion(collidedMinion, false);

                            tc.offsetX = 0;
                            tc.offsetY = 0;
                        } else if(collidedHero != null) {
                            System.out.println("Targeted a HERO");
                            playMinion(collidedHero, true);
                            tc.offsetX = 0;
                            tc.offsetY = 0;
                        } else {
                            tc.offsetX = 0;
                            tc.offsetY = 0;
                        }
                    }

                }
                draggingEntity = null;

            }
        }
    }


    private void playCard() {
        //play it
        CardComponent cc = ccMapper.get(draggingEntity);
        PlayCard playCard = Comm.get().getCommand(PlayCard.class);
        playCard.setCardComponent(cc);
        Comm.get().executeCommand(playCard);

        // remove it
        int slot = playCard.getCardComponent().slot;
        Comm.get().gameLogic.getEngine().removeEntity(draggingEntity);
        // also shift slots
        Comm.get().gameLogic.getEngine().getSystem(CardSystem.class).shiftSlots(playCard.getCardComponent().playerId, slot);
    }

    private void playMinion(Entity targetEntity, boolean isHero) {
        if(!isHero) {
            // is minion
            MinionComponent mc = mcMapper.get(draggingEntity);
            if(mc.cooldown > 0) return;
            MinionAttack minionAttack = Comm.get().getCommand(MinionAttack.class);
            minionAttack.fromSlot = mc.slot;
            minionAttack.isHero = false;
            minionAttack.targetSlot = targetEntity.getComponent(MinionComponent.class).slot;
            Comm.get().executeCommand(minionAttack);
        } else {
            // is hero
            MinionComponent mc = mcMapper.get(draggingEntity);
            if(mc.cooldown > 0) return;
            HeroComponent hero = targetEntity.getComponent(HeroComponent.class);
            MinionAttack minionAttack = Comm.get().getCommand(MinionAttack.class);
            minionAttack.fromSlot = mc.slot;
            minionAttack.targetSlot = 0;
            minionAttack.isHero = true;
            Comm.get().executeCommand(minionAttack);
        }
    }

    private void setTouchPos(Vector2 vector) {
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();

        Viewport viewport = Comm.get().gameLogic.getEngine().getSystem(RenderSystem.class).viewport;

        viewport.unproject(vector.set(x, y));
    }

    private Entity getCollision(Family family) {
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();

        Viewport viewport = Comm.get().gameLogic.getEngine().getSystem(RenderSystem.class).viewport;

        viewport.unproject(tmp.set(x, y));

        ImmutableArray<Entity> entities = Comm.get().gameLogic.getEngine().getEntitiesFor(family);

        for (int i = 0; i < entities.size(); ++i) {
            TransformComponent tc = tcMapper.get(entities.get(i));
            if(tmp.x >= tc.x+tc.offsetX && tmp.y+tc.offsetY > tc.y && tmp.x <= tc.x+tc.offsetX+tc.width && tmp.y <= tc.y+tc.offsetY+tc.height) {
                // we've got a hit
                return entities.get(i);
            }
        }

        return null;
    }

    private Entity getCollisionWithMinion(Family family, Vector2 pos, Entity sourceMinion) {
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();

        Viewport viewport = Comm.get().gameLogic.getEngine().getSystem(RenderSystem.class).viewport;

        viewport.unproject(tmp.set(x, y));

        ImmutableArray<Entity> entities = Comm.get().gameLogic.getEngine().getEntitiesFor(family);

        for (int i = 0; i < entities.size(); ++i) {
            if(entities.get(i) == sourceMinion) continue;
            TransformComponent tc = tcMapper.get(entities.get(i));
            if(tmp.x >= tc.x+tc.offsetX && tmp.y+tc.offsetY > tc.y && tmp.x <= tc.x+tc.offsetX+tc.width && tmp.y <= tc.y+tc.offsetY+tc.height) {
                // we've got a hit
                return entities.get(i);
            }
        }

        return null;
    }


    private Entity getCollisionWithHero() {
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();

        Viewport viewport = Comm.get().gameLogic.getEngine().getSystem(RenderSystem.class).viewport;

        viewport.unproject(tmp.set(x, y));

        ImmutableArray<Entity> entities = Comm.get().gameLogic.getEngine().getEntitiesFor(heroFamily);

        for (int i = 0; i < entities.size(); ++i) {
            HeroComponent hero = entities.get(i).getComponent(HeroComponent.class);
            if(hero.user_id == Comm.get().gameLogic.opponentUserId) {
                TransformComponent tc = tcMapper.get(entities.get(i));
                if(tmp.x >= tc.x+tc.offsetX && tmp.y+tc.offsetY > tc.y && tmp.x <= tc.x+tc.offsetX+tc.width && tmp.y <= tc.y+tc.offsetY+tc.height) {
                    // we've got a hit
                    return entities.get(i);
                }
            }
        }

        return null;
    }
}
