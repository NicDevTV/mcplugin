package tv.nicdev.mcplugin

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class VanishManager(private val plugin: MCPlugin) {
    private val vanished: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    fun isVanished(player: Player): Boolean = vanished.contains(player.uniqueId)

    fun getVanishedOnlinePlayers(): List<Player> = vanished.mapNotNull { uuid -> plugin.server.getPlayer(uuid) }

    fun setVanished(player: Player, vanish: Boolean) {
        if (vanish) {
            if (vanished.add(player.uniqueId)) applyVanish(player)
        } else {
            if (vanished.remove(player.uniqueId)) removeVanish(player)
        }
        try { plugin.db.setVanish(player.uniqueId, vanish) } catch (_: Throwable) {}
    }

    private fun applyVanish(player: Player) {
        plugin.server.onlinePlayers.forEach { viewer -> if (!viewer.hasPermission("mcplugin.vanish.see")) viewer.hidePlayer(plugin, player) else viewer.showPlayer(plugin, player) }
        val prefix = "<gradient:#3AB0FF:#0052D4>[SYSTEM]</gradient>"
        player.sendMessage(plugin.render("$prefix <green>DEIN VANISH WURDE AKTIVIERT</green>"))
        notifyPlayers(plugin.render("$prefix <gold>${player.name} hat den Vanish betreten</gold>"), exclude = player.uniqueId)
    }

    private fun removeVanish(player: Player) {
        plugin.server.onlinePlayers.forEach { viewer -> viewer.showPlayer(plugin, player) }
        val prefix = "<gradient:#3AB0FF:#0052D4>[SYSTEM]</gradient>"
        player.sendMessage(plugin.render("$prefix <red>DEIN VANISH WURDE DEAKTIVIERT</red>"))
        notifyPlayers(plugin.render("$prefix <gold>${player.name} hat den Vanish verlassen</gold>"), exclude = player.uniqueId)
    }

    fun unapplyOnQuit(player: Player) {
        if (vanished.remove(player.uniqueId)) {
            plugin.server.onlinePlayers.forEach { viewer -> try { viewer.showPlayer(plugin, player) } catch (_: Throwable) {} }
            val prefix = "<gradient:#3AB0FF:#0052D4>[SYSTEM]</gradient>"
            notifyPlayers(plugin.render("$prefix <gold>${player.name} hat den Vanish verlassen</gold>"), exclude = player.uniqueId)
        }
    }

    fun restoreSavedVanishIfNeeded(player: Player) {
        try {
            val saved = plugin.db.getVanish(player.uniqueId)
            if (saved == true && player.hasPermission("mcplugin.command.vanish")) {
                if (!isVanished(player)) {
                    vanished.add(player.uniqueId)
                    plugin.server.onlinePlayers.forEach { viewer -> if (!viewer.hasPermission("mcplugin.vanish.see")) viewer.hidePlayer(plugin, player) else viewer.showPlayer(plugin, player) }
                    val prefix = "<gradient:#3AB0FF:#0052D4>[SYSTEM]</gradient>"
                    player.sendMessage(plugin.render("$prefix <green>Dein Vanish wurde wiederhergestellt</green>"))
                    notifyPlayers(plugin.render("$prefix <gold>${player.name} wurde beim Join wieder gevanisht</gold>"), exclude = player.uniqueId)
                }
            }
        } catch (_: Throwable) {}
    }

    fun toggleNotify(player: Player): Boolean {
        val id = player.uniqueId
        val current = plugin.db.getNotify(id)
        val newVal = !(current ?: true)
        plugin.db.setNotify(id, newVal)
        return newVal
    }

    fun isNotifyEnabled(player: Player): Boolean {
        if (!player.hasPermission("mcplugin.vanish.notify")) return false
        val valDb = plugin.db.getNotify(player.uniqueId)
        return valDb ?: true
    }

    private fun notifyPlayers(message: Component, exclude: UUID? = null) {
        plugin.server.onlinePlayers.forEach { viewer ->
            if (exclude != null && viewer.uniqueId == exclude) return@forEach
            if (!viewer.hasPermission("mcplugin.vanish.notify")) return@forEach
            if (!isNotifyEnabled(viewer)) return@forEach
            try { viewer.sendMessage(message) } catch (_: Throwable) {}
        }
    }

    @Suppress("DEPRECATION")
    fun sendActionBarToVanished() {
        val comp = plugin.render("<blue>DU BIST IM VANISH</blue>")
        getVanishedOnlinePlayers().forEach { player ->
            try { player.sendActionBar(comp) } catch (_: Throwable) {
                try {
                    val text = componentToLegacy(comp)
                    player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent(text))
                } catch (_: Throwable) {}
            }
        }
    }

    private fun componentToLegacy(component: Component): String {
        return try { net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(component) } catch (_: Throwable) { component.toString() }
    }
}
