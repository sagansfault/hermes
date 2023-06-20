package com.projecki.hermes.struct;

import com.projecki.fusion.config.redis.HermesConfig;
import com.projecki.hermes.Hermes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Organization {

    private final String id;
    private final Set<Plugin> plugins;

    public Organization(String id, Set<Plugin> plugins) {
        this.id = id;
        this.plugins = plugins;
    }

    public Organization(String id) {
        this(id, new HashSet<>());
    }

    public String getId() {
        return id;
    }

    public Set<Plugin> getPlugins() {
        return Collections.unmodifiableSet(this.plugins);
    }

    public void updateAll(Hermes hermes) {
        hermes.updateFiles();
        for (Organization organization : hermes.getOrganizations()) {
            for (Plugin plugin : organization.getPlugins()) {
                for (Config config : plugin.getConfigs()) {
                    hermes.getRedisCommands()
                            .set(organization.getId() + ":" + plugin.getUniqueName() + ":" +
                                    config.getFileNameNoExtension() + ":config:key", config.getState())
                            .thenRun(() -> {
                                hermes.getMessageClient().send(
                                        organization.getId() + ":" + plugin.getUniqueName() + ":" +
                                                config.getFileNameNoExtension() + "-config-channel",
                                        new HermesConfig.UpdateMessage(config.getState())
                                );
                            });
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization that = (Organization) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
