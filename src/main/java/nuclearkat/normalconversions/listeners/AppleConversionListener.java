package nuclearkat.normalconversions.listeners;

import net.milkbowl.vault.economy.Economy;
import nuclearkat.normalconversions.NormalConversions;
import nuclearkat.normalconversions.inventories.InventoryManager;
import nuclearkat.normalconversions.conversion.ConversionRates;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AppleConversionListener implements Listener {

    private final InventoryManager inventoryManager;
    private final ConversionRates conversionRates;
    private final Map<UUID, Double> playerBalance = new HashMap<>();

    public AppleConversionListener(InventoryManager inventoryManager, ConversionRates conversionRates){
        this.inventoryManager = inventoryManager;
        this.conversionRates = conversionRates;
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event){
        if (!event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', "&4Apple Conversion Menu"))){
            return;
        }
        Player player = (Player) event.getPlayer();
        Economy economy = NormalConversions.getEconomy();
        this.playerBalance.put(player.getUniqueId(), economy.getBalance(player));
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if (!event.getView().getTitle().equals(ChatColor.translateAlternateColorCodes('&', "&4Apple Conversion Menu"))){
            return;
        }
        event.setCancelled(true);

        Economy economy = NormalConversions.getEconomy();
        Player player = (Player) event.getWhoClicked();

        playerBalance.putIfAbsent(player.getUniqueId(), economy.getBalance(player));
        double cachedBalance = this.playerBalance.get(player.getUniqueId());
        double currentBalance = economy.getBalance(player);



        if (cachedBalance != currentBalance){
            player.closeInventory();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYour balance has changed during this conversion, please try again!"));
            return;
        }


        handleInteraction(event.getRawSlot(), player);
    }

    private void handleInteraction(int clickedSlot, Player player){
        int appleAmount = inventoryManager.getAppleAmount();

        switch (clickedSlot){
            case 11 -> {
                if (appleAmount - 16 < 0){
                    return;
                }
                appleAmount -= 16;
                player.openInventory(inventoryManager.getAppleConversionMenu(appleAmount, calculateCost(player, appleAmount), player));
            }
            case 12 -> {
                if (appleAmount == 0){
                    return;
                }
                appleAmount -= 1;
                player.openInventory(inventoryManager.getAppleConversionMenu(appleAmount, calculateCost(player, appleAmount), player));
            }
            case 14 ->{
                if (appleAmount + 1 > 64){
                    return;
                }
                appleAmount += 1;
                player.openInventory(inventoryManager.getAppleConversionMenu(appleAmount, calculateCost(player, appleAmount), player));
            }
            case 15 -> {
                if (appleAmount + 16 > 64){
                    return;
                }
                appleAmount += 16;
                player.openInventory(inventoryManager.getAppleConversionMenu(appleAmount, calculateCost(player, appleAmount), player));
            }
            case 22 -> {
                if (appleAmount == 0){
                    return;
                }
                handleConversion(player, appleAmount);
            }
            case 26 -> player.openInventory(inventoryManager.getConversionMenu());
        }
    }

    private double calculateCost(Player player, int apples){
        double rate = conversionRates.getPlayerAppleRate(player.getUniqueId());
        return rate * apples;
    }

    private void handleConversion(Player player, int apples){
        Economy economy = NormalConversions.getEconomy();
        double cost = calculateCost(player, apples);
        double cachedBalance = playerBalance.get(player.getUniqueId());
        NumberFormat numberFormat = NumberFormat.getInstance();

        if (cachedBalance < cost){
            player.closeInventory();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have enough money for this conversion!"));
            return;
        }

        economy.withdrawPlayer(player, cost);
        CommandSender sender = Bukkit.getConsoleSender();
        String command = "apple give %player% %amount%".replace("%player%", player.getDisplayName()).replace("%amount%", String.valueOf(apples));
        Bukkit.dispatchCommand(sender, command);
        player.closeInventory();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aConverted &f" + numberFormat.format(cost) + " money to " + apples + "&c apples!"));
    }


}