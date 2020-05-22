package io.github.spigotjs.managers;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
	
	public FileConfiguration load(String path) {
		File file = new File(path);
		if(!file.exists()) {
			File parent = file.getParentFile();
			if(parent != null) {
				parent.mkdirs();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return YamlConfiguration.loadConfiguration(file);
	}
	
	public void save(String path, FileConfiguration configuration) {
		File file = new File(path);
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			configuration.save(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean exists(String path) {
		return new File(path).exists();
	}

}
