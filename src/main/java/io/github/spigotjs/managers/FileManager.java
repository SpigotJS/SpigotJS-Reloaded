package io.github.spigotjs.managers;

import io.github.spigotjs.SpigotJSReloaded;
import io.github.spigotjs.module.ModuleManager;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Map;
import java.lang.RuntimeException;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.NashornScriptEngine;

import lombok.Setter;

@SuppressWarnings({ "restriction", "unused" })
@Setter
public class FileManager {
	
    private NashornScriptEngine engine;
	private ScriptContext engineContext;
	private ModuleManager moduleManager;
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
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
		CompiledScript compiledScript = engine.compile("(function(){ const require = (function(module) { return FileManager.require('" + name + "', module); }); const config = (function(file, data) { return data ? FileManager.config(file, '" + name + "', data) : FileManager.config(file, '" + name + "'); }); let exports = {}; " + script + "; return exports; })();");
		return compiledScript.eval(engineContext);
	}
	
	public Object config(String name, String module) throws Exception {
		String jsonString = read("jsconfigs/" + module + "/" + name + ".json");
		Object jsonObject;
		ScriptEngineManager manager = new ScriptEngineManager(null);
		NashornScriptEngine engine = (NashornScriptEngine) manager.getEngineByName("JavaScript");
		ScriptContext context = engine.getContext();
		Bindings bindings = engine.createBindings();
		bindings.put("json", jsonString);
		context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		engine.compile("json = JSON.parse(json);").eval(context);
		jsonObject = (Object) engine.get("json");
		return jsonObject;
	}
    
	public void config(String name, String module, Object jsonObject) throws ScriptException {
		String jsonString;
		ScriptEngineManager manager = new ScriptEngineManager(null);
		NashornScriptEngine engine = (NashornScriptEngine) manager.getEngineByName("JavaScript");
		ScriptContext context = engine.getContext();
		Bindings bindings = engine.createBindings();
		bindings.put("json", jsonObject);
		context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		engine.compile("json = JSON.stringify(json, null, 4);").eval(context);
		jsonString = (String) engine.get("json");
		try {
			write("jsconfigs/" + module + "/" + name + ".json", jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
