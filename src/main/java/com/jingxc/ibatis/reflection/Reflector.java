package com.jingxc.ibatis.reflection;

import com.jingxc.ibatis.reflection.invoker.*;
import com.jingxc.ibatis.reflection.property.PropertyNamer;
import com.jingxc.ibatis.util.MapUtil;

import java.lang.reflect.*;
import java.text.MessageFormat;
import java.util.*;

public class Reflector {

    private final Class<?> type;
    private Constructor<?> defaultConstructor;

    // 存储get属性方法
    private final Map<String, Invoker> getMethods = new HashMap<>();

    // 存储get属性方法返回类型
    private final Map<String, Class<?>> getTypes = new HashMap<>();

    // 存储set属性方法
    private final Map<String, Invoker> setMethods = new HashMap<>();

    // 存储set属性方法返回值
    private final Map<String, Class<?>> setTypes = new HashMap<>();

    // 读属性名称集
    private final String[] readablePropertyNames;

    // 写属性名称集
    private final String[] writablePropertyNames;

    private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

    public Reflector(Class<?> clazz) {

        type = clazz;
        // 无参构造器
        addDefaultConstructor(clazz);
        // get方法
        addGetMethods(clazz);
        // set方法
        addSetMethods(clazz);
        // 属性
        addFields(clazz);

        // 读属性名称集
        this.readablePropertyNames = getMethods.keySet().toArray(new String[0]);
        // 写属性名称集
        this.writablePropertyNames = setMethods.keySet().toArray(new String[0]);

        for (String propName : readablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }

        for (String propName : writablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }

    }

    /**
     * 属性设置
     *
     * @param clazz
     */
    private void addFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!setMethods.containsKey(field.getName())) {
                int modifiers = field.getModifiers();
                if (!(Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers))) {
                    // 既不是静态方法也不是final方法
                    addSetField(field);
                }
            }
            if (!getMethods.containsKey(field.getName())) {
                addGetField(field);
            }
        }
        if (clazz.getSuperclass() != null) {
            addFields(clazz.getSuperclass());
        }

    }

    private void addGetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            getMethods.put(field.getName(), new GetFieldInvoker(field));
            Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
            getTypes.put(field.getName(), typeToClass(fieldType));
        }
    }

    private void addSetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            setMethods.put(field.getName(), new SetFieldInvoker(field));
            Type fieldType = TypeParameterResolver.resolveFieldType(field, type);
            setTypes.put(field.getName(), typeToClass(fieldType));
        }
    }

    /**
     * 设置set方法
     *
     * @param clazz
     */
    private void addSetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingSetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        Arrays.stream(methods).filter(m -> m.getParameterTypes().length == 1 && PropertyNamer.isSetter(m.getName()))
                .forEach(m -> addMethodConflict(conflictingSetters, PropertyNamer.methodToProperty(m.getName()), m));
        // 给set属性赋值
        resolveSetterConflicts(conflictingSetters);
    }

    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
        for (Map.Entry<String, List<Method>> entry : conflictingSetters.entrySet()) {
            String propName = entry.getKey();
            List<Method> setters = entry.getValue();
            Class<?> getterType = getTypes.get(propName);

            boolean isGetterAmbiguous = getMethods.get(propName) instanceof AmbiguousMethodInvoker;
            boolean isSetterAmbiguous = false;
            Method match = null;
            for (Method setter : setters) {
                // 如果set的入参是get的返回值
                if (!isGetterAmbiguous && setter.getParameterTypes()[0].equals(getterType)) {
                    match = setter;
                    break;
                }
                if (!isSetterAmbiguous) {
                    match = pickBetterSetter(match, setter, propName);
                    isSetterAmbiguous = match == null;
                }
            }
        }
    }

    private Method pickBetterSetter(Method setter1, Method setter2, String propName) {
        if (setter1 == null) {
            return setter2;
        }
        Class<?> parameterType1 = setter1.getParameterTypes()[0];
        Class<?> parameterType2 = setter2.getParameterTypes()[0];
        if (parameterType1.isAssignableFrom(parameterType2)) {
            // setter1是setter2父类
            return setter2;
        } else if (parameterType2.isAssignableFrom(parameterType1)) {
            // setter2是setter1父类
            return setter1;
        }

        AmbiguousMethodInvoker invoker = new AmbiguousMethodInvoker(setter1, MessageFormat.format(
                "为类“{1}”中的属性“{0}”定义了类型为“{2}”和“{3}”的模糊setter。",
                propName, setter2.getDeclaringClass().getName(), parameterType1.getName(), parameterType2.getName()));
        // 设置set属性方法
        setMethods.put(propName, invoker);
        Type[] paramTypes = TypeParameterResolver.resolveParamTypes(setter1, type);
        // 设置set属性方法返回类型
        setTypes.put(propName, typeToClass(paramTypes[0]));
        return null;
    }

    /**
     * 设置get方法
     *
     * @param clazz
     */
    private void addGetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingGetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        // 过滤方法参数个数为0，并且以get开头的方法
        Arrays.stream(methods).filter(m -> m.getParameterTypes().length == 0 && PropertyNamer.isGtter(m.getName()))
                // 循环每一个方法并赋值存储m
                .forEach(m -> addMethodConflict(conflictingGetters, PropertyNamer.methodToProperty(m.getName()), m));

        resolveGetterConflicts(conflictingGetters);
    }

    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
        // 可能存在这样的方法isMethod和getMethod，去除排头时都一样,非法重载
        for (Map.Entry<String, List<Method>> entry : conflictingGetters.entrySet()) {
            Method winner = null;
            String propName = entry.getKey();
            boolean isAmbiguous = false;
            for (Method candidate : entry.getValue()) {
                // 如果在List<Method>是多个元素，则直接先把[0]选为默认，然后在循环进行比对
                if (winner == null) {
                    winner = candidate;
                    continue;
                }
                Class<?> winnerReturnType = winner.getReturnType();
                Class<?> candidateReturnType = candidate.getReturnType();
                // 判断返回只类型是否是同一返回类型
                if (candidateReturnType.equals(winnerReturnType)) {
                    if (!boolean.class.equals(candidateReturnType)) {
                        // candidateReturnType不是boolean类型
                        isAmbiguous = true;
                        break;
                    } else if (candidateReturnType.isAssignableFrom(winnerReturnType)) {
                        // winnerReturnType是candidateReturnType的子类，get方法返回的是子类
                    } else if (winnerReturnType.isAssignableFrom(candidateReturnType)) {
                        // candidateReturnType是winnerReturnType的子类
                        winner = candidate;
                    } else {
                        isAmbiguous = true;
                        break;
                    }
                }
            }
            addGetMethod(propName, winner, isAmbiguous);
        }
    }

    private void addGetMethod(String name, Method method, boolean isAmbiguous) {
        MethodInvoker invoker = isAmbiguous ? new AmbiguousMethodInvoker(method, MessageFormat.format(
                "类“{1}”中属性“{0}”类型不明确的非法重载getter方法。这违反了JavaBeans规范，并可能导致不可预测的结果。",
                name, method.getDeclaringClass().getName())) : new MethodInvoker(method);
        getMethods.put(name, invoker);
        // 获取返回类型，内部结构比较复杂
        Type returnType = TypeParameterResolver.resolveReturnType(method, type);
        getTypes.put(name, typeToClass(returnType));
    }

    private Class<?> typeToClass(Type src) {
        Class<?> result = null;
        if (src instanceof Class) {
            result = (Class<?>) src;
        } else if (src instanceof ParameterizedType) {
            // ParameterizedType是参数化类型，即带有泛型的类型，比如List<String>、Set<Long>、Map<String, Long>、Class<Float>等类型。
            // 该方法是获取对应的原始类型，变量arg是List<String>类型，那么arg的原始类型就是List。
            result = (Class<?>) ((ParameterizedType) src).getRawType();
        } else if (src instanceof GenericArrayType) {
            // GenericArrayType是Type的子接口，用于表示“泛型数组”，描述的是形如：A<T>[]或T[]的类型
            // 获取“泛型数组”中元素的类型，要注意的是：无论从左向右有几个[]并列，这个方法仅仅脱去最右边的[]之后剩下的内容就作为这个方法的返回值。
            Type componentType = ((GenericArrayType) src).getGenericComponentType();
            if (componentType instanceof Class) {
                result = Array.newInstance((Class<?>) componentType, 0).getClass();
            } else {
                Class<?> componentClass = typeToClass(componentType);
                result = Array.newInstance(componentClass, 0).getClass();
            }
        }
        if (result == null) {
            result = Object.class;
        }
        return result;
    }

    private void addMethodConflict(Map<String, List<Method>> conflictingGetters, String name, Method m) {
        // 如果不是以某些开头的方法
        if (isValidPropertyName(name)) {
            // 给Map赋值，没有新创建并负责，否则直接追加
            List<Method> methods = MapUtil.computeIfAbsent(conflictingGetters, name, k -> new ArrayList<>());
            methods.add(m);
        }
    }

    /**
     * 排除$,serialVersionUID,class属性方法
     *
     * @param name
     * @return
     */
    private boolean isValidPropertyName(String name) {
        return !(name.startsWith("$") || "serialVersionUID".equals(name) || "class".equals(name));
    }

    /**
     * 获取class的所有方法
     *
     * @param clazz
     * @return
     */
    private Method[] getClassMethods(Class<?> clazz) {
        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {

            // 自己内部方法
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

            // 所实现的接口的方法
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                addUniqueMethods(uniqueMethods, anInterface.getDeclaredMethods());
            }
            // 通过自循环获取父类的方法
            currentClass = currentClass.getSuperclass();
        }

        Collection<Method> methods = uniqueMethods.values();
        return methods.toArray(new Method[0]);

    }

    /**
     * 存储方法模版
     *
     * @param uniqueMethods
     * @param methods
     */
    private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            if (!currentMethod.isBridge()) {
                String signature = getSignature(currentMethod);
                // 如果不存在则存储
                if (!uniqueMethods.containsKey(signature)) {
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }

    /**
     * 转换获取方法格式：String#myMethod:String,Integer
     *
     * @param method
     * @return
     */
    private String getSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        if (returnType != null) {
            sb.append(returnType.getName()).append("#");
        }
        sb.append(method.getName());
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(i == 0 ? ':' : ',').append(parameterTypes[i].getName());
        }
        return sb.toString();
    }

    /**
     * 设置无参构造器
     *
     * @param clazz
     */
    private void addDefaultConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        // 获取无参构造器
        Arrays.stream(constructors).filter(constructor -> constructor.getParameterTypes().length == 0).findAny()
                .ifPresent(constructor -> this.defaultConstructor = constructor);
    }

    /**
     * 判断是否有权限修改操作
     *
     * @return
     */
    public static boolean canControlMemberAccessible() {
        try {
            SecurityManager securityManager = System.getSecurityManager();
            if (null != securityManager) {
                securityManager.checkPermission(new RuntimePermission("suppressAccessChecks"));
            }
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }

    public boolean hasSetter(String name) {
        return setMethods.containsKey(name);
    }

    public Class<?> getGetterType(String name) {
        Class<?> aClass = getTypes.get(name);
        if (aClass == null) {
            throw new RuntimeException("There is no getter for property named '" + name + "' in '" + type + "'");
        }
        return aClass;
    }
}
