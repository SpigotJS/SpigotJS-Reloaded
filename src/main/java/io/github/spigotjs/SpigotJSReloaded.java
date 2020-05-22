package io.github.spigotjs;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.spigotjs.script.ScriptManager;
import lombok.Getter;

@Getter
public class SpigotJSReloaded extends JavaPlugin {

	@Getter
	private static SpigotJSReloaded instance;
	
	private JsonObject preDeclared;
	private ScriptManager scriptManager;
	
	@Override
	public void onLoad() {
		try {
			instance = this;
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
			scriptManager = new ScriptManager();
			scriptManager.loadScripts();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
