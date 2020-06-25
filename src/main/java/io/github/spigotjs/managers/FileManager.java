package io.github.spigotjs.managers;

import io.github.spigotjs.SpigotJSReloaded;
import io.github.spigotjs.module.ModuleManager;

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
	private ModuleManager moduleManager;
	
	public boolean exist(String path) {
		return !type(path).equals("none");
	}
	
	public String type(String path) {
		File file = new File(path);
		return file.exists() ? file.isDirectory() ? "folder" : "file" : "none";
	}
	
	public boolean mkdir(String path) throws Exception {
		File file = new File(path);
		File parent = file.getParentFile();
		if (!parent.exists()) {
			parent.mkdirs();
		}
		if (parent.isFile()) {
			return false;
		}
		if (file.isDirectory()) {
			return false;
		}
		if (file.isFile()) {
			return false;
		}
		return file.mkdir();
	}
	
	public boolean write(String path, String text) throws IOException {
		File file = new File(path);
		File parent = file.getParentFile();
		if (!parent.exists()) {
			if (parent.isFile()) {
				return false;
			}
			if (!parent.mkdirs()) {
				return false;
			}
		}
		if (!file.exists()) {
			if (!file.createNewFile()) {
				return false;
			}
		}
		Files.write(file.toPath(), text.getBytes());
		return true;
	}
	
	public String read(String path) throws IOException {
		File file = new File(path);
		if (!file.exists() || file.isDirectory()) {
			return "";
		}
		return new String(Files.readAllBytes(new File(path).toPath()));
	}
	
	public boolean delete(String path) {
		File file = new File(path);
		return file.delete();
	}
	
	public Object require(String... args) throws Exception {
		String scope = args.length > 0 ? args[0] : "";
		String scriptName = args.length > 1 ? args[1] : "";
		if (scriptName.isEmpty() && !scope.isEmpty()) {
			scriptName = scope;
			scope = "";
		}
		Map<String, JSONObject> modules = moduleManager.getModules();
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
		CompiledScript compiledScript = engine.compile("(function(){ const require = (function(module) { return FileManager.require('" + name + "', module); }); let exports = {}; " + script + "; return exports; })();");
		return compiledScript.eval(engineContext);
	}
    
}
