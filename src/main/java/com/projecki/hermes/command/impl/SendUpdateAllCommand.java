package com.projecki.hermes.command.impl;

import com.projecki.hermes.Hermes;
import com.projecki.hermes.command.Command;

public class SendUpdateAllCommand extends Command {

    private final Hermes hermes;

    public SendUpdateAllCommand(Hermes hermes) {
        super("updateall", "Similar to 'update', sends update for all configs");
        this.hermes = hermes;
    }

    @Override
    public void run(String[] args) {
        hermes.sendUpdateAll();
        hermes.getLogger().info("Sent update for all loaded files.");
    }
}
