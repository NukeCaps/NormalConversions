package nuclearkat.normalconversions.listeners;

import nuclearkat.normalconversions.NormalConversions;
import nuclearkat.normalconversions.conversion.ConversionRates;
import nuclearkat.normalconversions.conversion.PlayerConversion;
import nuclearkat.normalconversions.currency.Currency;
import nuclearkat.normalconversions.inventories.InventoryManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.regex.Pattern;

import static nuclearkat.normalconversions.util.Util.f;

public class ConversionListener implements Listener {

    private static final Pattern MENU_REGEX = Pattern.compile(ChatColor.translateAlternateColorCodes('&', "&4Convert \\w+ to \\w+ Menu"));

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if (!MENU_REGEX.matcher(event.getView().getTitle()).matches()){
            return;
        }
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();


        PlayerConversion conversion = NormalConversions.getConversionManager().getPlayerConversion(player.getUniqueId());
        int moneyAmount = conversion.getAmount();
        int clickedSlot = event.getRawSlot();

        InventoryManager inventoryManager = NormalConversions.getInventoryManager();

        switch (clickedSlot){
            case 11 -> {
                if (moneyAmount - 16 < 0){
                    return;
                }
                conversion.decreaseAmount(16);
                inventoryManager.openConversionMenu(player, conversion);
            }
            case 12 -> {
                if (moneyAmount == 0){
                    return;
                }
                conversion.decreaseAmount(1);
                inventoryManager.openConversionMenu(player, conversion);
            }
            case 14 ->{
                conversion.increaseAmount(1);
                inventoryManager.openConversionMenu(player, conversion);
            }
            case 15 -> {
                conversion.increaseAmount(16);
                inventoryManager.openConversionMenu(player, conversion);
            }
            case 22 -> {
                if (moneyAmount == 0){
                    return;
                }
                handleConversion(player, conversion);
            }
            case 26 -> inventoryManager.openChooseConversionMenu(player);
        }
    }

    public void handleConversion(Player player, PlayerConversion conversion) {
        ConversionRates conversionRates = NormalConversions.getConversionRates();
        Currency from = conversion.getFrom();
        Currency to = conversion.getTo();
        double amount = conversion.getAmount();
        double cost = conversionRates.getCost(from, to, amount);

        if (from.getPlayerAmount(player) < cost){
            player.closeInventory();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                f("&cYou do not have enough %s for this conversion!", from.getNameLowercasePlural())));
            return;
        }

        from.addPlayerAmount(player, -cost);
        to.addPlayerAmount(player, amount);

        player.closeInventory();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            f("&aConverted &f%.2f %s to &c%.2f %s!", amount, from.getNameLowercasePlural(), cost, to.getNameLowercasePlural())));

    }
}
