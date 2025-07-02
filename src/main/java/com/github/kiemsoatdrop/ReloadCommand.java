package com.github.kiemsoatdrop;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final KiemSoatDropMMOItems plugin;

    public ReloadCommand(KiemSoatDropMMOItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix", "&e&l[KS-Drop] &r"));

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("ksdrop.admin")) {
                String noPermMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission", "&cBan khong co quyen su dung lenh nay."));
                sender.sendMessage(prefix + noPermMsg);
                return true;
            }

            plugin.reloadConfig();

            String successMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.reload-success", "&aDa tai lai cau hinh thanh cong!"));
            sender.sendMessage(prefix + successMsg);
            return true;
        }

        String invalidCmdMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.invalid-command", "&cLenh khong hop le. Su dung /ksdrop reload."));
        sender.sendMessage(prefix + invalidCmdMsg);
        return true;
    }
}