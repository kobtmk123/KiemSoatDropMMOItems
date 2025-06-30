package com.github.kiemsoatdrop;

import org.bukkit.plugin.java.JavaPlugin;

public final class KiemSoatDropMMOItems extends JavaPlugin {

    @Override
    public void onEnable() {
        // Sao chép config.yml mặc định nếu chưa có
        saveDefaultConfig();

        // Đăng ký Listener để lắng nghe các sự kiện của block
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);

        // Đăng ký lệnh /ksdrop
        this.getCommand("ksdrop").setExecutor(new ReloadCommand(this));

        // Ghi log ra console
        getLogger().info("Plugin KiemSoatDropMMOItems da duoc bat.");
        getLogger().info("Plugin se ngan chan viec farm item tu block do nguoi choi dat.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin KiemSoatDropMMOItems da duoc tat.");
    }
}