package io.github.spigotjs.script;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.coveo.nashorn_modules.FilesystemFolder;
import com.coveo.nashorn_modules.Require;
import com.google.gson.JsonElement;

import io.github.spigotjs.SpigotJSReloaded;
import jdk.internal.dynalink.beans.StaticClass;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import lombok.Getter;

@Getter
public class ScriptManager {

	private NashornScriptEngine engine;
	private List<ScriptResource> scriptResources;
	private File scriptDirectory;
	private ScriptContext engineContext;

	public ScriptManager() {
		scriptResources = new ArrayList<ScriptResource>();
		scriptDirectory = new File("scripts/");
		scriptDirectory.mkdir();
		System.setProperty("nashorn.args", "--language=es6");
		loadRuntime();
	}

	public void loadRuntime() {
		try {
			engine = null;
			ScriptEngineManager manager = new ScriptEngineManager(null);
			engine = (NashornScriptEngine) manager.getEngineByName("JavaScript");
			Require.enable(engine, FilesystemFolder.create(scriptDirectory, "UTF-8"));

			ScriptContext context = engine.getContext();
			Bindings bindings = engine.createBindings();

			for (Entry<String, JsonElement> entry : SpigotJSReloaded.getInstance().getPreDeclared().entrySet()) {
				bindings.put(entry.getKey(), StaticClass.forClass(Class.forName(entry.getValue().getAsString())));
			}

			context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			engineContext = context;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void loadScripts() {
		for (File file : scriptDirectory.listFiles()) {
			if (!file.isDirectory()) {
				SpigotJSReloaded.getInstance().getLogger()
						.warning("There is a file in the \"scripts\" directory, that isnt a folder.");
				return;
			}
			loadResource(file);
		}
	}

	public void loadResource(File directory) {
		try {
			String name = directory.getName();
			File resourceFile = new File("scripts/" + name + "/resource.json");
			if (!resourceFile.exists()) {
				SpigotJSReloaded.getInstance().getLogger()
						.severe("The resource \"" + name + "\" misses a resource.json file!");
				return;
			}
			JSONObject jsonObject = (JSONObject) new JSONParser()
					.parse(new String(Files.readAllBytes(resourceFile.toPath())));
			if (!jsonObject.containsKey("version") || !jsonObject.containsKey("main")
					|| !jsonObject.containsKey("author")) {
				SpigotJSReloaded.getInstance().getLogger()
						.severe("The resource \"" + name + "\" has an invalid resource.json file!");
				return;
			}
			String version = jsonObject.get("version").toString();
			File main = new File("scripts/" + name + "/" + jsonObject.get("main").toString());
			String author = jsonObject.get("author").toString();
			if (!main.exists()) {
				SpigotJSReloaded.getInstance().getLogger()
						.severe("The main file of the resource \"" + name + "\" does not exist!");
				return;
			}
			ScriptResource resource = new ScriptResource(name, main, author, version);
			scriptResources.add(resource);
			evalScript(new String(Files.readAllBytes(main.toPath())));
			SpigotJSReloaded.getInstance().getLogger().info("Loaded " + name + " v" + version + " by " + author);

		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}
	}

	private void evalScript(String script) throws ScriptException {
		CompiledScript compiledScript = engine.compile(script);
		compiledScript.eval(engineContext);
	}

}
