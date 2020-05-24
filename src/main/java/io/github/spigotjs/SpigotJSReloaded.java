package io.github.spigotjs;

import java.io.File;
import java.util.Map.Entry;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.spigotjs.commands.SpigotJSCommandBase;
import io.github.spigotjs.script.ScriptManager;
import io.github.spigotjs.script.addons.ScriptAddonManager;
import io.github.spigotjs.script.addons.ScriptDeclarationAddon;
import lombok.Getter;

@Getter
public class SpigotJSReloaded extends JavaPlugin {

	@Getter
	private static SpigotJSReloaded instance;
	
	private JsonObject preDeclared;
	private ScriptManager scriptManager;
	
	private ScriptAddonManager scriptAddonManager;
	
	private long start;
	
	public static final String PREFIX = "§6§lSpigot§e§lJS §8➥ §7";
	
	@Override
	public void onLoad() {
		try {
			start = System.currentTimeMillis();
			instance = this;
			scriptAddonManager = new ScriptAddonManager();
			getDataFolder().mkdir();
			File predeclaredFile = new File("plugins/SpigotJS-Reloaded/predeclared.json");
			if(!predeclaredFile.exists()) {
				predeclaredFile.createNewFile();
		        Gson gson = new GsonBuilder().setPrettyPrinting().create();
		        JsonObject object = new JsonObject();
		        object.addProperty("Bukkit", "org.bukkit.Bukkit");
		        object.addProperty("GameMode", "org.bukkit.GameMode");
		        preDeclared = object;
		        String json = gson.toJson(object);
		        java.nio.file.Files.write(predeclaredFile.toPath(), json.getBytes());
			} else {
				preDeclared = new JsonParser().parse(new String(java.nio.file.Files.readAllBytes(predeclaredFile.toPath()))).getAsJsonObject();
			}
			for (Entry<String, JsonElement> entry : preDeclared.entrySet()) {
				scriptAddonManager.registerDeclarationAddon(new ScriptDeclarationAddon("PreDeclared-File", entry.getKey(), Class.forName(entry.getValue().getAsString())));
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void onEnable() {
		getCommand("spigotjs").setExecutor(new SpigotJSCommandBase());
		scriptManager = new ScriptManager();
		scriptManager.loadScripts();
		long ms = System.currentTimeMillis() - start;
		getLogger().info("There are " + scriptAddonManager.getDeclarationAddons().size() + " declaration-addons and " + scriptAddonManager.getCodeAddons().size() + " code-addons registered.");
		getLogger().info("Enabled SpigotJS in " + ms + "ms. Successfully loaded " + scriptManager.getScriptResources().size() + " scripts.");
	}
	
}
