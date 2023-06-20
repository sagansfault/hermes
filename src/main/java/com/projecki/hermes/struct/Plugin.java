package com.projecki.hermes.struct;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Plugin {

    private final String uniqueName;
    private final Set<Config> configs;

    public Plugin(String uniqueName, Set<Config> configs) {
        this.uniqueName = uniqueName;
        this.configs = configs;
    }

    public Plugin(String uniqueName) {
        this.uniqueName = uniqueName;
        this.configs = new HashSet<>();
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public Set<Config> getConfigs() {
        return configs;
    }

    public void addConfigs(Collection<Config> configs) {
        this.configs.addAll(configs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plugin plugin = (Plugin) o;
        return uniqueName.equals(plugin.uniqueName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueName);
    }
}
