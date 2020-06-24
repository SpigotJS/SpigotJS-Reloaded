package io.github.spigotjs;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonObject;

import io.github.spigotjs.commands.SpigotJSCommandBase;
import io.github.spigotjs.script.ScriptManager;
import lombok.Getter;

@Getter
public class SpigotJSReloaded extends JavaPlugin {

	@Getter
	private static SpigotJSReloaded instance;

	private JsonObject preDeclared;
	private ScriptManager scriptManager;


	private long start;

	public static final String PREFIX = "§6§lSpigot§e§lJS §8➥ §7";

	@Override
	public void onLoad() {
		try {
			Thread.currentThread().setContextClassLoader(getClassLoader());
			start = System.currentTimeMillis();
			instance = this;
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public ClassLoader getLoader() {
		return getClassLoader();
	}

	@Override
	public void onEnable() {
		getCommand("spigotjs").setExecutor(new SpigotJSCommandBase());
		scriptManager = new ScriptManager();
		scriptManager.loadScripts();
	}

}
