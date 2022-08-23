package de.digitaldevs.datatool.config;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the plugin config
 */
public class MySQLConfig {

    /**
     * The parent directory
     */
    private final File directory;

    /**
     * The config file
     */
    private final File configFile;

    /**
     * The bukkit configuration
     */
    private FileConfiguration configuration;

    /**
     * The mapping of config entries and their values
     *
     * @see Getter
     */
    @Getter
    private final Map<String, String> data;

    /**
     * Instantiates a new object of this class
     */
    public MySQLConfig() {
        this.directory = new File("plugins/DataTool");
        this.configFile = new File(directory, "config.yml");

        this.data = new HashMap<>();

        createDefault();
        cacheData();
    }

    /**
     * Creates a default config if it does not exist
     */
    @SneakyThrows
    private void createDefault() {
        // Creates the directory if not exists
        if (!directory.exists()) //noinspection ResultOfMethodCallIgnored
            directory.mkdirs();

        // Creates the config.yml file if not exists, set default values and saves it
        if (!configFile.exists()) { //noinspection ResultOfMethodCallIgnored
            configFile.createNewFile();

            configuration = YamlConfiguration.loadConfiguration(configFile);

            configuration.set("Database.host", "localhost");
            configuration.set("Database.port", 3306);
            configuration.set("Database.database", "test");
            configuration.set("Database.player-table", "data_tool_players");
            configuration.set("Database.server-table", "data_tool_server");
            configuration.set("Database.username", "root");
            configuration.set("Database.password", "");

            save();

        } else configuration = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Saves the config entries in a mapping of the entries and their values
     */
    private void cacheData() {
        final ConfigurationSection section = configuration.getConfigurationSection("Database");
        if (section == null) return; // If the section "Database" does not exist, no data can be mapped

        // Maps the data
        for (String key : section.getKeys(false)) data.put(key, configuration.getString("Database." + key));
    }

    /**
     * Saves the bukkit config
     * @see SneakyThrows
     */
    @SneakyThrows
    private void save() {
        this.configuration.save(configFile);
    }

}
