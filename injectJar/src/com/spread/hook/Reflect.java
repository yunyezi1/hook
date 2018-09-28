package com.spread.hook;

import android.annotation.SuppressLint;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Reflect {
	private final boolean isClass;
	private final Object object;

	private Reflect(Class<?> cls) {
		this.object = cls;
		this.isClass = true;
	}

	private Reflect(Object obj) {
		this.object = obj;
		this.isClass = false;
	}

	public static <T extends AccessibleObject> T accessible(T target) {
		if (target == null || target.isAccessible())
			return target;
		Member member = null;
		if (target instanceof Member) {
			member = (Member) target;
			if (Modifier.isPublic(member.getModifiers()))
				return target;
		}
		target.setAccessible(true);
		return target;
	}

	private Method exactMethod(String methodName, Class<?>[] methodTypes)
			throws NoSuchMethodException {
		Class<?> typeObj = type();
		try {
			Method method = typeObj.getMethod(methodName, methodTypes);
			return method;
		} catch (NoSuchMethodException e) {
			Class<?> mcls = null;
			do {
				try {
					Method method = typeObj.getDeclaredMethod(methodName,
							methodTypes);
					return method;
				} catch (NoSuchMethodException e1) {
					mcls = typeObj.getSuperclass();
					typeObj = mcls;
				}
			} while (mcls != null);
			throw new NoSuchMethodException();
		}
	}

	private Field field0(String fdname) throws ReflectException {
		Class<?> typeObj = type();
		try {
			Field fd = typeObj.getField(fdname);
			return fd;
		} catch (NoSuchFieldException e) {
			Class<?> fdClass = null;
			do {
				try {
					Field fd = (Field) accessible(typeObj
							.getDeclaredField(fdname));
					return fd;
				} catch (NoSuchFieldException e1) {
					fdClass = typeObj.getSuperclass();
					typeObj = fdClass;
				}
			} while (fdClass != null);
			throw new ReflectException(e);
		}
	}

	private static Class<?> forName(String clsName) throws ReflectException {
		try {
			Class<?> cls = Class.forName(clsName);
			return cls;
		} catch (Exception e) {
			throw new ReflectException(e);
		}
	}

	private boolean isSimilarSignature(Method method, String mdname,
			Class<?>[] mdParams) {
		return (method.getName().equals(mdname))
				&& (match(method.getParameterTypes(), mdParams));
	}

	private boolean match(Class<?>[] left, Class<?>[] right) {
		if (left.length != right.length) {
			return false;
		}
		for (int i = 0; i < left.length; i++) {
			if(right[i] == NULL.class)
				continue;
			if (!wrapper(left[i]).isAssignableFrom(wrapper(right[i]))) {
				return false;
			}
		}
		return true;
	}

	public static Reflect on(Class<?> cls) {
		return new Reflect(cls);
	}

	public static Reflect on(Object obj) {
		return new Reflect(obj);
	}

	public static Reflect on(String clsName) throws ReflectException {
		return on(forName(clsName));
	}

	private static Reflect on(Constructor<?> constructor, Object... params)
			throws ReflectException {
		try {
			Reflect reflect = on((accessible(constructor)).newInstance(params));
			return reflect;
		} catch (Exception e) {
			throw new ReflectException(e);
		}
	}

	private static Reflect on(Method method, Object obj, Object... args)
			throws ReflectException {
		try {
			method = accessible(method);
			if (method.getReturnType() == Void.TYPE) {
				method.invoke(obj, args);
				return on(obj);
			}
			Reflect reflect = on(method.invoke(obj, args));
			return reflect;
		} catch (Exception e) {
			throw new ReflectException(e);
		}
	}

	@SuppressLint("DefaultLocale") 
	private static String property(String name) {
		int i = name.length();
		if (i == 0) {
			return "";
		}
		if (i == 1) {
			return name.toLowerCase();
		}
		return name.substring(0, 1).toLowerCase() + name.substring(1);
	}

	private Method similarMethod(String mdname, Class<?>[] mdparams)
			throws NoSuchMethodException {
		Class<?> selfClass = type();
		do {
			Method[] selfMethods = selfClass.getDeclaredMethods();
			for (Method method : selfMethods) {
				if (isSimilarSignature(method, mdname, mdparams)) {
					return method;
				}
			}
			selfClass = selfClass.getSuperclass();
		} while (selfClass != null);
		return null;
	}

	private static Class<?>[] types(Object... args) {
		if (args == null || args.length <= 0)
			return new Class<?>[0];
		Class<?>[] result = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			if(args[i] == null){
				result[i] = NULL.class;
			}else{
				result[i] = args[i].getClass();
			}
		}
		return result;
	}

	private static Object unwrap(Object paramObject) {
		Object localObject = paramObject;
		if ((paramObject instanceof Reflect)) {
			localObject = ((Reflect) paramObject).get();
		}
		return localObject;
	}

	public static Class<?> wrapper(Class<?> cls) {
		if (cls == null)
			return Void.class;
		if (!cls.isPrimitive())
			return cls;
		if (Boolean.TYPE == cls) {
			return Boolean.class;
		} else if (Integer.TYPE == cls) {
			return Integer.class;
		} else if (Long.TYPE == cls) {
			return Long.class;
		} else if (Short.TYPE == cls) {
			return Short.class;
		} else if (Byte.TYPE == cls) {
			return Byte.class;
		} else if (Double.TYPE == cls) {
			return Double.class;
		} else if (Float.TYPE == cls) {
			return Float.class;
		} else if (Character.TYPE == cls) {
			return Character.class;
		}
		return Void.class;
	}

	@SuppressWarnings("unchecked")
	public <P> P as(Class<P> cls) {
		InvocationHandler local1 = new InvocationHandler() {
			@Override
			public Object invoke(Object obj, Method method, Object[] params)
					throws Throwable {
				String name = method.getName();
				try {
					Reflect reflect = Reflect.on(Reflect.this.object)
							.call(name, params).get();
					return reflect;
				} catch (ReflectException e) {
					if (Reflect.this.object instanceof Map) {
						Map localMap = (Map) Reflect.this.object;
						if (params == null) {
							return null;
						}
						if (name.startsWith("get")) {
							return localMap.get(Reflect.property(name
									.substring(3)));
						} else if (name.startsWith("is")) {
							return localMap.get(Reflect.property(name
									.substring(2)));
						} else if (name.startsWith("set")) {
							for (int i = 0; i < params.length; i++) {
								localMap.put((Object) Reflect.property(name
										.substring(3)), params[0]);
							}
							return null;
						}
					}
					throw new ReflectException();
				}
			}
		};
		return (P) Proxy.newProxyInstance(cls.getClassLoader(),
				new Class<?>[] { cls }, local1);
	}

	public Reflect call(String mdname) throws ReflectException {
		return call(mdname, new Object[0]);
	}

	public Reflect call(String mdname, Object... args) throws ReflectException {
		Class<?>[] arrayOfClass = types(args);
		try {
			Reflect localReflect = on(exactMethod(mdname, arrayOfClass),
					this.object, args);
			return localReflect;
		} catch (NoSuchMethodException e) {
			try {
				Reflect reflect = on(similarMethod(mdname, arrayOfClass),
						this.object, args);
				return reflect;
			} catch (NoSuchMethodException ex) {
				throw new ReflectException(ex);
			}
		}
	}

	public Reflect create() throws ReflectException {
		return create(new Object[0]);
	}

	public Reflect create(Object... args) throws ReflectException {
		Class<?>[] arrayOfClass = types(args);
		Constructor<?>[] arrayOfConstructor;
		try {
			Reflect localReflect = on(
					type().getDeclaredConstructor(arrayOfClass), args);
			return localReflect;
		} catch (NoSuchMethodException e) {
			arrayOfConstructor = type().getDeclaredConstructors();
			if (arrayOfConstructor.length <= 0) {
				throw new ReflectException(e.toString());
			}
			for (int i = 0; i < arrayOfConstructor.length; i++) {
				Constructor<?> localConstructor = arrayOfConstructor[i];
				if (match(localConstructor.getParameterTypes(), arrayOfClass)) {
					return on(localConstructor, args);
				}
			}
			throw new ReflectException(e.toString());
		}
	}

	public boolean equals(Object obj) {
		if ((obj instanceof Reflect)) {
			return this.object.equals(((Reflect) obj).get());
		}
		return false;
	}

	public Reflect field(String fdname) throws ReflectException {
		try {
			Reflect reflect = on(field0(fdname).get(this.object));
			return reflect;
		} catch (Exception e) {
			throw new ReflectException(e);
		}
	}

	public Map<String, Reflect> fields() {
		LinkedHashMap<String, Reflect> linkedHashMap = new LinkedHashMap<String, Reflect>();
		Class<?> selfClass = type();
		Field[] selfFields = null;
		do {
			if (selfClass == null)
				break;
			selfFields = selfClass.getDeclaredFields();
			if (selfFields == null || selfFields.length <= 0)
				selfClass = selfClass.getSuperclass();
		} while (selfFields == null);
		if (selfFields == null)
			return linkedHashMap;
		for (Field field : selfFields) {
			if (!Modifier.isStatic(field.getModifiers())) {
				linkedHashMap.put(field.getName(), field(field.getName()));
			}
		}
		return linkedHashMap;
	}

	@SuppressWarnings("unchecked")
	public <T> T get() {
		return (T) this.object;
	}

	public <T> T get(String paramString) throws ReflectException {
		return field(paramString).get();
	}

	public int hashCode() {
		return this.object.hashCode();
	}

	public Reflect set(String fdname, Object obj) throws ReflectException {
		try {
			field0(fdname).set(this.object, unwrap(obj));
			return this;
		} catch (Exception e) {
			throw new ReflectException(e);
		}
	}

	public String toString() {
		return this.object.toString();
	}

	public Class<?> type() {
		if (this.isClass) {
			return (Class<?>) this.object;
		}
		return this.object.getClass();
	}

	private static class NULL {
	}
}