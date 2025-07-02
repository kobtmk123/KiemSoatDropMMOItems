package com.github.kiemsoatdrop;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;

public class BlockListener implements Listener {

    private final KiemSoatDropMMOItems plugin;
    final NamespacedKey playerPlacedKey;

    public BlockListener(KiemSoatDropMMOItems plugin) {
        this.plugin = plugin;
        this.playerPlacedKey = new NamespacedKey(plugin, "player_placed_block");
    }

    /**
     * Su kien nay duoc goi khi nguoi choi DAT MOT BLOCK.
     * Chung ta se "danh dau" block nay la do nguoi choi dat.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getConfig().getBoolean("settings.check-player-placed-blocks", true)) {
            return;
        }
        Block block = event.getBlock();
        block.getState().getPersistentDataContainer().set(playerPlacedKey, PersistentDataType.BYTE, (byte) 1);
        block.getState().update(false, false);
    }
}