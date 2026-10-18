package com.illusivesoulworks.polymorph.common.integration.util;

import java.lang.reflect.Field;

public class CompatReflection {

  public static Class<?> findClass(String name) {

    try {
      return Class.forName(name, false, CompatReflection.class.getClassLoader());
    } catch (ClassNotFoundException | LinkageError e) {
      return null;
    }
  }

  public static Field publicField(Class<?> owner, String name) {

    try {
      return owner == null ? null : owner.getField(name);
    } catch (NoSuchFieldException e) {
      return null;
    }
  }

  public static Object get(Field field, Object instance) {

    try {
      return field == null ? null : field.get(instance);
    } catch (IllegalAccessException | IllegalArgumentException e) {
      return null;
    }
  }
}
