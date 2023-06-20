package com.projecki.hermes.command;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CommandManager {

    private final Set<Command> commands = new HashSet<>();

    public Optional<Command> getCommand(String base) {
        for (Command command : this.commands) {
            if (command.getRoot().equalsIgnoreCase(base)) {
                return Optional.of(command);
            }
        }
        return Optional.empty();
    }

    public Set<Command> getCommands() {
        return new HashSet<>(this.commands);
    }

    public void register(Command command) {
        this.commands.add(command);
    }
}
