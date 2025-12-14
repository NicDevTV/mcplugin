package tv.nicdev.mcplugin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

import tv.nicdev.mcplugin.listener.VanishListener
import tv.nicdev.mcplugin.command.VanishCommand

class MCPlugin : JavaPlugin(), Listener {

    private val miniMessage: MiniMessage? = try {
        MiniMessage.miniMessage()
    } catch (t: Throwable) {
        logger.warning("MiniMessage nicht gefunden auf dem Server, benutze einfachen Text-Fallback")
        null
    }

    lateinit var db: DatabaseManager
    val vanishManager: VanishManager by lazy { VanishManager(this) }

    private val pingCooldowns: MutableMap<UUID, Long> = ConcurrentHashMap()
    private val PING_COOLDOWN_MS = 5_000L

    private var actionBarTaskId: Int = -1

    override fun onEnable() {
        logger.info("MCPlugin enabled")
        db = DatabaseManager(dataFolder)

        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(VanishListener(this), this)

        val cmd = getCommand("vanish")
        if (cmd != null) {
            val handler = VanishCommand(this)
            cmd.setExecutor(handler)
            cmd.tabCompleter = handler
            cmd.setAliases(listOf("v"))
        } else {
            logger.warning("Command /vanish ist nicht in plugin.yml registriert")
        }

        actionBarTaskId = server.scheduler.scheduleSyncRepeatingTask(this, {
            vanishManager.sendActionBarToVanished()
        }, 20L, 20L)
    }

    override fun onDisable() {
        logger.info("MCPlugin disabled")
        if (actionBarTaskId != -1) server.scheduler.cancelTask(actionBarTaskId)

        try { db.close() } catch (_: Throwable) {}

        vanishManager.getVanishedOnlinePlayers().forEach { p ->
            try { p.resetPlayerTime(); p.resetPlayerWeather(); server.onlinePlayers.forEach { viewer -> viewer.showPlayer(this, p) } } catch (_: Throwable) {}
        }
    }

    fun render(message: String, vararg placeholders: Pair<String, String>): Component {
        val placeholderMap = placeholders.toMap()
        if (miniMessage != null) {
            val resolvers = placeholderMap.map { net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed(it.key, it.value) }.toTypedArray()
            return miniMessage.deserialize(message, *resolvers)
        }

        var text = message
        placeholderMap.forEach { (k, v) -> text = text.replace("<$k>", v).replace("</$k>", "") }
        text = text.replace(Regex("</?\\w+>"), "")
        return Component.text(text)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name.equals("ping", ignoreCase = true)) {
            if (sender is Player) {
                val id = sender.uniqueId
                val now = System.currentTimeMillis()
                val last = pingCooldowns[id] ?: 0L
                val remaining = PING_COOLDOWN_MS - (now - last)
                if (remaining > 0) {
                    val seconds = ((remaining + 999) / 1000)
                    val comp = render("<red>Bitte warte noch <gold><seconds></gold> Sekunden.</red>", "seconds" to seconds.toString())
                    sender.sendMessage(comp)
                    return true
                }
                pingCooldowns[id] = now
                val comp = render("<green>Pong!</green>")
                sender.sendMessage(comp)
                return true
            } else {
                sender.sendMessage("Pong!")
                return true
            }
        }
        return super.onCommand(sender, command, label, args)
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        val comp = render("<green>Willkommen auf dem Server, <gold><name></gold>!</green>", "name" to player.name)
        player.sendMessage(comp)

        // Zeige Notify-Status beim Join
        val notifyEnabled = vanishManager.isNotifyEnabled(player)
        val prefix = "<gradient:#3AB0FF:#0052D4>[SYSTEM]</gradient>"
        if (player.hasPermission("mcplugin.vanish.notify")) {
            if (notifyEnabled) {
                player.sendMessage(render("$prefix <green>Vanish-Notifies sind aktuell AKTIVIERT</green>"))
            } else {
                player.sendMessage(render("$prefix <red>Vanish-Notifies sind aktuell DEAKTIVIERT</red>"))
            }
        }

        logger.info("Player ${player.name} joined")
    }
}
