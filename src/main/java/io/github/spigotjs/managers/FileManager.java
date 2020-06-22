package io.github.spigotjs.managers;

import io.github.spigotjs.SpigotJSReloaded;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.lang.RuntimeException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import com.coveo.nashorn_modules.Require;
import jdk.nashorn.api.scripting.NashornScriptEngine;

import lombok.Setter;

@Setter
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

    public Object require(String findModuleName) throws ScriptException, RuntimeException, IOException, ParseException {
        File scriptDir = new File("scripts/");
        String runFileName = "";
        for (File file : scriptDir.listFiles()) {
            String folderName = file.getName();
            File resourceFile = new File("scripts/" + folderName + "/resource.json");
            if (resourceFile.exists()) {
                JSONObject config = (JSONObject) new JSONParser().parse(new String(Files.readAllBytes(resourceFile.toPath())));
                String moduleName = config.containsKey("name") ? config.get("name").toString() : "";
                if (moduleName.equals(findModuleName)) {
                    runFileName = "scripts/" + moduleName + "/" + (config.containsKey("main") ? config.get("main") : "");
                }
            } else {
                if (folderName.equals(findModuleName)) {
                    runFileName = "scripts/" + findModuleName + "/" + findModuleName + ".js";
                }
            }
        }
        if (runFileName.equals("")) {
            throw new RuntimeException("The module \"" + findModuleName + "\" could not be found!");
        }
        File runFile = new File(runFileName);
        if (!runFile.exists()) {
            throw new RuntimeException("The main file for \"" + findModuleName + "\" does not exist!");
        }
        String script = new String(Files.readAllBytes(runFile.toPath()));
        CompiledScript compiledScript = engine.compile("(function(){let exports = {};" + script + "return exports;})();");
		return compiledScript.eval(engineContext);
    }

}
