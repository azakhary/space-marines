package com.rockbite.hackathon.sm.communications;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.rockbite.hackathon.sm.communications.actions.CardDrawn;
import com.rockbite.hackathon.sm.communications.actions.EmojiShown;
import com.rockbite.hackathon.sm.communications.actions.HeroSync;
import com.rockbite.hackathon.sm.communications.actions.MinionAttackAnim;
import com.rockbite.hackathon.sm.communications.actions.MinionUpdate;
import com.rockbite.hackathon.sm.communications.actions.SummonMinion;

import java.util.HashMap;

public class ActionPool {

    private HashMap<Class<? extends Action>, Pool> poolMap;

    public ActionPool(int initialCapacity) {
        poolMap = new HashMap<Class<? extends Action>, Pool>(initialCapacity);

        /**
         * REGISTERING ALL COMMANDS HERE
         */
        register(EmojiShown.class);
        register(CardDrawn.class);
        register(MinionUpdate.class);
        register(HeroSync.class);
        register(MinionAttackAnim.class);
        register(SummonMinion.class);
    }

    private <T extends Action> void register(final Class<T> clazz) {
        Pool pool = new Pool<T>() {
            @Override
            protected T newObject() {
                try {
                    return ClassReflection.newInstance(clazz);
                } catch (ReflectionException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
        poolMap.put(clazz, pool);
    }

    public <T extends Action> T obtain(Class<T> clazz) {
        return (T) poolMap.get(clazz).obtain();
    }

    public void free(Action action) {
        poolMap.get(action.getClass()).free(action);
    }
}