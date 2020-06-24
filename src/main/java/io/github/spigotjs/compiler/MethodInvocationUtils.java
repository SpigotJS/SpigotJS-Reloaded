package io.github.spigotjs.compiler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodInvocationUtils {
	/**
	 * Utility method to invoke the first static method in the given class that can
	 * accept the given parameters.
	 * 
	 * @param c          The class
	 * @param methodName The method name
	 * @param args       The arguments for the method call
	 * @return The return value of the method call
	 * @throws RuntimeException If either the class or a matching method could not
	 *                          be found
	 */
	public static Object invokeStaticMethod(Class<?> c, String methodName, Object... args) {
		Method m = findFirstMatchingStaticMethod(c, methodName, args);
		if (m == null) {
			throw new RuntimeException("No matching method found");
		}
		try {
			return m.invoke(null, args);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Utility method to find the first static method in the given class that has
	 * the given name and can accept the given arguments. Returns <code>null</code>
	 * if no such method can be found.
	 * 
	 * @param c          The class
	 * @param methodName The name of the method
	 * @param args       The arguments
	 * @return The first matching static method.
	 */
	private static Method findFirstMatchingStaticMethod(Class<?> c, String methodName, Object... args) {
		Method methods[] = c.getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().equals(methodName) && Modifier.isStatic(m.getModifiers())) {
				Class<?>[] parameterTypes = m.getParameterTypes();
				if (areAssignable(parameterTypes, args)) {
					return m;
				}
			}
		}
		return null;
	}

	/**
	 * Returns whether the given arguments are assignable to the respective types
	 * 
	 * @param types The types
	 * @param args  The arguments
	 * @return Whether the arguments are assignable
	 */
	private static boolean areAssignable(Class<?> types[], Object... args) {
		if (types.length != args.length) {
			return false;
		}
		for (int i = 0; i < types.length; i++) {
			Object arg = args[i];
			Class<?> type = types[i];
			if (arg != null && !type.isAssignableFrom(arg.getClass())) {
				return false;
			}
		}
		return true;
	}

}
