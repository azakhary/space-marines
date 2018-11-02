package com.rockbite.hackathon.sm.communications;

import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.Queue;

import java.util.HashMap;


public class Comm {

    private static final Comm instance = new Comm();

    private HashMap<Class<? extends Action>, ObjectSet<Observer>> observerMap;

    private ActionPool actionPool;
    private CommandPool commandPool;

    private Queue<Action> actionQueue;

    private Comm() {
        observerMap = new HashMap<Class<? extends Action>, ObjectSet<Observer>>();
        actionQueue = new Queue<Action>();

        actionPool = new ActionPool(100);
        commandPool = new CommandPool(100);
    }

    public static Comm get() {
        return instance;
    }

    public <T extends Command> void executeCommand(T command) {
        command.execute();
    }

    public  <T extends Action> void sendAction(T action) {
        actionQueue.addLast(action);
    }

    private void tryToInvokeNextAction() {
        if(actionQueue.size == 0) return;
        /**
         * if it's not yet sent to observers, send it
         */
        if(!actionQueue.first().isDoneSending()) {
            Action action = actionQueue.first();
            for (Observer observer : observerMap.get(action.getClass()).iterator()) {
                observer.onActionReceived(action);
            }
            action.setDoneSending(true);
        }

        if(actionQueue.first().isDoneDisplaying()) {
            Action action = actionQueue.first();
            actionQueue.removeValue(action, true);
            actionPool.free(action);
        }
    }

    public void update() {
        tryToInvokeNextAction();
    }

    public <T extends Action> void registerObserver(Observer observer, Class<T> actionClass) {
        if(!observerMap.containsKey(actionClass)) {
            observerMap.put(actionClass, new ObjectSet<Observer>());
        }

        observerMap.get(actionClass).add(observer);
    }

    public void removeObserver(Observer observer) {

    }

    public <T extends Action> T getAction(Class<T> clazz) {
        return actionPool.obtain(clazz);
    }

    public <T extends Command> T getCommand(Class<T> clazz) {
        return commandPool.obtain(clazz);
    }
}
