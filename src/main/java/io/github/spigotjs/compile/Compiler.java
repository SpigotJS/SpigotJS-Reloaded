package io.github.spigotjs.compile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

public class Compiler {

	private final JavaCompiler javaCompiler;
	private final Map<String, byte[]> classData;
	private final MapClassLoader mapClassLoader;
	private final ClassDataFileManager classDataFileManager;
	private final List<JavaFileObject> compilationUnits;

	public Compiler() {
		this.javaCompiler = ToolProvider.getSystemJavaCompiler();
		if (javaCompiler == null) {
			throw new NullPointerException(
					"No JavaCompiler found. Make sure to run this with a JDK, and not only with a JRE");
		}
		this.classData = new LinkedHashMap<String, byte[]>();
		this.mapClassLoader = new MapClassLoader(classData);
		this.classDataFileManager = new ClassDataFileManager(javaCompiler.getStandardFileManager(null, null, null), classData);
		this.compilationUnits = new ArrayList<JavaFileObject>();
	}

	public void addClass(String className, String code) {
		String javaFileName = className + ".java";
		JavaFileObject javaFileObject = new MemoryJavaSourceFileObject(javaFileName, code);
		compilationUnits.add(javaFileObject);
	}

	public boolean compile() {
		DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<JavaFileObject>();
		CompilationTask task = javaCompiler.getTask(null, classDataFileManager, diagnosticsCollector, null, null,
				compilationUnits);
		boolean success = task.call();
		compilationUnits.clear();
		for (Diagnostic<?> diagnostic : diagnosticsCollector.getDiagnostics()) {
			System.out.println(diagnostic.getKind() + " : " + diagnostic.getMessage(null));
			System.out.println("Line " + diagnostic.getLineNumber() + " of " + diagnostic.getSource());
			System.out.println();
		}
		return success;
	}

	public Class<?> getCompiledClass(String className) {
		return mapClassLoader.findClass(className);
	}


}