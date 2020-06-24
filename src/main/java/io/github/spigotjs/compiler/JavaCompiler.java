package io.github.spigotjs.compiler;

public class JavaCompiler {
	
	public static Class<?> compileClass(String className, String code) {
		Compiler compiler = new Compiler();
		compiler.addClass(className, code);
		compiler.compile();
		return compiler.getClass();
	}

}
