package de.digitaldevs.datatool.listener;

import de.digitaldevs.database.mysql.MySQLHandler;
import de.digitaldevs.datatool.DataToolPlugin;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * This class represents the event fired when a player joins the server.
 *
 * @see Listener
 */
@AllArgsConstructor
public class ConnectionListener implements Listener {

    /**
     * The plugin instance
     *
     * @see AllArgsConstructor
     */
    private final DataToolPlugin plugin;

    /*
    The event logic
    The priority was set to LOWEST because in event hierarchy the lowest runs first
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        updatePlayerData(player);
    }

    /**
     * Updates the player specific data in the database asynchronously.
     *
     * @param player The joining player. Cannot be null
     */
    private void updatePlayerData(@NotNull final Player player) {
        final String username = player.getName();
        final String uuid = player.getUniqueId().toString(); // The uuid has to be of the type String in order to store it in the database correctly

        final MySQLHandler handler = plugin.getMySQLHandler(); // Get the instance of the MySQLHandler from the plugin instance
        final Map<String, String> data = plugin.getDatabaseConnectionData(); // Get the data mappings of the config entries

        /*
        Gets the table name from the data mapping of the config.
        If something went wrong with caching the data or the specific entry does not exist a default value is used instead to have a working table name
         */
        final String tableName = data.getOrDefault("player-table", "data_tool_players");

        // Saves to player data in the database
        handler.createBuilder("INSERT INTO " + tableName + " (uuid, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE uuid=?, name=?;")
                .addParameters(uuid, username, uuid, username)
                .updateAsync();
    }


}
