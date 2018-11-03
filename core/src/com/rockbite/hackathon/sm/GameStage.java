package com.rockbite.hackathon.sm;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.rockbite.hackathon.sm.communications.Comm;
import com.rockbite.hackathon.sm.components.CardComponent;

public class GameStage extends Stage {

    Image pixel;

    Group cardDialog;
    Image cardBg;
    Image cardImage;
    Image textBg;
    Label titleLabel;
    Label descriptionLabel;

    Label atkLbl;
    Label hpLbl;
    Label costLbl;

    public GameStage() {
        getRoot().setTouchable(Touchable.disabled);
    }

    public void init() {
        pixel = new Image(Comm.get().gameLogic.getAssets().atlas.findRegion("solid-white"));
        pixel.setColor(new Color(Color.BLACK));
        pixel.getColor().a = 0;
        pixel.setSize(getViewport().getScreenWidth(), getViewport().getScreenHeight());
        addActor(pixel);

        cardDialog = new Group();
        cardBg = new Image(Comm.get().gameLogic.getAssets().atlas.findRegion("card-board"));
        cardBg.setPosition(-5, 110);
        cardBg.setSize(300f, 300*1.238f);
        cardImage = new Image(Comm.get().gameLogic.getAssets().atlas.findRegion("boris-card"));
        cardImage.setPosition(35, 140);
        cardImage.setSize(237f, 310f);
        textBg = new Image(Comm.get().gameLogic.getAssets().atlas.findRegion("text-bg"));
        textBg.setPosition(0, 0);
        textBg.setSize(300f, 139f);
        titleLabel = new Label("Boris the Animal", Comm.get().gameLogic.getAssets().labelStyle);
        titleLabel.setPosition(160 - titleLabel.getWidth()/2f, 120);
        descriptionLabel = new Label("This is Boris the animal, he is a very nice guy", Comm.get().gameLogic.getAssets().labelStyleSmall);
        descriptionLabel.setSize(260, 100);
        descriptionLabel.setPosition(160 - descriptionLabel.getWidth()/2f, 120 - descriptionLabel.getHeight());
        descriptionLabel.setWrap(true);

        atkLbl = new Label("1", Comm.get().gameLogic.getAssets().labelStyleSmall);
        atkLbl.setPosition(45, 172);
        hpLbl = new Label("1", Comm.get().gameLogic.getAssets().labelStyleSmall);
        hpLbl.setPosition(254, 172);
        costLbl = new Label("1", Comm.get().gameLogic.getAssets().labelStyleSmall);
        costLbl.setPosition(157, 468);


        cardDialog.addActor(cardImage);
        cardDialog.addActor(cardBg);
        cardDialog.addActor(textBg);
        cardDialog.addActor(titleLabel);
        cardDialog.addActor(descriptionLabel);
        cardDialog.addActor(atkLbl);
        cardDialog.addActor(hpLbl);
        cardDialog.addActor(costLbl);
        cardDialog.getColor().a = 0;

        cardDialog.setSize(300, 485);
        cardDialog.setOrigin(150, 242);
        cardDialog.setPosition(getViewport().getScreenWidth()/2f - cardDialog.getWidth()/2f, getViewport().getScreenHeight()/2f - cardDialog.getHeight()/2f);

        addActor(cardDialog);
    }

    public void showBlackPixel() {
        pixel.getColor().a = 0.0f;
        pixel.clearActions();
        pixel.addAction(Actions.alpha(0.5f, 0.2f));
    }

    public void hideBlackPixel() {
        pixel.getColor().a = 0.5f;
        pixel.clearActions();
        pixel.addAction(Actions.alpha(0.0f, 0.1f));
    }

    public void showCardDiallog(CardComponent card) {
        showBlackPixel();
        cardDialog.getColor().a = 0;
        cardDialog.clearActions();
        cardDialog.addAction(Actions.alpha(1f, 0.2f));

        ((TextureRegionDrawable)cardImage.getDrawable()).setRegion(Comm.get().gameLogic.getAssets().atlas.findRegion(card.id+"-card"));
        titleLabel.setText(card.title);
        descriptionLabel.setText(card.description);

        atkLbl.setText(card.minion.atk+"");
        hpLbl.setText(card.minion.hp+"");
        costLbl.setText(card.cost+"");
    }

    public void hideCardDiallog() {
        hideBlackPixel();
        cardDialog.getColor().a = 1;
        cardDialog.clearActions();
        cardDialog.addAction(Actions.alpha(0f, 0.1f));
    }
}
