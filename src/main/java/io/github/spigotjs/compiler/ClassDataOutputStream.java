package io.github.spigotjs.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class ClassDataOutputStream extends OutputStream {
	/**
	 * The name of the class that the received class data represents
	 */
	private final String className;

	/**
	 * The output stream that will receive the class data
	 */
	private final ByteArrayOutputStream baos;
	
	private final Map<String, byte[]> classData;

	/**
	 * Creates a new output stream that will store the class data for the class with
	 * the given name
	 * 
	 * @param className The class name
	 */
	public ClassDataOutputStream(String className, Map<String, byte[]> classData) {
		this.className = className;
		this.classData = classData;
		this.baos = new ByteArrayOutputStream();
	}

	@Override
	public void write(int b) throws IOException {
		baos.write(b);
	}

	@Override
	public void close() throws IOException {
		classData.put(className, baos.toByteArray());
		super.close();
	}
}
