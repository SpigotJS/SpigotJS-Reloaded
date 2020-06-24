package io.github.spigotjs;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.JsonObject;

import io.github.spigotjs.commands.SpigotJSCommandBase;
import io.github.spigotjs.compile.MethodInvocationUtils;
import io.github.spigotjs.compile.Compiler;
import io.github.spigotjs.module.ModuleManager;
import lombok.Getter;

@Getter
public class SpigotJSReloaded extends JavaPlugin {

	@Getter
	private static SpigotJSReloaded instance;

	private JsonObject preDeclared;
	private ModuleManager moduleManager;


	public static final String PREFIX = "§6§lSpigot§e§lJS §8➥ §7";
	
	@Override
	public void onLoad() {
		try {
			Thread.currentThread().setContextClassLoader(getClassLoader());
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
		moduleManager = new ModuleManager();
		moduleManager.loadModules();
	}

}
