package com.github.kiemsoatdrop;

import org.bukkit.plugin.java.JavaPlugin;

public final class KiemSoatDropMMOItems extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Đăng ký cả hai listener cần thiết
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new MMOItemsListener(this), this);

        // Đăng ký lệnh /ksdrop
        this.getCommand("ksdrop").setExecutor(new ReloadCommand(this));

        getLogger().info("Plugin KiemSoatDropMMOItems v1.1 da duoc bat.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin KiemSoatDropMMOItems v1.1 da duoc tat.");
    }
}