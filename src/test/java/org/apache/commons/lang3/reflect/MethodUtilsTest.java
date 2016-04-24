/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3.reflect;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils.Interfaces;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.reflect.testbed.Annotated;
import org.apache.commons.lang3.reflect.testbed.GenericConsumer;
import org.apache.commons.lang3.reflect.testbed.GenericParent;
import org.apache.commons.lang3.reflect.testbed.StringParameterizedChild;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests MethodUtils
 */
public class MethodUtilsTest {

    private static interface PrivateInterface {}

    static class TestBeanWithInterfaces implements PrivateInterface {
        public String foo() {
            return "foo()";
        }
    }

    public static class TestBean {

        public static String bar() {
            return "bar()";
        }

        public static String bar(final int i) {
            return "bar(int)";
        }

        public static String bar(final Integer i) {
            return "bar(Integer)";
        }

        public static String bar(final double d) {
            return "bar(double)";
        }

        public static String bar(final String s) {
            return "bar(String)";
        }

        public static String bar(final Object o) {
            return "bar(Object)";
        }

        public static String bar(final String... s) {
            return "bar(String...)";
        }

        public static String bar(final Integer i, final String... s) {
            return "bar(int, String...)";
        }

        public static void oneParameterStatic(final String s) {
            // empty
        }

        @SuppressWarnings("unused")
        private void privateStuff() {
        }


        public String foo() {
            return "foo()";
        }

        public String foo(final int i) {
            return "foo(int)";
        }

        public String foo(final Integer i) {
            return "foo(Integer)";
        }

        public String foo(final double d) {
            return "foo(double)";
        }

        public String foo(final String s) {
            return "foo(String)";
        }

        public String foo(final Object o) {
            return "foo(Object)";
        }

        public String foo(final String... s) {
            return "foo(String...)";
        }

        public String foo(final Integer i, final String... s) {
            return "foo(int, String...)";
        }

        public void oneParameter(final String s) {
            // empty
        }

        public String foo(final Object... s) {
            return "foo(Object...)";
        }

        public int[] unboxing(int... values) {
            return values;
        }

        // This method is overloaded for the wrapper class for every primitive type, plus the common supertypes
        // Number and Object. This is an acid test since it easily leads to ambiguous methods.
        public static String varOverload(Byte... args) { return "Byte..."; }
        public static String varOverload(Character... args) { return "Character..."; }
        public static String varOverload(Short... args) { return "Short..."; }
        public static String varOverload(Boolean... args) { return "Boolean..."; }
        public static String varOverload(Float... args) { return "Float..."; }
        public static String varOverload(Double... args) { return "Double..."; }
        public static String varOverload(Integer... args) { return "Integer..."; }
        public static String varOverload(Long... args) { return "Long..."; }
        public static String varOverload(Number... args) { return "Number..."; }
        public static String varOverload(Object... args) { return "Object..."; }
        public static String varOverload(String... args) { return "String..."; }

        // This method is overloaded for the wrapper class for every numeric primitive type, plus the common
        // supertype Number
        public static String numOverload(Byte... args) { return "Byte..."; }
        public static String numOverload(Short... args) { return "Short..."; }
        public static String numOverload(Float... args) { return "Float..."; }
        public static String numOverload(Double... args) { return "Double..."; }
        public static String numOverload(Integer... args) { return "Integer..."; }
        public static String numOverload(Long... args) { return "Long..."; }
        public static String numOverload(Number... args) { return "Number..."; }

        // These varOverloadEcho and varOverloadEchoStatic methods are designed to verify that
        // not only is the correct overloaded variant invoked, but that the varags arguments
        // are also delivered correctly to the method.
        public ImmutablePair<String, Object[]> varOverloadEcho(String... args) {
          return new ImmutablePair<String, Object[]>("String...", args);
        }
        public ImmutablePair<String, Object[]> varOverloadEcho(Number... args) {
          return new ImmutablePair<String, Object[]>("Number...", args);
        }

        public static ImmutablePair<String, Object[]> varOverloadEchoStatic(String... args) {
          return new ImmutablePair<String, Object[]>("String...", args);
        }
        public static ImmutablePair<String, Object[]> varOverloadEchoStatic(Number... args) {
          return new ImmutablePair<String, Object[]>("Number...", args);
        }

        static void verify(ImmutablePair<String, Object[]> a, ImmutablePair<String, Object[]> b) {
          assertEquals(a.getLeft(), b.getLeft());
          assertArrayEquals(a.getRight(), b.getRight());
        }

        static void verify(ImmutablePair<String, Object[]> a, Object _b) {
          final ImmutablePair<String, Object[]> b = (ImmutablePair<String, Object[]>) _b;
          verify(a, b);
        }

    }

    private static class TestMutable implements Mutable<Object> {
        @Override
        public Object getValue() {
            return null;
        }

        @Override
        public void setValue(final Object value) {
        }
    }

    private TestBean testBean;
    private final Map<Class<?>, Class<?>[]> classCache = new HashMap<Class<?>, Class<?>[]>();

    @Before
    public void setUp() throws Exception {
        testBean = new TestBean();
        classCache.clear();
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(MethodUtils.class.newInstance());
    }

    @Test
    public void verifyJavaVarargsOverloadingResolution() throws Exception {
        // This code is not a test of MethodUtils.
        // Rather it makes explicit the behavior of the Java specification for
        // various cases of overload resolution.
        assertEquals("Byte...", TestBean.varOverload((byte) 1, (byte) 2));
        assertEquals("Short...", TestBean.varOverload((short) 1, (short) 2));
        assertEquals("Integer...", TestBean.varOverload(1, 2));
        assertEquals("Long...", TestBean.varOverload(1L, 2L));
        assertEquals("Float...", TestBean.varOverload(1f, 2f));
        assertEquals("Double...", TestBean.varOverload(1d, 2d));
        assertEquals("Character...", TestBean.varOverload('a', 'b'));
        assertEquals("String...", TestBean.varOverload("a", "b"));
        assertEquals("Boolean...", TestBean.varOverload(true, false));

        assertEquals("Object...", TestBean.varOverload(1, "s"));
        assertEquals("Object...", TestBean.varOverload(1, true));
        assertEquals("Object...", TestBean.varOverload(1.1, true));
        assertEquals("Object...", TestBean.varOverload('c', true));
        assertEquals("Number...", TestBean.varOverload(1, 1.1));
        assertEquals("Number...", TestBean.varOverload(1, 1L));
        assertEquals("Number...", TestBean.varOverload(1d, 1f));
        assertEquals("Number...", TestBean.varOverload((short) 1, (byte) 1));
        assertEquals("Object...", TestBean.varOverload(1, 'c'));
        assertEquals("Object...", TestBean.varOverload('c', "s"));
    }

    @Test
    public void testInvokeJavaVarargsOverloadingResolution() throws Exception {
        assertEquals("Byte...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", (byte) 1, (byte) 2));
        assertEquals("Short...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", (short) 1, (short) 2));
        assertEquals("Integer...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 1, 2));
        assertEquals("Long...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 1L, 2L));
        assertEquals("Float...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 1f, 2f));
        assertEquals("Double...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 1d, 2d));
        assertEquals("Character...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 'a', 'b'));
        assertEquals("String...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", "a", "b"));
        assertEquals("Boolean...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", true, false));

        assertEquals("Object...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 1, "s"));
        assertEquals("Object...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 1, true));
        assertEquals("Object...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 1.1, true));
        assertEquals("Object...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 'c', true));
        assertEquals("Number...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 1, 1.1));
        assertEquals("Number...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 1, 1L));
        assertEquals("Number...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 1d, 1f));
        assertEquals("Number...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", (short) 1, (byte) 1));
        assertEquals("Object...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 1, 'c'));
        assertEquals("Object...", MethodUtils.invokeStaticMethod(TestBean.class,
                "varOverload", 'c', "s"));

        assertEquals("Object...", MethodUtils.invokeStaticMethod(TestBean.class, "varOverload",
                (Object[]) ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("Number...", MethodUtils.invokeStaticMethod(TestBean.class, "numOverload",
                (Object[]) ArrayUtils.EMPTY_CLASS_ARRAY));
    }

    @Test
    public void testInvokeMethod() throws Exception {
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo",
                (Object[]) ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo"));
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo",
                (Object[]) null));
        assertEquals("foo()", MethodUtils.invokeMethod(testBean, "foo",
                (Object[]) null, (Class<?>[]) null));
        assertEquals("foo(String)", MethodUtils.invokeMethod(testBean, "foo",
                ""));
        assertEquals("foo(Object)", MethodUtils.invokeMethod(testBean, "foo",
                new Object()));
        assertEquals("foo(Object)", MethodUtils.invokeMethod(testBean, "foo",
                Boolean.TRUE));
        assertEquals("foo(Integer)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.INTEGER_ONE));
        assertEquals("foo(int)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.BYTE_ONE));
        assertEquals("foo(double)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.LONG_ONE));
        assertEquals("foo(double)", MethodUtils.invokeMethod(testBean, "foo",
                NumberUtils.DOUBLE_ONE));
        assertEquals("foo(String...)", MethodUtils.invokeMethod(testBean, "foo",
                "a", "b", "c"));
        assertEquals("foo(String...)", MethodUtils.invokeMethod(testBean, "foo",
                "a", "b", "c"));
        assertEquals("foo(int, String...)", MethodUtils.invokeMethod(testBean, "foo",
                5, "a", "b", "c"));

        TestBean.verify(new ImmutablePair("String...", new String[]{"x", "y"}),
                        MethodUtils.invokeMethod(testBean, "varOverloadEcho", "x", "y"));
        TestBean.verify(new ImmutablePair("Number...", new Number[]{17, 23, 42}),
                        MethodUtils.invokeMethod(testBean, "varOverloadEcho", 17, 23, 42));
        TestBean.verify(new ImmutablePair("String...", new String[]{"x", "y"}),
                        MethodUtils.invokeMethod(testBean, "varOverloadEcho", new String[]{"x", "y"}));
        TestBean.verify(new ImmutablePair("Number...", new Number[]{17, 23, 42}),
                        MethodUtils.invokeMethod(testBean, "varOverloadEcho", new Number[]{17, 23, 42}));
    }

    @Test
    public void testInvokeExactMethod() throws Exception {
        assertEquals("foo()", MethodUtils.invokeExactMethod(testBean, "foo",
                (Object[]) ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("foo()", MethodUtils.invokeExactMethod(testBean, "foo"));
        assertEquals("foo()", MethodUtils.invokeExactMethod(testBean, "foo",
                (Object[]) null));
        assertEquals("foo()", MethodUtils.invokeExactMethod(testBean, "foo",
                (Object[]) null, (Class<?>[]) null));
        assertEquals("foo(String)", MethodUtils.invokeExactMethod(testBean,
                "foo", ""));
        assertEquals("foo(Object)", MethodUtils.invokeExactMethod(testBean,
                "foo", new Object()));
        assertEquals("foo(Integer)", MethodUtils.invokeExactMethod(testBean,
                "foo", NumberUtils.INTEGER_ONE));
        assertEquals("foo(double)", MethodUtils.invokeExactMethod(testBean,
                "foo", new Object[] { NumberUtils.DOUBLE_ONE },
                new Class[] { Double.TYPE }));

        try {
            MethodUtils
                    .invokeExactMethod(testBean, "foo", NumberUtils.BYTE_ONE);
            fail("should throw NoSuchMethodException");
        } catch (final NoSuchMethodException e) {
        }
        try {
            MethodUtils
                    .invokeExactMethod(testBean, "foo", NumberUtils.LONG_ONE);
            fail("should throw NoSuchMethodException");
        } catch (final NoSuchMethodException e) {
        }
        try {
            MethodUtils.invokeExactMethod(testBean, "foo", Boolean.TRUE);
            fail("should throw NoSuchMethodException");
        } catch (final NoSuchMethodException e) {
        }
    }

    @Test
    public void testInvokeStaticMethod() throws Exception {
        assertEquals("bar()", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", (Object[]) ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("bar()", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", (Object[]) null));
        assertEquals("bar()", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", (Object[]) null, (Class<?>[]) null));
        assertEquals("bar(String)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", ""));
        assertEquals("bar(Object)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", new Object()));
        assertEquals("bar(Object)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", Boolean.TRUE));
        assertEquals("bar(Integer)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", NumberUtils.INTEGER_ONE));
        assertEquals("bar(int)", MethodUtils.invokeStaticMethod(TestBean.class,
                "bar", NumberUtils.BYTE_ONE));
        assertEquals("bar(double)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", NumberUtils.LONG_ONE));
        assertEquals("bar(double)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", NumberUtils.DOUBLE_ONE));
        assertEquals("bar(String...)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", "a", "b"));
        assertEquals("bar(int, String...)", MethodUtils.invokeStaticMethod(
                TestBean.class, "bar", NumberUtils.INTEGER_ONE, "a", "b"));

        TestBean.verify(new ImmutablePair("String...", new String[]{"x", "y"}),
                        MethodUtils.invokeStaticMethod(TestBean.class, "varOverloadEchoStatic", "x", "y"));
        TestBean.verify(new ImmutablePair("Number...", new Number[]{17, 23, 42}),
                        MethodUtils.invokeStaticMethod(TestBean.class, "varOverloadEchoStatic", 17, 23, 42));
        TestBean.verify(new ImmutablePair("String...", new String[]{"x", "y"}),
                        MethodUtils.invokeStaticMethod(TestBean.class, "varOverloadEchoStatic", new String[]{"x", "y"}));
        TestBean.verify(new ImmutablePair("Number...", new Number[]{17, 23, 42}),
                        MethodUtils.invokeStaticMethod(TestBean.class, "varOverloadEchoStatic", new Number[]{17, 23, 42}));

        try {
            MethodUtils.invokeStaticMethod(TestBean.class, "does_not_exist");
            fail("should throw NoSuchMethodException");
        } catch (final NoSuchMethodException e) {
        }
    }

    @Test
    public void testInvokeExactStaticMethod() throws Exception {
        assertEquals("bar()", MethodUtils.invokeExactStaticMethod(TestBean.class,
                "bar", (Object[]) ArrayUtils.EMPTY_CLASS_ARRAY));
        assertEquals("bar()", MethodUtils.invokeExactStaticMethod(TestBean.class,
                "bar", (Object[]) null));
        assertEquals("bar()", MethodUtils.invokeExactStaticMethod(TestBean.class,
                "bar", (Object[]) null, (Class<?>[]) null));
        assertEquals("bar(String)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", ""));
        assertEquals("bar(Object)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", new Object()));
        assertEquals("bar(Integer)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", NumberUtils.INTEGER_ONE));
        assertEquals("bar(double)", MethodUtils.invokeExactStaticMethod(
                TestBean.class, "bar", new Object[] { NumberUtils.DOUBLE_ONE },
                new Class[] { Double.TYPE }));

        try {
            MethodUtils.invokeExactStaticMethod(TestBean.class, "bar",
                    NumberUtils.BYTE_ONE);
            fail("should throw NoSuchMethodException");
        } catch (final NoSuchMethodException e) {
        }
        try {
            MethodUtils.invokeExactStaticMethod(TestBean.class, "bar",
                    NumberUtils.LONG_ONE);
            fail("should throw NoSuchMethodException");
        } catch (final NoSuchMethodException e) {
        }
        try {
            MethodUtils.invokeExactStaticMethod(TestBean.class, "bar",
                    Boolean.TRUE);
            fail("should throw NoSuchMethodException");
        } catch (final NoSuchMethodException e) {
        }
    }

    @Test
    public void testGetAccessibleInterfaceMethod() throws Exception {
        final Class<?>[][] p = { ArrayUtils.EMPTY_CLASS_ARRAY, null };
        for (final Class<?>[] element : p) {
            final Method method = TestMutable.class.getMethod("getValue", element);
            final Method accessibleMethod = MethodUtils.getAccessibleMethod(method);
            assertNotSame(accessibleMethod, method);
            assertSame(Mutable.class, accessibleMethod.getDeclaringClass());
        }
    }

    @Test
    public void testGetAccessibleMethodPrivateInterface() throws Exception {
        final Method expected = TestBeanWithInterfaces.class.getMethod("foo");
        assertNotNull(expected);
        final Method actual = MethodUtils.getAccessibleMethod(TestBeanWithInterfaces.class, "foo");
        assertNull(actual);
    }

    @Test
    public void testGetAccessibleInterfaceMethodFromDescription()
            throws Exception {
        final Class<?>[][] p = { ArrayUtils.EMPTY_CLASS_ARRAY, null };
        for (final Class<?>[] element : p) {
            final Method accessibleMethod = MethodUtils.getAccessibleMethod(
                    TestMutable.class, "getValue", element);
            assertSame(Mutable.class, accessibleMethod.getDeclaringClass());
        }
    }

    @Test
    public void testGetAccessiblePublicMethod() throws Exception {
        assertSame(MutableObject.class, MethodUtils.getAccessibleMethod(
                MutableObject.class.getMethod("getValue",
                        ArrayUtils.EMPTY_CLASS_ARRAY)).getDeclaringClass());
    }

    @Test
    public void testGetAccessiblePublicMethodFromDescription() throws Exception {
        assertSame(MutableObject.class, MethodUtils.getAccessibleMethod(
                MutableObject.class, "getValue", ArrayUtils.EMPTY_CLASS_ARRAY)
                .getDeclaringClass());
    }

    @Test
   public void testGetAccessibleMethodInaccessible() throws Exception {
        final Method expected = TestBean.class.getDeclaredMethod("privateStuff");
        final Method actual = MethodUtils.getAccessibleMethod(expected);
        assertNull(actual);
    }

    @Test
   public void testGetMatchingAccessibleMethod() throws Exception {
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                ArrayUtils.EMPTY_CLASS_ARRAY, ArrayUtils.EMPTY_CLASS_ARRAY);
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                null, ArrayUtils.EMPTY_CLASS_ARRAY);
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(String.class), singletonArray(String.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Object.class), singletonArray(Object.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Boolean.class), singletonArray(Object.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Byte.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Byte.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Short.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Short.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Character.class), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Character.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Integer.class), singletonArray(Integer.class));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Integer.TYPE), singletonArray(Integer.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Long.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Long.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Float.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Float.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Double.class), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Double.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                singletonArray(Double.TYPE), singletonArray(Double.TYPE));
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                new Class[] {String.class, String.class}, new Class[] {String[].class});
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "foo",
                new Class[] {Integer.TYPE, String.class, String.class}, new Class[] {Integer.class, String[].class});
        expectMatchingAccessibleMethodParameterTypes(InheritanceBean.class, "testOne",
                singletonArray(ParentObject.class), singletonArray(ParentObject.class));
        expectMatchingAccessibleMethodParameterTypes(InheritanceBean.class, "testOne",
                singletonArray(ChildObject.class), singletonArray(ParentObject.class));
        expectMatchingAccessibleMethodParameterTypes(InheritanceBean.class, "testTwo",
                singletonArray(ParentObject.class), singletonArray(GrandParentObject.class));
        expectMatchingAccessibleMethodParameterTypes(InheritanceBean.class, "testTwo",
                singletonArray(ChildObject.class), singletonArray(ChildInterface.class));
    }

    @Test
    public void testNullArgument() {
        expectMatchingAccessibleMethodParameterTypes(TestBean.class, "oneParameter",
                singletonArray(null), singletonArray(String.class));
    }

    @Test
    public void testGetOverrideHierarchyIncludingInterfaces() {
        final Method method = MethodUtils.getAccessibleMethod(StringParameterizedChild.class, "consume", String.class);
        final Iterator<MethodDescriptor> expected =
            Arrays.asList(new MethodDescriptor(StringParameterizedChild.class, "consume", String.class),
                new MethodDescriptor(GenericParent.class, "consume", GenericParent.class.getTypeParameters()[0]),
                new MethodDescriptor(GenericConsumer.class, "consume", GenericConsumer.class.getTypeParameters()[0]))
                .iterator();
        for (final Method m : MethodUtils.getOverrideHierarchy(method, Interfaces.INCLUDE)) {
            assertTrue(expected.hasNext());
            final MethodDescriptor md = expected.next();
            assertEquals(md.declaringClass, m.getDeclaringClass());
            assertEquals(md.name, m.getName());
            assertEquals(md.parameterTypes.length, m.getParameterTypes().length);
            for (int i = 0; i < md.parameterTypes.length; i++) {
                assertTrue(TypeUtils.equals(md.parameterTypes[i], m.getGenericParameterTypes()[i]));
            }
        }
        assertFalse(expected.hasNext());
    }

    @Test
    public void testGetOverrideHierarchyExcludingInterfaces() {
        final Method method = MethodUtils.getAccessibleMethod(StringParameterizedChild.class, "consume", String.class);
        final Iterator<MethodDescriptor> expected =
            Arrays.asList(new MethodDescriptor(StringParameterizedChild.class, "consume", String.class),
                new MethodDescriptor(GenericParent.class, "consume", GenericParent.class.getTypeParameters()[0]))
                .iterator();
        for (final Method m : MethodUtils.getOverrideHierarchy(method, Interfaces.EXCLUDE)) {
            assertTrue(expected.hasNext());
            final MethodDescriptor md = expected.next();
            assertEquals(md.declaringClass, m.getDeclaringClass());
            assertEquals(md.name, m.getName());
            assertEquals(md.parameterTypes.length, m.getParameterTypes().length);
            for (int i = 0; i < md.parameterTypes.length; i++) {
                assertTrue(TypeUtils.equals(md.parameterTypes[i], m.getGenericParameterTypes()[i]));
            }
        }
        assertFalse(expected.hasNext());
    }

    @Test
    @Annotated
    public void testGetMethodsWithAnnotation() throws NoSuchMethodException {
        assertArrayEquals(new Method[0], MethodUtils.getMethodsWithAnnotation(Object.class, Annotated.class));

        Method[] methodsWithAnnotation = MethodUtils.getMethodsWithAnnotation(MethodUtilsTest.class, Annotated.class);
        assertEquals(2, methodsWithAnnotation.length);
        assertThat(methodsWithAnnotation, hasItemInArray(MethodUtilsTest.class.getMethod("testGetMethodsWithAnnotation")));
        assertThat(methodsWithAnnotation, hasItemInArray(MethodUtilsTest.class.getMethod("testGetMethodsListWithAnnotation")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMethodsWithAnnotationIllegalArgumentException1() {
        MethodUtils.getMethodsWithAnnotation(FieldUtilsTest.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMethodsWithAnnotationIllegalArgumentException2() {
        MethodUtils.getMethodsWithAnnotation(null, Annotated.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMethodsWithAnnotationIllegalArgumentException3() {
        MethodUtils.getMethodsWithAnnotation(null, null);
    }

    @Test
    @Annotated
    public void testGetMethodsListWithAnnotation() throws NoSuchMethodException {
        assertEquals(0, MethodUtils.getMethodsListWithAnnotation(Object.class, Annotated.class).size());

        final List<Method> methodWithAnnotation = MethodUtils.getMethodsListWithAnnotation(MethodUtilsTest.class, Annotated.class);
        assertEquals(2, methodWithAnnotation.size());
        assertThat(methodWithAnnotation, hasItems(
                MethodUtilsTest.class.getMethod("testGetMethodsWithAnnotation"),
                MethodUtilsTest.class.getMethod("testGetMethodsListWithAnnotation")
        ));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMethodsListWithAnnotationIllegalArgumentException1() {
        MethodUtils.getMethodsListWithAnnotation(FieldUtilsTest.class, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMethodsListWithAnnotationIllegalArgumentException2() {
        MethodUtils.getMethodsListWithAnnotation(null, Annotated.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMethodsListWithAnnotationIllegalArgumentException3() {
        MethodUtils.getMethodsListWithAnnotation(null, null);
    }

    private void expectMatchingAccessibleMethodParameterTypes(final Class<?> cls,
            final String methodName, final Class<?>[] requestTypes, final Class<?>[] actualTypes) {
        final Method m = MethodUtils.getMatchingAccessibleMethod(cls, methodName,
                requestTypes);
        assertNotNull("could not find any matches for " + methodName
                + " (" + (requestTypes == null ? null : toString(requestTypes)) + ")", m);
        assertTrue(toString(m.getParameterTypes()) + " not equals "
                + toString(actualTypes), Arrays.equals(actualTypes, m
                .getParameterTypes()));
    }

    private String toString(final Class<?>[] c) {
        return Arrays.asList(c).toString();
    }

    private Class<?>[] singletonArray(final Class<?> c) {
        Class<?>[] result = classCache.get(c);
        if (result == null) {
            result = new Class[] { c };
            classCache.put(c, result);
        }
        return result;
    }

    public static class InheritanceBean {
        public void testOne(final Object obj) {}
        public void testOne(final GrandParentObject obj) {}
        public void testOne(final ParentObject obj) {}
        public void testTwo(final Object obj) {}
        public void testTwo(final GrandParentObject obj) {}
        public void testTwo(final ChildInterface obj) {}
    }

    interface ChildInterface {}
    public static class GrandParentObject {}
    public static class ParentObject extends GrandParentObject {}
    public static class ChildObject extends ParentObject implements ChildInterface {}

    private static class MethodDescriptor {
        final Class<?> declaringClass;
        final String name;
        final Type[] parameterTypes;

        MethodDescriptor(final Class<?> declaringClass, final String name, final Type... parameterTypes) {
            this.declaringClass = declaringClass;
            this.name = name;
            this.parameterTypes = parameterTypes;
        }
    }

    @Test
    public void testVarArgsUnboxing() throws Exception {
        TestBean testBean = new TestBean();
        int[] actual = (int[])MethodUtils.invokeMethod(testBean, "unboxing", Integer.valueOf(1), Integer.valueOf(2));
        Assert.assertArrayEquals(new int[]{1, 2}, actual);
    }
}
