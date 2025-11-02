package com.floorplugins.essentialsz;

import com.floorplugins.essentialsz.commands.TpaCommand;
import io.papermc.paper.command.brigadier.BasicCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsZ extends JavaPlugin {

    @Override
    public void onEnable() {
        registerCommands();
    }

    private void registerCommands() {
        BasicCommand tpaCommand = new TpaCommand();
        registerCommand("tpa", tpaCommand);
    }
}
