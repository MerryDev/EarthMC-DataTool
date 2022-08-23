package de.digitaldevs.datatool;

import de.digitaldevs.database.mysql.MySQLHandler;
import de.digitaldevs.datatool.config.MySQLConfig;
import de.digitaldevs.datatool.listener.ConnectionListener;
import de.digitaldevs.datatool.util.DataFetcher;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Map;

/**
 * This class represents the main class of the plugin.
 *
 * @see JavaPlugin
 */
@Getter
public class DataToolPlugin extends JavaPlugin {

    /**
     * The instance of the {@code MySQLHandler}
     *
     * @see Getter
     */
    private MySQLHandler mySQLHandler;

    /**
     * The mapping of the config entries and their values
     *
     * @see Getter
     */
    private Map<String, String> databaseConnectionData = Collections.emptyMap();

    // Plugin startup logic
    @Override
    public void onEnable() {
        setupDatabaseConnection(); // Initializes the database connection

        // Only if a valid connection is open, the tables should be created and the plugin starts successfully
        if (isConnected()) {
            createTables();

            // Register the used events
            final PluginManager pluginManager = Bukkit.getPluginManager();
            pluginManager.registerEvents(new ConnectionListener(this), this);

            // Start fetching statistics of the server
            new DataFetcher(this).startFetching();

            Bukkit.getConsoleSender().sendMessage("DataFetching-Tool has been started!");

        } else {
            Bukkit.getConsoleSender().sendMessage("Something went wrong here :(");
        }
    }

    // Plugin shutdown logic
    @Override
    public void onDisable() {
        // If an open connection is pending, close it
        if (isConnected()) this.mySQLHandler.closeConnection();
    }

    /**
     * Tries to open a connection to the database based on the connection data of the config.yml file
     */
    private void setupDatabaseConnection() {
        final MySQLConfig config = new MySQLConfig(); // Creates a new instance of the MySQLConfig
        final Map<String, String> data = config.getData(); // Get the mapping of its entries and their values

        /*
        Gets the connection data from the data mapping of the config.
        If something went wrong with caching the data or the specific entries do not exist a default value is used instead
         */
        final String host = data.getOrDefault("host", "localhost");
        final String port = data.getOrDefault("port", "3306");
        final String database = data.getOrDefault("database", "data-tool");
        final String username = data.getOrDefault("username", "root");
        final String password = data.getOrDefault("password", "");

        // Creates a new instance of the MySQLHandler with the connection data and no use of SSL
        this.mySQLHandler = new MySQLHandler(host, port, database, username, password, false);
        this.mySQLHandler.openConnection(); // Tries to open the connection. If it fails an SQLException will be thrown

        this.databaseConnectionData = data; // Initialize the mapping to access it outside og this class
    }

    /**
     * Checks if a database connection is open
     *
     * @return {@code true} if a connection is open. Otherwise {@code false}
     */
    private boolean isConnected() {
        return this.mySQLHandler.isConnected();
    }

    /**
     * Creates the tables used by this plugin if they do not exist synchronously
     */
    private void createTables() {
        final Map<String, String> data = this.databaseConnectionData; // Gets the mapping of the config entries and their values

        // If, for what reason, the mapping does not exist, the following operations should not be executed to avoid errors
        if (data == null) return;

        /*
        Gets the table names from the data mapping of the config.
        If something went wrong with caching the data or the specific entries do not exist a default value is used instead
         */
        final String serverTableName = data.getOrDefault("server-table", "data_tool_server");
        final String playerTableName = data.getOrDefault("player-table", "data_tool_players");

        // Creates the tables if they do not exist synchronously
        this.mySQLHandler.createBuilder("CREATE TABLE IF NOT EXISTS " + serverTableName + " (ID INT NOT NULL PRIMARY KEY, player_count INT NOT NULL);").updateSync();
        this.mySQLHandler.createBuilder("CREATE TABLE IF NOT EXISTS " + playerTableName + " (ID INT NOT NULL PRIMARY KEY AUTO_INCREMENT, uuid TEXT NOT NULL UNIQUE KEY, name VARCHAR(16) NOT NULL) ENGINE = INNODB;").updateSync();
    }
}
