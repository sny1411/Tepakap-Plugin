package fr.sny1411.tepakap.commands;

import fr.sny1411.tepakap.Main;
import fr.sny1411.tepakap.sql.MysqlDb;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Unlock implements CommandExecutor {
    private static MysqlDb bdd;
    private static Main main;
    public Unlock(MysqlDb bdd, Main main) {
        Unlock.bdd = bdd;
        Unlock.main = main;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(main, () -> {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Block targetBlock = player.getTargetBlock(null, 5);
                ResultSet resultCoffre = Lock.searchCoffre(targetBlock,
                                                           targetBlock.getX(),
                                                           targetBlock.getY(),
                                                           targetBlock.getZ(),
                                                           targetBlock.getWorld().getName(),
                                                           player);
                try {
                    assert resultCoffre != null;
                    if (resultCoffre.next()) {
                        int idCoffre = resultCoffre.getInt("id_coffre");
                        if (resultCoffre.getString("UUID").equals(player.getUniqueId().toString())) {
                            bdd.modifyItems("DELETE FROM ACCEDE WHERE id_coffre=" + idCoffre);
                            bdd.modifyItems("DELETE FROM COFFRE WHERE id_coffre=" + idCoffre);
                            player.sendMessage("§2[SecureChest] §aCe block n'est plus protégé");
                        } else {
                            player.sendMessage("§4[SecureChest] §cVous n'êtes pas le propriétaire du block");
                        }
                    } else {
                        player.sendMessage("§4[SecureChest] §cBlock non trouvé :(");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Bukkit.getConsoleSender().sendMessage("§4[SecureCHEST] §cCommande non executable par la console");
            }
        });
        return false;
    }
}
