package io.github.spigotjs.support;

import io.github.spigotjs.SpigotJSReloaded;

import org.apache.commons.lang.ClassUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NodeConsole {
	
	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public void output(String type, Object obj, boolean onlyString) {
		if (!onlyString) {
			boolean isPrimitiveOrWrapped = obj.getClass().isPrimitive() || ClassUtils.wrapperToPrimitive(obj.getClass()) != null;
			if(isPrimitiveOrWrapped) {
				System.out.println(obj);
				return;
			}
		}
		String msg = onlyString ? (String) obj : gson.toJson(obj);
		msg = "[SJS] " + msg;
		if (type.equals("[Info]")) SpigotJSReloaded.getInstance().getLogger().info(msg);
		if (type.equals("[Warn]")) SpigotJSReloaded.getInstance().getLogger().warning(msg);
		if (type.equals("[Debug]")) SpigotJSReloaded.getInstance().getLogger().info(msg);
		if (type.equals("[Log]")) SpigotJSReloaded.getInstance().getLogger().fine(msg);
		if (type.equals("[Error]")) SpigotJSReloaded.getInstance().getLogger().severe(msg);
	}
	
	public void info(Object obj) {
		output("[Info]", obj, false);
	}
	
	public void debug(Object obj) {
		output("[Debug]", obj, false);
	}
	
	public void error(Object obj) {
		output("[Error]", obj, false);
	}
	
	public void warn(Object obj) {
		output("[Warn]", obj, false);
	}
	
	public void log(Object obj) {
		output("[Log]", obj, false);
	}
	
	public void info(String obj) {
		output("[Info]", obj, true);
	}
	
	public void debug(String obj) {
		output("[Debug]", obj, true);
	}
	
	public void error(String obj) {
		output("[Error]", obj, true);
	}
	
	public void warn(String obj) {
		output("[Warn]", obj, true);
	}
	
	public void log(String obj) {
		output("[Log]", obj, true);
	}
}
