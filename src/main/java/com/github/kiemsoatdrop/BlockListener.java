package com.github.kiemsoatdrop;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
// SỬA LỖI: Import class cần thiết cho việc ép kiểu
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.List;

public class BlockListener implements Listener {

    private final KiemSoatDropMMOItems plugin;
    private final NamespacedKey playerPlacedKey;

    public BlockListener(KiemSoatDropMMOItems plugin) {
        this.plugin = plugin;
        this.playerPlacedKey = new NamespacedKey(plugin, "player_placed_block");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfig().getBoolean("settings.check-player-placed-blocks", true)) {
            return;
        }

        Block block = event.getBlock();
        
        // SỬA LỖI: Ép kiểu (cast) block.getState() sang PersistentDataHolder
        PersistentDataHolder holder = (PersistentDataHolder) block.getState();
        holder.getPersistentDataContainer().set(playerPlacedKey, PersistentDataType.BYTE, (byte) 1);
        
        block.getState().update(false, false);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        List<String> exemptBlocks = plugin.getConfig().getStringList("settings.exempt-blocks");
        if (exemptBlocks.contains(block.getType().name())) {
            return;
        }

        boolean shouldCancelMMODrops = false;

        if (plugin.getConfig().getBoolean("settings.check-player-placed-blocks", true)) {
            // SỬA LỖI: Ép kiểu (cast) block.getState() sang PersistentDataHolder
            PersistentDataHolder holder = (PersistentDataHolder) block.getState();
            if (holder.getPersistentDataContainer().has(playerPlacedKey, PersistentDataType.BYTE)) {
                shouldCancelMMODrops = true;
            }
        }
        
        if (!shouldCancelMMODrops && plugin.getConfig().getBoolean("settings.check-crop-growth", true)) {
            BlockData blockData = block.getBlockData();
            if (blockData instanceof Ageable) {
                Ageable ageable = (Ageable) blockData;
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    shouldCancelMMODrops = true;
                }
            }
        }

        if (shouldCancelMMODrops) {
            event.setCancelled(true);
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            Collection<ItemStack> drops = block.getDrops(itemInHand, player);
            block.setType(Material.AIR);
            if (!drops.isEmpty()) {
                drops.forEach(item -> block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), item));
            }
        }
    }
}