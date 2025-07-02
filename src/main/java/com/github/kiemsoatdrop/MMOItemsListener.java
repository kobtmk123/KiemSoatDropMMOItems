package com.github.kiemsoatdrop;

import io.lumine.mythic.lib.api.event.MMOItemsDropItemEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class MMOItemsListener implements Listener {

    private final KiemSoatDropMMOItems plugin;
    private final NamespacedKey playerPlacedKey;

    public MMOItemsListener(KiemSoatDropMMOItems plugin) {
        this.plugin = plugin;
        this.playerPlacedKey = new NamespacedKey(plugin, "player_placed_block");
    }

    /**
     * Su kien nay duoc kich hoat boi MMOItems SAU KHI no da quyet dinh se drop item gi.
     * Day la noi an toan nhat de can thiep.
     */
    @EventHandler
    public void onMMOItemsDrop(MMOItemsDropItemEvent event) {
        Block block = event.getDropper().getBlock(); // Lấy block từ dropper
        if (block == null) {
            return;
        }

        List<String> exemptBlocks = plugin.getConfig().getStringList("settings.exempt-blocks");
        if (exemptBlocks.contains(block.getType().name())) {
            return;
        }

        boolean shouldCancelMMODrops = false;

        // Logic 1: Kiem tra block co phai do nguoi choi dat khong
        if (plugin.getConfig().getBoolean("settings.check-player-placed-blocks", true)) {
            if (block.getState().getPersistentDataContainer().has(playerPlacedKey, PersistentDataType.BYTE)) {
                shouldCancelMMODrops = true;
            }
        }

        // Logic 2: Kiem tra cay trong da lon toi da chua
        if (!shouldCancelMMODrops && plugin.getConfig().getBoolean("settings.check-crop-growth", true)) {
            BlockData blockData = block.getBlockData();
            if (blockData instanceof Ageable) {
                Ageable ageable = (Ageable) blockData;
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    shouldCancelMMODrops = true;
                }
            }
        }

        // Hanh dong cuoi cung: Neu block khong hop le, xoa toan bo drop list
        if (shouldCancelMMODrops) {
            event.getDrops().clear();
        }
    }
}