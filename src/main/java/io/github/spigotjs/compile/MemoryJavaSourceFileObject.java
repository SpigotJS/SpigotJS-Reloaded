package io.github.spigotjs.compile;

import java.io.IOException;
import java.net.URI;

import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;

public class MemoryJavaSourceFileObject extends SimpleJavaFileObject {

	private final String code;

	public MemoryJavaSourceFileObject(String fileName, String code) {
		super(URI.create("string:///" + fileName), Kind.SOURCE);
		this.code = code;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		return code;
	}
}