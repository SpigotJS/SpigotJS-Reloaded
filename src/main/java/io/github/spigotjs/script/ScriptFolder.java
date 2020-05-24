package io.github.spigotjs.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import com.coveo.nashorn_modules.AbstractFolder;
import com.coveo.nashorn_modules.Folder;

public class ScriptFolder extends AbstractFolder {
	private File root;
	private String encoding = "UTF-8";

	private ScriptFolder(File root, Folder parent, String path, String encoding) {
	    super(parent, path);
	    this.root = root;
	    this.encoding = encoding;
	  }

	@Override
	public String getFile(String name) {
		File file = new File(root.getAbsolutePath() + name);

		try {
			return new String(Files.readAllBytes(file.toPath()));
		} catch (FileNotFoundException ex) {
			return null;
		} catch (IOException ex) {
			return null;
		}
	}

	@Override
	public Folder getFolder(String name) {
		File folder = new File(root.getAbsolutePath() + name);
		if (!folder.exists()) {
			return null;
		}

		return new ScriptFolder(folder, this, getPath() + name + "/", encoding);
	}

	public static ScriptFolder create(File root, String encoding) {
		File absolute = root.getAbsoluteFile();
		return new ScriptFolder(absolute, null, absolute.getPath() + "/", encoding);
	}

}
