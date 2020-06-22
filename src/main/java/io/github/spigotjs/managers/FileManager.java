package io.github.spigotjs.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import com.coveo.nashorn_modules.Require;
import jdk.nashorn.api.scripting.NashornScriptEngine;

public class FileManager {

    private NashornScriptEngine engine;
	private ScriptContext engineContext;

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

    public require(String findModuleName) throws ScriptException {
        File scriptDir = new File("scripts/");
        String runFileName = "";
        for (File file : scriptDir.listFiles()) {
            String folderName = file.getName();
            File resourceFile = new File("scripts/" + folderName + "/resource.json");
            if (resourceFile.exists()) {
                JSONObject config = (JSONObject) new JSONParser().parse(new String(Files.readAllBytes(resourceFile.toPath())));
                String moduleName = config.containsKey("name") ? config.get("name").toString() : moduleName;
                if (moduleName == findModuleName) {
                    runFileName = "scripts/" + moduleName + "/" + (config.containsKey("main") ? config.get("main") : "");
                }
            } else {
                runFileName = "scripts/" + findModuleName + "/" + findModuleName + ".js";
            }
        }
        if (runFileName != "") {
            return false;
        }
        File runFile = new File(runFileName);
        if (!runFile.exists()) {
            SpigotJSReloaded.getInstance().getLogger().severe("The main file for \"" + findModuleName + "\" does not exist!");
            return false;
        }
        String script = new String(Files.readAllBytes(runFile.toPath()));
        CompiledScript compiledScript = engine.compile("(function(){let exports = {};" + script + "return exports;})();");
		return compiledScript.eval(engineContext);
    }

}
