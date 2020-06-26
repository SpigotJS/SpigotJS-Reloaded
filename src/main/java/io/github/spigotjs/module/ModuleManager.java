package io.github.spigotjs.module;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import io.github.spigotjs.SpigotJSReloaded;
import io.github.spigotjs.libraries.MySQL;
import io.github.spigotjs.managers.CommandManager;
import io.github.spigotjs.managers.ConfigManager;
import io.github.spigotjs.managers.EventManager;
import io.github.spigotjs.managers.FileManager;
import io.github.spigotjs.managers.TaskManager;
import io.github.spigotjs.support.NodeConsole;
import io.github.spigotjs.utils.ModuleBukkitCommand;
import jdk.internal.dynalink.beans.StaticClass;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import lombok.Getter;

@SuppressWarnings({ "restriction" })
@Getter
public class ModuleManager {
	private NashornScriptEngine engine;
	private File modulesDirectory;
	private ScriptContext engineContext;
	private List<ModuleBukkitCommand> moduleBukkitCommands;
	private Field bukkitCommandMap;
	private SimpleCommandMap commandMap;
	private CommandManager commandManager;
	private EventManager eventManager;
	private ConfigManager configManager;
	private FileManager fileManager;
	private TaskManager taskManager;
	private Map<String, JSONObject> modules;
	private ArrayList<String> moduleDone;
	private ArrayList<String> moduleError;
	private ArrayList<String> moduleLoading;
	/*
	 * Node-Support
	 */
	private NodeConsole console;
	private String codeFromAddons;
	public ModuleManager() {
		modulesDirectory = new File("jsmodules/");
		modulesDirectory.mkdir();
		moduleBukkitCommands = new ArrayList<ModuleBukkitCommand>();
		System.setProperty("nashorn.args", "--language=es6");
		eventManager = new EventManager(SpigotJSReloaded.getInstance());
		configManager = new ConfigManager();
		fileManager = new FileManager();
		commandManager = new CommandManager();
		taskManager = new TaskManager();
		/*
		 * Node-Support
		 */
		console = new NodeConsole();
	}

	public void loadRuntime() {
		try {
			bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			bukkitCommandMap.setAccessible(true);
			commandMap = (SimpleCommandMap) bukkitCommandMap.get(Bukkit.getServer());
			engine = null;
			ScriptEngineManager manager = new ScriptEngineManager(null);
			engine = (NashornScriptEngine) manager.getEngineByName("JavaScript");
			ScriptContext context = engine.getContext();
			Bindings bindings = engine.createBindings();
			bindings.put("PluginLogger", SpigotJSReloaded.getInstance().getLogger());
			bindings.put("CommandManager", commandManager);
			bindings.put("EventManager", eventManager);
			bindings.put("MySQL", StaticClass.forClass(MySQL.class));
			bindings.put("ConfigManager", configManager);
			bindings.put("FileManager", fileManager);
			bindings.put("TaskManager", taskManager);
			bindings.put("console", console);
			bindings.put("__Plugin", SpigotJSReloaded.getInstance());
			context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			engineContext = context;
			fileManager.setEngine(engine);
			fileManager.setEngineContext(context);
			fileManager.setModuleManager(this);
			engine.compile(""
				+ "const global = this;"
				+ "const JavaCompiler = Packages.io.github.spigotjs.compiler.JavaCompiler;"
				+ "const require = global.require = FileManager.require;"
				+ "const onEvent = (function(event, callback, priority) { if (!priority) return EventManager.on(event, callback); EventManager.on(event, callback, priority); }); "
				+ "const onCommand = (function(name, callback, prefix) { CommandManager.on(name, prefix ? prefix : name, callback); });"
				+ "const compileClass = (function(name, code) { return JavaCompiler.compileClass(name, code); });"
				+ "const setInterval = (function(func, i) { let init = false; return TaskManager.runTimer(function() { if (!init) return init = true; func(); }, (20 / 1000) * (i || 1)); });"
				+ "const setTimeout = (function(func, i) { return TaskManager.runLater(func, (20 / 1000) * (i || 1)); });"
				+ "const clearInterval = (function(input) { TaskManager.cancelTask(input); }); const clearTimeout = clearInterval;"
				+ "const execute = (function(command, player) { const Server = org.bukkit.Bukkit.getServer(); Server.dispatchCommand(player || Server.getConsoleSender(), command); });"
				+ "const doVarParse = (function(input) { const type = typeof(input); if (type === 'string') { input = input + ''; if (input.substr(0, 1) === '{' || input.substr(0, 1) === '[') { try { input = JSON.parse(input); } catch(e) {} } if (input.substr(0, 3) === 'js:') { input = eval(input.substr(3)); } } if (type === 'number') {} if (type === 'boolean') {} if (type === 'object') { if (Array.isArray(input)) { for (let x = 0; x < input.length; x++) { input[x] = doVarParse(input[x]); } } else { Object.keys(input).forEach(function(key) { input[key] = doVarParse(input[key]); }); } } return input; });"
			).eval(engineContext);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void unRegisterCommand(String cmd) {
		try {
			BukkitCommand command = (BukkitCommand) commandMap.getCommand(cmd);
			Field knownCommandsField = commandMap.getClass().getSuperclass().getDeclaredField("knownCommands");
			knownCommandsField.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<String, Command> knownCommands = (HashMap<String, Command>) knownCommandsField.get(commandMap);
			knownCommands.remove(command.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private String initModule(String folderName) {
		File folder = new File("jsmodules/" + folderName);
		if (!folder.exists()) {
			return "::Cannot find " + folderName + " folder.";
		}
		if (!folder.isDirectory()) {
			return "::Module '" + folderName + "' isn't folder, skipping...";
		}
		File res = new File("jsmodules/" + folderName + "/resource.json");
		if (!res.exists()) {
			return "::resource.json non existent.";
		}
		JSONObject config;
		try {
			config = (JSONObject) new JSONParser().parse(new String(Files.readAllBytes(res.toPath())));
		} catch (ParseException | IOException e) {
			return "::Error reading resource.json!";
		}
		if (!config.containsKey("main") || !config.containsKey("author") || !config.containsKey("version")) {
			return "::Missing required resource options.";
		}
		if (!config.containsKey("name")) {
			config.put("name", folderName);
		}
		String name = (String) config.get("name");
		if (!config.containsKey("startup")) {
			config.put("startup", true);
		}
		if (!config.containsKey("requires")) {
			config.put("requires", new JSONArray());
		}
		if (!config.containsKey("vars")) {
			config.put("vars", new JSONObject());
		}
		config.put("$maxLoadAttempts", ((JSONArray) config.get("requires")).size() * 5);
		config.put("$loadAttempts", 0);
		config.put("$started", false);
		JSONObject scripts = new JSONObject();
		File moduleFiles = new File("jsmodules/" + folderName);
		for (File file : moduleFiles.listFiles()) {
			if (file.isDirectory()) { continue; }
			if (!file.getName().endsWith(".js")) { continue; }
			try {
				String script = new String(Files.readAllBytes(file.toPath()));
				scripts.put(file.getName(), script);
			} catch (IOException e) {
				console.warn("Could not read script '" + file.getName() + "' for module '" + name + "'!");
				e.printStackTrace();
				continue;
			}
		}
		config.put("scripts", scripts);
		modules.put(name, config);
		new File("jsconfigs/" + name).mkdirs();
		return name;
	}
	
	@SuppressWarnings("unchecked")
	private boolean runModule(JSONObject config) {
		String main = (String) config.get("main");
		JSONObject scripts = (JSONObject) config.get("scripts");
		String script = (String) scripts.get(main);
		JSONObject vars = (JSONObject) config.get("vars");
		String additionalVars = "";
		Iterator<?> varNames = vars.keySet().iterator();
		while (varNames.hasNext()) {
			String varName = (String) varNames.next();
			Object val = vars.get(varName);
			if (val instanceof JSONArray || val instanceof JSONObject) {
				// val = val;
			}
			if (val instanceof Integer || val instanceof Long) {
				val = (Number) val;
			}
			if (val instanceof Float || val instanceof Double) {
				val = (Number) val;
			}
			if (val instanceof Boolean) {
				val = (Boolean) val;
			}
			if (val instanceof String) {
				JSONArray arr = new JSONArray();
				val = (String) val;
				arr.add(val);
				val = arr.toJSONString() + "[0]";
			}
			console.log(val);
			additionalVars += "let " + varName + " = doVarParse(" + val + ");";
		}
		try {
			CompiledScript compiledScript = engine.compile("(function(){ const require = (function(module) { return FileManager.require('" + config.get("name") + "', module); }); const config = (function(file, data) { return data ? FileManager.config(file, '" + config.get("name") + "', data) : FileManager.config(file, '" + config.get("name") + "'); }); let exports = {}; " + additionalVars + " " + script + "; return exports; })();");
			compiledScript.eval(engineContext);
		} catch (ScriptException e) {
			console.warn("Could not run module '" + config.get("name") + "' on startup!");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean loadingSize() {
		return moduleLoading.size() > 0;
	}
	
	@SuppressWarnings("unchecked")
	public void loadModules() {
		loadRuntime();
		for (ModuleBukkitCommand cmd : moduleBukkitCommands) {
			unRegisterCommand(cmd.getName());
		}
		HandlerList.unregisterAll((Plugin) SpigotJSReloaded.getInstance());
		Bukkit.getScheduler().cancelTasks(SpigotJSReloaded.getInstance());
		moduleBukkitCommands.clear();
		modules = new HashMap<String, JSONObject>();
		moduleDone = new ArrayList<String>();
		moduleError = new ArrayList<String>();
		moduleLoading = new ArrayList<String>();
		for (File folder : modulesDirectory.listFiles()) {
			String name = initModule(folder.getName());
			if (name.startsWith("::")) {
				console.error("Module issue: 'jsmodules/" + folder.getName() + "'");
				console.error(name);
				continue;
			}
			moduleLoading.add(name);
		}
		while (loadingSize()) {
			ArrayList<String> toRemove = new ArrayList<String>();
			Iterator<String> loadIte = moduleLoading.iterator();
			while (loadIte.hasNext()) {
				String name = loadIte.next();
				JSONObject config = modules.get(name);
				if (config.get("startup").equals(false)) {
					toRemove.add(name);
					continue;
				}
				JSONArray depends = (JSONArray) config.get("requires");
				Integer inactiveDepends = depends.size();
				Integer activeDepends = 0;
				Iterator<String> depIte = depends.iterator();
				while (depIte.hasNext()) {
					String dName = depIte.next();
					if (moduleDone.contains(dName)) {
						activeDepends++;
					}
				}
				if (inactiveDepends.equals(activeDepends)) {
					if (runModule(config)) {
						config.put("$started",  true);
						toRemove.add(name);
						moduleDone.add(name);
					} else {
						toRemove.add(name);
						moduleError.add(name);
						console.error("Could not run module '" + name + "'!");
					}
					continue;
				}
				Integer loadAttempts = (Integer) config.get("$loadAttempts") + 1;
				config.put("$loadAttempts", loadAttempts);
				if (config.get("$loadAttempts") == config.get("$maxLoadAttempts")) {
					toRemove.add(name);
					moduleError.add(name);
					JSONArray thedepends = (JSONArray) config.get("requires");
					thedepends.removeIf(m -> moduleDone.contains(m));
					console.error("Missing dependencies for " + name + ": " + thedepends);
				}
			}
			Iterator<String> removeIte = toRemove.iterator();
			while (removeIte.hasNext()) {
				moduleLoading.remove(removeIte.next());
			}
		}
		console.info("Load Complete! Loaded " + modules.size() + " modules, started " + moduleDone.size() + " modules, with " + moduleError.size() + " modules erroring out!");
	}

}
