package tv.nicdev.mcplugin.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import tv.nicdev.mcplugin.MCPlugin

class VanishListener(private val plugin: MCPlugin) : Listener {

    @Suppress("DEPRECATION")
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        val vm = plugin.vanishManager

        vm.restoreSavedVanishIfNeeded(player)

        if (vm.isVanished(player)) {
            e.joinMessage = null
        }

        plugin.server.onlinePlayers.forEach { other ->
            if (vm.isVanished(other) && !player.hasPermission("mcplugin.vanish.see")) {
                try { player.hidePlayer(plugin, other) } catch (_: Throwable) {}
            }
        }

        if (vm.isVanished(player)) {
            plugin.server.onlinePlayers.forEach { viewer ->
                if (!viewer.hasPermission("mcplugin.vanish.see")) {
                    try { viewer.hidePlayer(plugin, player) } catch (_: Throwable) {}
                } else {
                    try { viewer.showPlayer(plugin, player) } catch (_: Throwable) {}
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        val vm = plugin.vanishManager

        if (vm.isVanished(player)) {
            e.quitMessage = null
        }

        plugin.vanishManager.unapplyOnQuit(player)
    }

    @EventHandler
    fun onEntityPickup(e: EntityPickupItemEvent) {
        val entity = e.entity
        if (entity !is org.bukkit.entity.Player) return
        val player = entity
        if (plugin.vanishManager.isVanished(player) && !player.isSneaking) {
            e.isCancelled = true
        }
    }
}
