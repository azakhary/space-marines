package com.rockbite.hackathon.sm.communications;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.rockbite.hackathon.sm.communications.commands.SendEmoji;

import java.util.HashMap;

public class CommandPool {

    private HashMap<Class<? extends Command>, Pool> poolMap;

    public CommandPool(int initialCapacity) {
        poolMap = new HashMap<Class<? extends Command>, Pool>(initialCapacity);

        /**
         * REGISTERING ALL COMMANDS HERE
         */
        register(SendEmoji.class);

    }

    private <T extends Command> void register(final Class<T> clazz) {
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

    public <T extends Command> T obtain(Class<T> clazz) {
        return (T) poolMap.get(clazz).obtain();
    }

    public void free(Command command) {
        poolMap.get(command.getClass()).free(command);
    }
}
