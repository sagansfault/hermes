package com.projecki.hermes.command.impl;

import com.projecki.hermes.Hermes;
import com.projecki.hermes.command.Command;
import com.projecki.hermes.struct.Organization;
import com.projecki.hermes.struct.Plugin;

import java.util.stream.Collectors;

public class ListCommand extends Command {

    private final Hermes hermes;

    public ListCommand(Hermes hermes) {
        super("list", "Lists all current plugins and their configs");
        this.hermes = hermes;
    }

    @Override
    public void run(String[] args) {
        for (Organization organization : hermes.getOrganizations()) {
            for (Plugin plugin : organization.getPlugins()) {
                System.out.println(organization.getId() + ": " + plugin.getUniqueName() + " - [" +
                        plugin.getConfigs().stream().map(c -> c.getFile().getName()).collect(Collectors.joining(", ")) +
                        "]");
            }
        }
    }
}
