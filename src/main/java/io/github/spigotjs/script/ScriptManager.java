package io.github.spigotjs.script;

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
import io.github.spigotjs.script.addons.ScriptCodeAddon;
import io.github.spigotjs.script.addons.ScriptDeclarationAddon;
import io.github.spigotjs.support.NodeConsole;
import io.github.spigotjs.utils.ScriptBukkitCommand;
import jdk.internal.dynalink.beans.StaticClass;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import lombok.Getter;

@SuppressWarnings({ "restriction", "unused" })
@Getter
public class ScriptManager {
	private NashornScriptEngine engine;
	private List<ScriptResource> scriptResources;
	private File scriptDirectory;
	private ScriptContext engineContext;
	private List<ScriptBukkitCommand> scriptBukkitCommands;
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
	public ScriptManager() {
		scriptResources = new ArrayList<ScriptResource>();
		scriptDirectory = new File("scripts/");
		scriptDirectory.mkdir();
		scriptBukkitCommands = new ArrayList<ScriptBukkitCommand>();
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
		codeFromAddons = "";
		for(ScriptCodeAddon code : SpigotJSReloaded.getInstance().getScriptAddonManager().getCodeAddons()) {
			codeFromAddons += (code.getCode() + ";");
		}
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
			for(ScriptDeclarationAddon declareAddon : SpigotJSReloaded.getInstance().getScriptAddonManager().getDeclarationAddons()) {
				bindings.put(declareAddon.getClassName(), declareAddon.getClass());
				console.info("Registered new declaration \"" + declareAddon.getClassName() + "\" for class \"" + declareAddon.getTargetClass().getName() + "\" by " + declareAddon.getSource());
			}
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
            fileManager.setScriptManager(this);
            engine.compile("const global = this; const require = global.require = FileManager.require;").eval(engineContext);
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
        File folder = new File("scripts/" + folderName);
        if (!folder.exists()) {
        	return "::Cannot find module folder.";
        }
        if (!folder.isDirectory()) {
        	return "::Module '" + folderName + "' isn't folder, skipping...";
        }
        File res = new File("scripts/" + folderName + "/resource.json");
        if (!res.exists()) {
        	return "::Resource.json non existent.";
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
        config.put("$maxLoadAttempts", ((JSONArray) config.get("requires")).size() * 5);
        config.put("$loadAttempts", 0);
        config.put("$started", false);
        JSONObject scripts = new JSONObject();
        File moduleFiles = new File("scripts/" + folderName);
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
        return name;
    }
	
    private boolean runModule(JSONObject config) {
    	String main = (String) config.get("main");
    	JSONObject scripts = (JSONObject) config.get("scripts");
    	String script = (String) scripts.get(main);
    	try {
			CompiledScript compiledScript = engine.compile("(function(){const require = (function(module) { return FileManager.require('" + config.get("name") + "', module); }); let exports = {};" + script + ";return exports;})();");
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
	public void loadScripts() {
		loadRuntime();
		for (ScriptBukkitCommand cmd : scriptBukkitCommands) {
			unRegisterCommand(cmd.getName());
		}
		HandlerList.unregisterAll((Plugin) SpigotJSReloaded.getInstance());
		Bukkit.getScheduler().cancelTasks(SpigotJSReloaded.getInstance());
		scriptBukkitCommands.clear();
        modules = new HashMap<String, JSONObject>();
        moduleDone = new ArrayList<String>();
        moduleError = new ArrayList<String>();
        moduleLoading = new ArrayList<String>();
        for (File folder : scriptDirectory.listFiles()) {
            String name = initModule(folder.getName());
            if (name.startsWith("::")) {
                console.error("Module issue: 'scripts/" + folder.getName() + "'");
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
