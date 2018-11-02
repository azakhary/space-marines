package com.rockbite.hackathon.sm.communications;

public interface Observer {

    public void registerActionChannels();

    public void onActionReceived(Action action);

}
