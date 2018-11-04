package com.rockbite.hackathon.sm.components.render;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.rockbite.hackathon.sm.communications.Comm;

public class TransformComponent implements Component{
    public float x;
    public float y;
    public float width;
    public float height;

    public float offsetX;
    public float offsetY;

    public Color tint = new Color(Color.WHITE);

    public Actor actor = new Actor();
    public float scaleX;
    public float scaleY;
    public float rotation;
    public float alpha;

    public void reset() {
        offsetX = 0;
        offsetY = 0;
        scaleX =1;
        scaleY = 1;
        rotation = 0;
        alpha = 1f;

    }

    public void set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width =width;
        this.height=height;
    }

    public void addAction(Action action) {
        actor.setX(x);
        actor.setY(y);
        actor.getColor().a = alpha;
        actor.addAction(action);
    }

    public void initActorIfNotInited() {
        actor.setVisible(false);
        Comm.get().gameLogic.stage.addActor(actor);
    }

    public float hit(TransformComponent component) {
        Rectangle rect1 = new Rectangle(x+offsetX, y+offsetY, width, height);
        Rectangle rect2 = new Rectangle(component.x+component.offsetX, component.y+component.offsetY, component.width, component.height);
        Rectangle result = new Rectangle();
        if(Intersector.intersectRectangles(rect1, rect2, result)) {
            return result.area();
        }
        return 0;
    }
}
