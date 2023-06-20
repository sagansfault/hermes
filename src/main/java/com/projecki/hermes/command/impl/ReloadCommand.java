package com.projecki.hermes.command.impl;

import com.projecki.hermes.Hermes;
import com.projecki.hermes.command.Command;

public class ReloadCommand extends Command {

    private final Hermes hermes;

    public ReloadCommand(Hermes hermes) {
        super("reload", "Loads all file/folder mutations. Run this when you remove or add a file/folder");
        this.hermes = hermes;
    }

    @Override
    public void run(String[] args) {
        hermes.updateFiles();
        hermes.getLogger().info("Reloaded files.");
    }
}
