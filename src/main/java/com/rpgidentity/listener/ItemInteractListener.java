package com.rpgidentity.listener;

import com.rpgidentity.RPGIdentityPlugin;
import com.rpgidentity.gui.IdentityCardGUI;
import com.rpgidentity.model.IdentityData;
import com.rpgidentity.util.CardItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ItemInteractListener implements Listener {

    private final RPGIdentityPlugin plugin;

    public ItemInteractListener(RPGIdentityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (isCardItem(item)) {
                event.setCancelled(true);
                Player viewer = event.getPlayer();
                Player target = viewer;

                UUID ownerUuid = CardItemUtil.getOwnerUuid(plugin, item);
                if (ownerUuid != null) {
                    Player owner = Bukkit.getPlayer(ownerUuid);
                    if (owner != null) {
                        target = owner;
                    }
                }
                IdentityCardGUI.open(plugin, viewer, target);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (player.isSneaking() && event.getRightClicked() instanceof Player target) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (isCardItem(item)) {
                event.setCancelled(true);
                IdentityData data = plugin.getIdentityManager().getIdentity(player.getUniqueId());
                if (data != null && data.isRegistered()) {
                    player.sendMessage(plugin.getPluginConfig().getShowCardSender(target.getName()));
                    target.sendMessage(plugin.getPluginConfig().getShowCardReceiver(player.getName()));
                    IdentityCardGUI.open(plugin, target, player);
                    target.playSound(target.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
                }
            }
        }
    }

    private boolean isCardItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        String name = item.getItemMeta().getDisplayName();
        return name != null && (name.contains("KARTU IDENTITAS RPG") || name.contains("KARTU TANDA PENDUDUK"));
    }
}
