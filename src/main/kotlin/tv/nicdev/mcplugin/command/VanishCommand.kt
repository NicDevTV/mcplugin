package tv.nicdev.mcplugin.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import tv.nicdev.mcplugin.MCPlugin

class VanishCommand(private val plugin: MCPlugin) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Nur Spieler können vanish benutzen.")
            return true
        }

        if (!sender.hasPermission("mcplugin.command.vanish")) {
            sender.sendMessage("§cDu hast keine Berechtigung, diesen Befehl zu nutzen.")
            return true
        }

        if (args.isNotEmpty()) {
            when (args[0].lowercase()) {
                "notify" -> {
                    if (!sender.hasPermission("mcplugin.vanish.notify")) {
                        sender.sendMessage("§cDu hast keine Berechtigung, Notifies zu toggeln.")
                        return true
                    }
                    val enabled = plugin.vanishManager.toggleNotify(sender)
                    val prefix = "<gradient:#3AB0FF:#0052D4>[SYSTEM]</gradient>"
                    if (enabled) {
                        sender.sendMessage(plugin.render("$prefix <green>Vanish-Notifies wurden aktiviert</green>"))
                    } else {
                        sender.sendMessage(plugin.render("$prefix <red>Vanish-Notifies wurden deaktiviert</red>"))
                    }
                    return true
                }
            }
        }

        val vm = plugin.vanishManager
        val newState = !vm.isVanished(sender)
        vm.setVanished(sender, newState)

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String>? {
        if (args.size == 1) {
            val opts = mutableListOf<String>()
            val candidate = "notify"
            if (candidate.startsWith(args[0], ignoreCase = true)) {
                if (sender.hasPermission("mcplugin.vanish.notify")) {
                    opts.add(candidate)
                }
            }
            return opts
        }
        return mutableListOf()
    }
}
