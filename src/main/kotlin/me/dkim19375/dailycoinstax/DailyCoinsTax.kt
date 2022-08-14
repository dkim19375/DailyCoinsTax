package me.dkim19375.dailycoinstax

import de.NeonnBukkit.CoinsAPI.API.CoinsAPI
import me.dkim19375.dkimbukkitcore.javaplugin.CoreJavaPlugin
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import kotlin.math.roundToInt

class DailyCoinsTax : CoreJavaPlugin() {

    override fun onEnable() {
        registerCommand(
            command = "takecoinstax",
            executor = { sender, _, _, _ ->
                onCommand(sender)
                true
            }
        )
    }

    private fun onCommand(sender: CommandSender) {
        sender.sendMessage("${ChatColor.YELLOW}Removing ${ChatColor.GOLD}1% ${ChatColor.YELLOW}of coins from all players!")
        var total = 0 to 0
        for (player in Bukkit.getOfflinePlayers()) {
            val uuid = player.uniqueId.toString()
            if (!CoinsAPI.playerExists(uuid)) {
                continue
            }
            val coins = CoinsAPI.getCoins(uuid)
            val new = (coins.toDouble() * 0.99).roundToInt()
            if (new == coins) {
                continue
            }
            CoinsAPI.setCoins(uuid, new)
            total = total.first + 1 to total.second + (coins - new)
        }
        sender.sendMessage(
            "${ChatColor.YELLOW}Removed ${ChatColor.GOLD}${total.second} ${ChatColor.YELLOW}coins from " +
                    "${ChatColor.GOLD}${total.first} ${ChatColor.YELLOW}players!"
        )
    }
}