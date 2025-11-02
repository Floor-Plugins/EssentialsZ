package com.floorplugins.essentialsz.commands;

import com.floorplugins.essentialsz.utils.MessageUtils;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ReloadCommand implements BasicCommand {
    public final JavaPlugin plugin;
    public final MessageUtils messageUtils;
    public final MiniMessage mm = MiniMessage.miniMessage();

    public ReloadCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messageUtils = new MessageUtils(this.plugin);
    }

    @Override
    public void execute(@NotNull CommandSourceStack source, String @NotNull [] args) {
        plugin.reloadConfig();
        send(source.getSender(), "success", "config-reloaded", null);
    }

    @Override
    public String permission() {
        return "essentialsz.admin";
    }

    private void send(CommandSender sender, String type, String path, Map<String, String> replace) {
        sender.sendMessage(mm.deserialize(messageUtils.withFormat(type, messageUtils.get("config", path, replace))));
    }
}
