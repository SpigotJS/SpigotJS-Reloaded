package io.github.spigotjs.compiler;

import java.util.Map;

public class MapClassLoader extends ClassLoader {
	
	private final Map<String, byte[]> classData;
	
	public MapClassLoader(Map<String, byte[]> classData) {
		this.classData = classData;
	}
	
	@Override
	public Class<?> findClass(String name) {
		byte[] b = classData.get(name);
		return defineClass(name, b, 0, b.length);
	}
}