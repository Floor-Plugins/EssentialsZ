package com.floorplugins.essentialsz.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public record MessageUtils(JavaPlugin plugin) {
    public String withFormat(String type, String message) {
        FileConfiguration config = plugin.getConfig();
        String template = config.getString("config.message-templates." + type, "$message$");
        return template.replace("$message$", message);
    }

    public String get(String command, String path, Map<String, String> replace) {
        FileConfiguration config = plugin.getConfig();
        String text = config.getString(command + ".messages." + path, "<red>Missing message! <bold>" + command + "/" + path + "</bold></red>");

        if (replace != null) {
            for (Map.Entry<String, String> entry : replace.entrySet()) {
                text = text.replace("$" + entry.getKey() + "$", entry.getValue());
            }
        }

        return text;
    }
}
