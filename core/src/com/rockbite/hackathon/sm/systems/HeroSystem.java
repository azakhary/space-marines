package com.rockbite.hackathon.sm.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Game;
import com.rockbite.hackathon.sm.communications.actions.HeroSync;
import com.rockbite.hackathon.sm.components.GameComponent;
import com.rockbite.hackathon.sm.components.HeroComponent;

public class HeroSystem extends EntitySystem {


    private ComponentMapper<HeroComponent> herMapper = ComponentMapper.getFor(HeroComponent.class);

    public void heroSync(HeroSync heroSync) {
        ImmutableArray<Entity> heroes = getEngine().getEntitiesFor(Family.all(HeroComponent.class).get());

        ImmutableArray<Entity> tmp = getEngine().getEntitiesFor(Family.all(GameComponent.class).get());
        Entity gameEntity = tmp.get(0);


        for (int i = 0; i < heroes.size(); ++i) {
            HeroComponent hero = herMapper.get(heroes.get(i));
            if(hero.user_id == heroSync.user_id) {

                hero.maxHP = heroSync.maxHP;
                hero.hp = heroSync.hp;

                gameEntity.getComponent(GameComponent.class).mana = heroSync.mana;

                return;
            }
        }
    }

    public Entity getFor(int uniqueUserId) {
        ImmutableArray<Entity> heroes = getEngine().getEntitiesFor(Family.all(HeroComponent.class).get());
        for (int i = 0; i < heroes.size(); ++i) {
            HeroComponent hero = herMapper.get(heroes.get(i));
            if (hero.user_id == uniqueUserId) {
                return heroes.get(i);
            }
        }
        return null;
    }
}
