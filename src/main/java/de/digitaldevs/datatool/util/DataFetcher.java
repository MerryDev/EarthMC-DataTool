package de.digitaldevs.datatool.util;

import de.digitaldevs.database.mysql.MySQLHandler;
import de.digitaldevs.datatool.DataToolPlugin;
import de.digitaldevs.datatool.config.MySQLConfig;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * This class represents a tool for fetching statistics from the server.
 */
public class DataFetcher {

    /**
     * The plugin instance
     */
    private final DataToolPlugin plugin;

    /**
     * The instance of {@code MySQLHandler}
     */
    private final MySQLHandler handler;

    /**
     * A mapping of config entries and their values
     *
     * @see MySQLConfig#getData()
     */
    private final Map<String, String> data;

    /**
     * Instantiates a new object of this class
     *
     * @param plugin The plugin instance. Cannot be null
     */
    public DataFetcher(@NotNull DataToolPlugin plugin) {
        this.plugin = plugin;
        this.handler = plugin.getMySQLHandler();
        this.data = plugin.getDatabaseConnectionData();
    }

    /**
     * Fetches statistics from the server every minute asynchronously.
     */
    public void startFetching() {
        /*
        Get the table name for server statistics from config
        If something went wrong with caching the data or the specific entry does not exist a default value is used instead to have a working table name
         */
        final String tableName = data.getOrDefault("server-table", "data_tool_server");

        // Runs the code every minute
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, () -> {
            final int playerCount = getCurrentPlayerCount(); // Get the current player count

            // Saves the player count in the table
            handler.createBuilder("INSERT INTO " + tableName + " (id, player_count) VALUES (?, ?) ON DUPLICATE KEY UPDATE player_count=?;").addParameters(1, playerCount, playerCount).updateAsync();
        }, 0L, 20L * 60);
    }

    /**
     * Gets the current player count of the bukkit server.
     * @return The current player count
     */
    private int getCurrentPlayerCount() {
        return Bukkit.getOnlinePlayers().size();
    }

}
