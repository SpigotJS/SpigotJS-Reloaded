package io.github.spigotjs.compile;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

import javax.tools.SimpleJavaFileObject;

public class MemoryJavaClassFileObject extends SimpleJavaFileObject {
	
	private final Map<String, byte[]> classData;
	
	/**
	 * The name of the class represented by the file object
	 */
	private final String className;

	public MemoryJavaClassFileObject(String className, Map<String, byte[]> classData) {
		super(URI.create("string:///" + className + ".class"), Kind.CLASS);
		this.className = className;
		this.classData = classData;
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		return new ClassDataOutputStream(className, classData);
	}
}