package com.floorplugins.essentialsz;

import com.floorplugins.essentialsz.commands.ReloadCommand;
import com.floorplugins.essentialsz.commands.TpaCommand;
import io.papermc.paper.command.brigadier.BasicCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsZ extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerCommands();
    }

    private void registerCommands() {
        BasicCommand reloadCommand = new ReloadCommand(this);
        registerCommand("essentialsz-reload", reloadCommand);

        BasicCommand tpaCommand = new TpaCommand(this);
        registerCommand("tpa", tpaCommand);
    }
}
