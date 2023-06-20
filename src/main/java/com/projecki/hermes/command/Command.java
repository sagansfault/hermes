package com.projecki.hermes.command;

import java.util.Objects;

public abstract class Command {

    private final String root;
    private final String desc;

    public Command(String root, String desc) {
        this.root = root;
        this.desc = desc;
    }

    public String getRoot() {
        return root;
    }

    public String getDesc() {
        return desc;
    }

    public abstract void run(String[] args);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return root.equalsIgnoreCase(command.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(root);
    }
}
