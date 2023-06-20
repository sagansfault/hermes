package com.projecki.hermes.struct;

import com.projecki.fusion.config.redis.HermesConfig;
import com.projecki.hermes.Hermes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class Config {

    private final File file;
    private final String fileNameNoExtension;
    private String state;

    public Config(File file) {
        this.file = file;
        try {
            this.state = Files.readString(file.toPath());
        } catch (IOException e) {
            System.out.println("Could not read config file as string for state");
            this.state = "none";
            e.printStackTrace();
        }
        this.fileNameNoExtension = this.file.getName().substring(0, this.file.getName().lastIndexOf("."));
    }

    public File getFile() {
        return file;
    }

    public String getFileNameNoExtension() {
        return fileNameNoExtension;
    }

    public String getState() {
        return this.state;
    }

    public void sendUpdate(Hermes hermes, Organization organization, Plugin plugin) {
        this.sendUpdate(hermes, organization, plugin, true);
    }

    public void sendUpdate(Hermes hermes, Organization organization, Plugin plugin, boolean reload) {
        if (reload) {
            hermes.updateFiles();
            hermes.getLogger().info("Reloaded files.");
        }
        hermes.getRedisCommands()
                .set(organization.getId() + ":" + plugin.getUniqueName() + ":" + this.fileNameNoExtension + ":config:key", this.state)
                .thenRun(() -> {
                    hermes.getMessageClient().send(
                            organization.getId() + ":" + plugin.getUniqueName() + ":" + this.fileNameNoExtension + "-config-channel",
                            new HermesConfig.UpdateMessage(this.state)
                    );
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Config config = (Config) o;
        return file.equals(config.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}
