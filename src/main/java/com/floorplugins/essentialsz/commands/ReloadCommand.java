package com.floorplugins.essentialsz.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements BasicCommand {
    public final JavaPlugin plugin;

    public ReloadCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack source, String @NotNull [] args) {
        plugin.reloadConfig();
    }

    @Override
    public String permission() {
        return "essentialsz.admin";
    }
}
