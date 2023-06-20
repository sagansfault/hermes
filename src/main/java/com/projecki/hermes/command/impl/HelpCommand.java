package com.projecki.hermes.command.impl;

import com.projecki.hermes.Hermes;
import com.projecki.hermes.command.Command;

public class HelpCommand extends Command {

    private final Hermes hermes;

    public HelpCommand(Hermes hermes) {
        super("help", "Shows all available commands");
        this.hermes = hermes;
    }

    @Override
    public void run(String[] args) {
        System.out.println("Available Commands");
        for (Command command : hermes.getCommandManager().getCommands()) {
            System.out.println(" - " + command.getRoot() + " :: " + command.getDesc());
        }
    }
}
