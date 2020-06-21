package io.github.spigotjs.script;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.coveo.nashorn_modules.FilesystemFolder;
import com.coveo.nashorn_modules.Require;

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

@Getter
public class ScriptManager {

	private NashornScriptEngine engine;
	private List<ScriptResource> scriptResources;
	private File scriptDirectory;
	private ScriptContext engineContext;
	
	private List<ScriptBukkitCommand> scriptBukkitCommands;
	
	private Field bukkitCommandMap;
	private CommandMap commandMap;
	
	private CommandManager commandManager;
	private EventManager eventManager;
	private ConfigManager configManager;
	private FileManager fileManager;
	private TaskManager taskManager;
	
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
			commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
			
			engine = null;
			ScriptEngineManager manager = new ScriptEngineManager(null);
			engine = (NashornScriptEngine) manager.getEngineByName("JavaScript");
			Require.enable(engine, FilesystemFolder.create(scriptDirectory, "UTF-8"));
			ScriptContext context = engine.getContext();
			Bindings bindings = engine.createBindings();

			for(ScriptDeclarationAddon declareAddon : SpigotJSReloaded.getInstance().getScriptAddonManager().getDeclarationAddons()) {
				bindings.put(declareAddon.getClassName(), declareAddon.getClass());
				//SpigotJSReloaded.getInstance().getLogger().info("Registered new declaration \"" + declareAddon.getClassName() + "\" for class \"" + declareAddon.getTargetClass().getName() + "\" by " + declareAddon.getSource());
			}
			bindings.put("PluginLogger", SpigotJSReloaded.getInstance().getLogger());
			bindings.put("CommandManager", commandManager);
			bindings.put("EventManager", eventManager);
			bindings.put("MySQL", StaticClass.forClass(MySQL.class));
			bindings.put("ConfigManager", configManager);
			bindings.put("FileManager", fileManager);
			bindings.put("TaskManager", taskManager);
			bindings.put("TaskManager", taskManager);
			bindings.put("console", console);
			bindings.put("__Plugin", SpigotJSReloaded.getInstance());
			context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
			engineContext = context;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void unRegisterCommand(String cmd) {
		try {
			BukkitCommand command = (BukkitCommand) commandMap.getCommand(cmd);
			Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
			knownCommandsField.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<String, Command> knownCommands = (HashMap<String, Command>) knownCommandsField.get(commandMap);
			knownCommands.remove(command.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadScripts() {
		loadRuntime();
		for (ScriptBukkitCommand cmd : scriptBukkitCommands) {
			unRegisterCommand(cmd.getName());
		}
		HandlerList.unregisterAll((Plugin) SpigotJSReloaded.getInstance());
		Bukkit.getScheduler().cancelTasks(SpigotJSReloaded.getInstance());
		scriptBukkitCommands.clear();
		for (File file : scriptDirectory.listFiles()) {
			if (!file.isDirectory()) {
				SpigotJSReloaded.getInstance().getLogger()
						.warning("There is a file in the \"scripts\" directory, that isnt a folder.");
				
			} else {
				loadResource(file);
			}
			
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
		Require.enable(engine, ScriptFolder.create(new File("./scripts/"), "UTF-8"));
		CompiledScript compiledScript = engine.compile(codeFromAddons + script);
		compiledScript.eval(engineContext);
	}

}
