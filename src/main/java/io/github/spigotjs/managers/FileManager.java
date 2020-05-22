package io.github.spigotjs.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileManager {
	
	public boolean exists(String path) {
		return new File(path).exists();
	}
	
	public void create(String path) {
		File file = new File(path);
		File parent = file.getParentFile();
		if(parent != null) {
			parent.mkdirs();
		}
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String load(String path) {
		try {
			return new String(Files.readAllBytes(new File(path).toPath()));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void save(String path, String lines) {
		try {
			Files.write(new File(path).toPath(), lines.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
