/*
 * +---------------------------------------------------------
 * | Author Jared.Yan<yanhuaiwen@163.com>
 * +---------------------------------------------------------
 * | Copyright (c) http://cmsen.com All rights reserved.
 * +---------------------------------------------------------
 */
package com.cmsen.mybatis.pagination;

import org.apache.ibatis.mapping.BoundSql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

public class ObjectUtil {
    @SuppressWarnings("unchecked")
    public static <T> T get(Object o) {
        return (T) o;
    }

    public static boolean equals(Object o, Class<?> cls) {
        boolean isEqualsClass = o.getClass().equals(cls);
        boolean isEqualsSuperclass = o.getClass().getSuperclass().equals(cls);
        boolean isEqualsInterfaces = Arrays.asList(o.getClass().getInterfaces()).contains(cls);
        return isEqualsClass || isEqualsSuperclass || isEqualsInterfaces;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getAdditionalParameters(BoundSql boundSql) throws NoSuchFieldException, IllegalAccessException {
        Field additionalParametersField = boundSql.getClass().getDeclaredField("additionalParameters");
        additionalParametersField.setAccessible(true);
        return (Map<String, Object>) additionalParametersField.get(boundSql);
    }

    public static Field getDeclaredField(String s, Class<?> clazz) {
        try {
            Field declaredField = clazz.getDeclaredField(s);
            int modifiers = declaredField.getModifiers();
            if (!declaredField.isAccessible() && (Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers))) {
                declaredField.setAccessible(true);
            }
            return declaredField;
        } catch (NoSuchFieldException e) {
            try {
                Field declaredField = clazz.getSuperclass().getDeclaredField(s);
                int modifiers = declaredField.getModifiers();
                if (!declaredField.isAccessible() && (Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers))) {
                    declaredField.setAccessible(true);
                }
                return declaredField;
            } catch (NoSuchFieldException e1) {
                return null;
            }
        }
    }

    public static Field[] getDeclaredFields(Object o) {
        Class<?> aClass = o.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        int length = declaredFields.length;
        Field[] fields = new Field[length];
        for (int i = 0; i < length; i++) {
            Field declaredField = declaredFields[i];
            int modifiers = declaredField.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                continue;
            }
            if (!declaredField.isAccessible() && (Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers))) {
                declaredField.setAccessible(true);
            }
            fields[i] = declaredField;
        }
        return fields;
    }
}
