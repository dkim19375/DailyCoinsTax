package me.dkim19375.dailycoinstax

import de.NeonnBukkit.CoinsAPI.d
import me.dkim19375.dkimbukkitcore.javaplugin.CoreJavaPlugin
import me.dkim19375.dkimcore.extension.atomicBoolean
import me.dkim19375.dkimcore.extension.toUUID
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class DailyCoinsTax : CoreJavaPlugin() {
    private val executor = Executors.newSingleThreadExecutor()
    private var removing by atomicBoolean()

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
        if (removing) {
            sender.sendMessage("${ChatColor.RED}Already removing coins from players!")
            return
        }
        removing = true
        sender.sendMessage("${ChatColor.YELLOW}Removing ${ChatColor.GOLD}1% ${ChatColor.YELLOW}of coins from all players!")
        executor.execute {
            runCatching {
                val start = System.nanoTime()
                val table = hashMapOf<UUID, Int>()
                val connection = d.f
                connection.createStatement().use { statement ->
                    statement.executeQuery("SELECT * FROM Coins").use { sets ->
                        while (sets.next()) {
                            val uuid = sets.getString("UUID").toUUID() ?: continue
                            val coins = sets.getInt("Coins")
                            table[uuid] = coins
                        }
                    }
                }
                val modify = mutableMapOf<UUID, Int>()
                var total = 0 to 0
                for ((uuid, coins) in table.entries) {
                    val new = (coins.toDouble() * 0.99).roundToInt()
                    if (new == coins) {
                        continue
                    }
                    modify[uuid] = new
                    total = total.first + 1 to total.second + (coins - new)
                }
                if (modify.isNotEmpty()) {
                    connection.createStatement().use { statement ->
                        @Suppress("SqlWithoutWhere")
                        val query = "UPDATE Coins SET Coins = CASE UUID ${
                            modify.entries.joinToString(" ") { (uuid, coins) ->
                                "WHEN '$uuid' THEN '$coins'"
                            }
                        } ELSE Coins END WHERE UUID IN (${
                            modify.keys.joinToString { uuid -> "'$uuid'" }
                        });"
                        statement.execute(query)
                    }
                }
                val end = System.nanoTime()
                val time = ((end - start) / 100000).toDouble() / 10
                sender.sendMessage(
                    "${ChatColor.YELLOW}Removed ${ChatColor.GOLD}${total.second} ${ChatColor.YELLOW}coins from " +
                            "${ChatColor.GOLD}${total.first} ${ChatColor.YELLOW}players (${time}ms)!"
                )
                removing = false
            }.onFailure {
                it.printStackTrace()
                sender.sendMessage("${ChatColor.RED}Error while removing coins from players! More info in console.")
            }
        }
    }
}