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
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.List;

public class BlockListener implements Listener {

    private final KiemSoatDropMMOItems plugin;
    // Khóa duy nhất để lưu trữ dữ liệu vào block, giống như "tên" của dữ liệu.
    private final NamespacedKey playerPlacedKey;

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
        // Chỉ xử lý nếu tính năng được bật trong config
        if (!plugin.getConfig().getBoolean("settings.check-player-placed-blocks", true)) {
            return;
        }

        Block block = event.getBlock();
        // Luu mot "dau hieu" vao block, de sau nay biet la do nguoi choi dat.
        // PersistentDataContainer la noi an toan de luu tru du lieu nay, no se ton tai qua cac lan restart server.
        block.getState().getPersistentDataContainer().set(playerPlacedKey, PersistentDataType.BYTE, (byte) 1);
        block.getState().update(false, false); // Luu lai trang thai ma khong gay lag (không cần cập nhật vật lý)
    }

    /**
     * Su kien nay duoc goi khi nguoi choi PHA MOT BLOCK.
     * Day la noi logic chinh se duoc thuc thi.
     * Uu tien HIGHEST de chay truoc MMOItems va cac plugin khac.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Bo qua neu nguoi choi o che do Creative
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Kiem tra xem block co duoc mien tru trong config khong
        List<String> exemptBlocks = plugin.getConfig().getStringList("settings.exempt-blocks");
        if (exemptBlocks.contains(block.getType().name())) {
            return; // Neu duoc mien tru, bo qua va cho phep drop binh thuong
        }

        boolean shouldCancelMMODrops = false;

        // --- Logic 1: Kiem tra block co phai do nguoi choi dat khong ---
        if (plugin.getConfig().getBoolean("settings.check-player-placed-blocks", true)) {
            // Kiem tra xem block co "dau hieu" ma chung ta da luu o onBlockPlace khong.
            if (block.getState().getPersistentDataContainer().has(playerPlacedKey, PersistentDataType.BYTE)) {
                shouldCancelMMODrops = true;
            }
        }

        // --- Logic 2: Kiem tra cay trong da lon toi da chua ---
        // Chi kiem tra cay trong neu logic 1 chua xac dinh can huy drop
        if (!shouldCancelMMODrops && plugin.getConfig().getBoolean("settings.check-crop-growth", true)) {
            BlockData blockData = block.getBlockData();
            // Kiem tra neu block la mot loai cay trong co "tuoi" (Ageable)
            if (blockData instanceof Ageable) {
                Ageable ageable = (Ageable) blockData;
                // So sanh tuoi hien tai voi tuoi toi da
                if (ageable.getAge() < ageable.getMaximumAge()) {
                    shouldCancelMMODrops = true;
                }
            }
        }

        // --- Hanh dong cuoi cung: Neu can huy drop cua MMOItems ---
        if (shouldCancelMMODrops) {
            // Day la "meo" de huy drop MMOItems ma van giu drop vanilla:
            // 1. Huy su kien BlockBreakEvent. Dieu nay se ngan chan MMOItems (va cac plugin khac) xu ly su kien nay.
            event.setCancelled(true);

            // 2. Lay cac vat pham vanilla se roi ra tu block (co tinh ca Fortune).
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            Collection<ItemStack> drops = block.getDrops(itemInHand, player);

            // 3. Tu tay pha vo block.
            block.setType(Material.AIR);

            // 4. Tu tay lam roi ra cac vat pham vanilla.
            // Vi chung ta da huy su kien, MMOItems se khong bao gio biet block nay da bi pha.
            if (!drops.isEmpty()) {
                drops.forEach(item -> block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), item));
            }
        }
        // Neu shouldCancelMMODrops la false, chung ta khong lam gi ca,
        // de su kien tiep tuc va cho phep MMOItems xu ly drop.
    }
}