package com.projecki.hermes;

import com.projecki.fusion.config.local.LocalConfig;
import com.projecki.fusion.config.redis.HermesConfig;
import com.projecki.fusion.config.serialize.JacksonSerializer;
import com.projecki.fusion.message.MessageClient;
import com.projecki.fusion.message.redis.RedisMessageClient;
import com.projecki.fusion.util.Result;
import com.projecki.hermes.command.CommandManager;
import com.projecki.hermes.command.impl.HelpCommand;
import com.projecki.hermes.command.impl.ListCommand;
import com.projecki.hermes.command.impl.ReloadCommand;
import com.projecki.hermes.command.impl.SendUpdateAllCommand;
import com.projecki.hermes.config.RedisCredsConfig;
import com.projecki.hermes.struct.Config;
import com.projecki.hermes.struct.Organization;
import com.projecki.hermes.struct.Plugin;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.async.RedisAsyncCommands;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Hermes {

    private final Logger logger;

    private @Nullable File parent = null;
    private CommandManager commandManager;
    private Set<Organization> organizations;
    private RedisAsyncCommands<String, String> redisCommands;
    private MessageClient messageClient;

    public Hermes() {
        this.logger = LoggerFactory.getLogger("Hermes");
        logger.info("Starting...");

        Result<RedisClient, String> redisClientResult = connectRedis();

        redisClientResult.ifOkayOrElse(redisClient -> {
            this.redisCommands = redisClient.connect().async();
            this.messageClient = new RedisMessageClient(redisCommands, redisClient.connectPubSub().async());

            organizations = this.loadFiles();
            for (Organization organization : this.organizations) {
                for (Plugin plugin : organization.getPlugins()) {
                    for (Config config : plugin.getConfigs()) {
                        config.sendUpdate(this, organization, plugin, false);
                    }
                }
            }

            this.commandManager = new CommandManager();
            this.commandManager.register(new HelpCommand(this));
            this.commandManager.register(new SendUpdateAllCommand(this));
            this.commandManager.register(new ReloadCommand(this));
            this.commandManager.register(new ListCommand(this));
            logger.info("Running");
        }, e -> {
            this.logger.error(e);
            System.exit(0);
        });
    }

    private Result<RedisClient, String> connectRedis() {
        try {
            parent = new File(Hermes.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();
        } catch (URISyntaxException e) {
            logger.error("Could not load jar parent directory. Exiting");
            e.printStackTrace();
            System.exit(0);
        }

        File redisCredsFile = new File(parent, "redis.yml");
        if (!redisCredsFile.exists() || redisCredsFile.isDirectory()) {
            logger.error("No redis file found alongside hermes jar. Exiting");
            System.exit(0);
        }

        LocalConfig<RedisCredsConfig> redisConfigLoader = LocalConfig.fromSideBySideConfig(JacksonSerializer.ofYaml(RedisCredsConfig.class), Hermes.class, "redis.yml").orElse(null);
        if (redisConfigLoader == null) {
            return Result.error("Could not get config loader. Shutting down...");
        }
        RedisCredsConfig redisConfig;
        try {
            redisConfig = redisConfigLoader.loadConfig().get().orElse(null);
        } catch (InterruptedException | ExecutionException e) {
            return Result.error("Thread error. Shutting down...");
        }
        if (redisConfig == null) {
            return Result.error("Config not present. Shutting down...");
        }

        RedisClient redisClient = RedisClient.create(RedisURI.builder()
                .withHost(redisConfig.getHost())
                .withPort(Integer.parseInt(redisConfig.getPort()))
                .withPassword(redisConfig.getPassword())
                .build());

        return Result.ok(redisClient);
    }

    public Logger getLogger() {
        return logger;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public Set<Organization> getOrganizations() {
        return organizations;
    }

    public RedisAsyncCommands<String, String> getRedisCommands() {
        return redisCommands;
    }

    public MessageClient getMessageClient() {
        return this.messageClient;
    }

    public final Set<Organization> loadFiles() {
        Set<Organization> organizations = new HashSet<>();

        File configs = new File(parent, "configs");

        if (!configs.exists() || !configs.isDirectory()) {
            getLogger().warn("Config folder isn't a folder or doesnt exist");
            return organizations;
        }

        File[] organizationDirs = configs.listFiles();
        if (organizationDirs == null || organizationDirs.length == 0) {
            getLogger().warn("No organizations found in config folder");
            return organizations;
        }
        for (File orgDir : organizationDirs) {
            if (orgDir.exists() && orgDir.isDirectory()) {

                String orgName = orgDir.getName();
                File[] pluginFiles = orgDir.listFiles();
                if (pluginFiles == null) {
                    getLogger().warn("No plugin folders found");
                    return new HashSet<>();
                }

                Set<Plugin> plugins = new HashSet<>();
                for (File pluginFile : pluginFiles) {
                    if (pluginFile.exists() && pluginFile.isDirectory()) {

                        String pluginName  = pluginFile.getName();
                        File[] configFiles = pluginFile.listFiles();
                        if (configFiles == null) {
                            continue;
                        }
                        Set<Config> pluginConfigs = new HashSet<>();
                        for (File configFile : configFiles) {
                            if (!configFile.exists() || configFile.isDirectory()) {
                                continue;
                            }
                            pluginConfigs.add(new Config(configFile));
                        }
                        plugins.add(new Plugin(pluginName, pluginConfigs));
                    }
                }

                organizations.add(new Organization(orgName, plugins));
            }
        }

        return organizations;
    }

    public void updateFiles() {
        this.organizations.clear();
        this.organizations.addAll(loadFiles());
    }

    public void sendUpdateAll() {
        this.updateFiles();
        for (Organization organization : this.organizations) {
            for (Plugin plugin : organization.getPlugins()) {
                for (Config config : plugin.getConfigs()) {
                    redisCommands
                            .set(organization.getId() + ":" + plugin.getUniqueName() + ":" +
                                    config.getFileNameNoExtension() + ":config:key", config.getState())
                            .thenRun(() -> {
                                messageClient.send(
                                        organization.getId() + ":" + plugin.getUniqueName() + ":" +
                                                config.getFileNameNoExtension() + "-config-channel",
                                        new HermesConfig.UpdateMessage(config.getState())
                                );
                            });
                }
            }
        }
    }
}
