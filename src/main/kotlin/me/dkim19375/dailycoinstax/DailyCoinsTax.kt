package me.dkim19375.dailycoinstax

import de.NeonnBukkit.CoinsAPI.API.CoinsAPI
import me.dkim19375.dailycoinstax.data.DateData
import me.dkim19375.dkimbukkitcore.function.logInfo
import me.dkim19375.dkimbukkitcore.javaplugin.CoreJavaPlugin
import org.bukkit.Bukkit
import kotlin.math.roundToInt

class DailyCoinsTax : CoreJavaPlugin() {
    private var lastDate = DateData.getToday()

    override fun onEnable() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, {
            val date = DateData.getToday()
            if (date <= lastDate) {
                return@runTaskTimerAsynchronously
            }
            lastDate = date
            logInfo("Removing 1% of coins from all players!")
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
            logInfo("Removed ${total.second} coins from ${total.first} players!")
        }, 60L * 20L, 60L * 20L)
    }
}