package io.github.spigotjs.support;

import org.apache.commons.lang.ClassUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class NodeConsole {

	Gson gson = new GsonBuilder().setPrettyPrinting().create();
	
	public void log(Object obj) {
		boolean isPrimitiveOrWrapped = obj.getClass().isPrimitive() || ClassUtils.wrapperToPrimitive(obj.getClass()) != null;
		if(isPrimitiveOrWrapped) {
			System.out.println(obj.toString());
			return;
		}
		System.out.println(gson.toJson(obj));
	}
	
	public void info(Object obj) {
		log(obj);
	}

	public void debug(Object obj) {
		log(obj);
	}
	
	public void error(Object obj) {
		log(obj);
	}
	
	public void warn(Object obj) {
		log(obj);
	}
}
