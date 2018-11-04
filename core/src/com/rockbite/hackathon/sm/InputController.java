package com.rockbite.hackathon.sm;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
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
import com.rockbite.hackathon.sm.systems.MinionSystem;
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

    private boolean dialogOpened = false;


    public void update(double deltaTime) {
        int currPlayerId = Comm.get().gameLogic.uniqueUserId;
        int opponentPlayerId = Comm.get().gameLogic.opponentUserId;

        if(Gdx.input.isTouched() && !wasTouchDown) {

            if(dialogOpened) {
                Comm.get().gameLogic.stage.hideCardDiallog();
                dialogOpened = false;
            }

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
                    if(tc == null) return; // weird

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
                        // need to check where minion is dropped.
                        Integer dropSlotId = Comm.get().gameLogic.getEngine().getSystem(MinionSystem.class).hitPlayerSlot(tc);

                        if(dropSlotId != null && cardComponent.cost <= mana) {
                            playCard(dropSlotId);
                            tc.offsetX = 0;
                            tc.offsetY = 0;
                        } else {
                            tc.offsetX = 0;
                            tc.offsetY = 0;

                            // if it was just A CLICK
                            if(Math.sqrt((currTouch.x-firstTouch.x)*(currTouch.x-firstTouch.x)+(currTouch.y-firstTouch.y)*(currTouch.y-firstTouch.y)) < 10) {
                                System.out.println("card clicked");
                                Comm.get().gameLogic.stage.showCardDiallog(cardComponent);
                                dialogOpened = true;
                            }

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


    private void playCard(int slotId) {

        // check if this slot is empty
        boolean isSlotEmpty = Comm.get().gameLogic.getEngine().getSystem(MinionSystem.class).isSlotEmpty(slotId);
        if(!isSlotEmpty) return;

        //play it
        CardComponent cc = ccMapper.get(draggingEntity);
        PlayCard playCard = Comm.get().getCommand(PlayCard.class);
        playCard.targetSlot = slotId; // the slot we want to put this minion on
        playCard.targetPlayer = Comm.get().gameLogic.uniqueUserId; // todo: make this possible to target other player too
        playCard.targetType = 0; //todo: targeting minion, but make it possible to target hero as well.
        playCard.setCardComponent(cc);
        Comm.get().executeCommand(playCard);

        // do some fake hiding animation, which will come back if anything
        TransformComponent tc = draggingEntity.getComponent(TransformComponent.class);
        tc.initActorIfNotInited();
        tc.addAction(Actions.sequence(
                Actions.fadeOut(0.3f),
                Actions.delay(1.5f),
                Actions.fadeIn(0.1f)
        ));
        tc.tint.a = 0;
    }

    private void playMinion(Entity targetEntity, boolean isHero) {
        if(!isHero) {
            // is minion
            MinionComponent mc = mcMapper.get(draggingEntity);

            if(mc.user_id == targetEntity.getComponent(MinionComponent.class).user_id) {
                // trying to target friendly minion, this has to be ignored.
                return;
            }

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
