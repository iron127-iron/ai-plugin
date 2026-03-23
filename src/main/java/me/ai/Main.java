package me.ai;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        getCommand("ai").setExecutor(new AICommand(this));
    }

    public static Main getInstance() {
        return instance;
    }
}
