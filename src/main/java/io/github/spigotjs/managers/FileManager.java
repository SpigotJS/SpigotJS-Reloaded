package io.github.spigotjs.managers;

import io.github.spigotjs.SpigotJSReloaded;
import io.github.spigotjs.script.ScriptManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.lang.RuntimeException;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import lombok.Setter;

@SuppressWarnings({ "restriction", "unused" })
@Setter
public class FileManager {
	
    private NashornScriptEngine engine;
	private ScriptContext engineContext;
	private ScriptManager scriptManager;
	
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
	
	public Object require(String... args) throws Exception {
		String scope = args.length > 0 ? args[0] : "";
		String scriptName = args.length > 1 ? args[1] : "";
		Map<String, JSONObject> modules = scriptManager.getModules();
		if (scriptName.startsWith("./")) {
			if (!scope.isEmpty()) {
				scriptName = scriptName.split("./")[1];
				JSONObject module = (JSONObject) modules.get(scope);
				JSONObject scripts = (JSONObject) module.get("scripts");
				String script = (String) scripts.get(scriptName);
				return run(script, scope);
			}
			throw new Exception("Scope cannot be empty when requiring subscript.");
		}
		if (!modules.containsKey(scriptName)) {
			throw new Exception("Invalid module requested '" + scriptName + "' !");
		}
		JSONObject module = (JSONObject) modules.get(scriptName);
		String main = (String) module.get("main");
		JSONObject scripts = (JSONObject) module.get("scripts");
		String script = (String) scripts.get(main);
		return run(script, scriptName);
	}
	
	private Object run(String script, String name) throws ScriptException {
		CompiledScript compiledScript = engine.compile("(function(){const require = (function(module) { return FileManager.require(\"" + name + "\", module); }); let exports = {};" + script + ";return exports;})();");
		return compiledScript.eval(engineContext);
	}
    
}
