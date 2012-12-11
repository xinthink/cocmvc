/*
 * Copyright 2012 yingxinwu.g@gmail.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.cocmvc;

import net.sf.cocmvc.controllers.NotAController;
import net.sf.cocmvc.controllers.TestAnnoController;
import net.sf.cocmvc.controllers.TestPlainController;
import net.sf.cocmvc.controllers.module1.ModuleOneController;
import net.sf.cocmvc.controllers.module1.module11.ModuleOneOneController;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.*;
import static org.springframework.util.ReflectionUtils.*;

public class ConventionalHandlerMappingTest {

    private ConventionalHandlerMapping mapping;

    @Before
    public void setUp() {
        mapping = new ConventionalHandlerMapping();
    }

    /**
     * Conventional url mapping
     */
    @Test
    public void testConventionalActions() {
        final Class handlerType = TestPlainController.class;
        doWithMethods(handlerType, new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                String methodName = method.getName();

                if ("index".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/testPlain");
                }
                else if ("list".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/testPlain/list");
                }
                else if ("doSomething".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/testPlain/doSomething");
                }
                else if ("init".equals(methodName)) {
                    assertNoMappings(handlerType, method);
                }
                else if ("destroy".equals(methodName)) {
                    assertNoMappings(handlerType, method);
                }
            }
        });
    }

    /**
     * Conventional url mapping, Snake-case style
     */
    @Test
    public void testConventionalActionsUseSnakeCase() {
        mapping.setUseSnakeCase(true);

        final Class handlerType = TestPlainController.class;
        doWithMethods(handlerType, new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                String methodName = method.getName();

                if ("index".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/test-plain");
                }
                else if ("list".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/test-plain/list");
                }
                else if ("doSomething".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/test-plain/do-something");
                }
                else if ("init".equals(methodName)) {
                    assertNoMappings(handlerType, method);
                }
                else if ("destroy".equals(methodName)) {
                    assertNoMappings(handlerType, method);
                }
            }
        });
    }

    /**
     * Configuration overrides convention
     */
    @Test
    public void testConfiguredActions() {
        mapping.setMapAnnotationedMethod(true);

        final Class handlerType = TestAnnoController.class;
        doWithMethods(handlerType, new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                String methodName = method.getName();

                if ("relativePath".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/test/ann//action1", "/test/annotation/action1");
                }
                else if ("absolutePath".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/test/ann//action2", "/test/annotation/action2");
                }
            }
        });

        // stop mapping annotationed methods
        mapping.setMapAnnotationedMethod(false);

        doWithMethods(handlerType, new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                String methodName = method.getName();

                if ("relativePath".equals(methodName)) {
                    assertNoMappings(handlerType, method);
                }
                else if ("absolutePath".equals(methodName)) {
                    assertNoMappings(handlerType, method);
                }
            }
        });
    }

    /**
     * Mixed convention and configuration
     */
    @Test
    public void testMixedConventionAndConfiguration() {
        final Class handlerType = TestAnnoController.class;
        doWithMethods(handlerType, new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                String methodName = method.getName();

                if ("conventionalAction".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/test/ann//conventionalAction", "/test/annotation/conventionalAction");
                }
            }
        });
    }

    /**
     * Configuration overrides convention
     *
     * type/method marked with @NoMapping should be ignored
     */
    @Test
    public void testIgnoreNoMapping() {
        assertFalse(mapping.isHandler(NotAController.class));

        Method method = findMethod(TestPlainController.class, "notAnAction");
        assertNoMappings(TestPlainController.class, method);
    }

    @Test
    public void testModularityMapping() {
        mapping.setBasePackage("net.sf.cocmvc.controllers");

        final Class handlerType = ModuleOneController.class;
        doWithMethods(handlerType, new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                String methodName = method.getName();

                if ("index".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/module1/moduleOne");
                } else if ("fooBar".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/module1/moduleOne/fooBar");
                }
            }
        });

        final Class subModuleHandlerType = ModuleOneOneController.class;
        doWithMethods(handlerType, new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                String methodName = method.getName();

                if ("index".equals(methodName)) {
                    assertMappingPatterns(subModuleHandlerType, method, "/module1/module11/moduleOneOne");
                }
                else if ("fooBar".equals(methodName)) {
                    assertMappingPatterns(subModuleHandlerType, method, "/module1/module11/moduleOneOne/fooBar");
                }
            }
        });

        final Class topLevelHandlerType = TestPlainController.class;
        doWithMethods(handlerType, new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                String methodName = method.getName();

                if ("index".equals(methodName)) {
                    assertMappingPatterns(topLevelHandlerType, method, "/testPlain");
                }
                else if ("list".equals(methodName)) {
                    assertMappingPatterns(topLevelHandlerType, method, "/testPlain/list");
                }
            }
        });
    }

    @Test
    public void testModularityMappingInSnakeCase() {
        mapping.setBasePackage("net.sf.cocmvc.controllers");
        mapping.setUseSnakeCase(true);

        final Class handlerType = ModuleOneController.class;
        doWithMethods(handlerType, new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                String methodName = method.getName();

                if ("index".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/module1/module-one");
                } else if ("fooBar".equals(methodName)) {
                    assertMappingPatterns(handlerType, method, "/module1/module-one/foo-bar");
                }
            }
        });

        final Class subModuleHandlerType = ModuleOneOneController.class;
        doWithMethods(handlerType, new MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                String methodName = method.getName();

                if ("index".equals(methodName)) {
                    assertMappingPatterns(subModuleHandlerType, method, "/module1/module11/module-one-one");
                }
                else if ("fooBar".equals(methodName)) {
                    assertMappingPatterns(subModuleHandlerType, method, "/module1/module11/module-one-one/foo-bar");
                }
            }
        });
    }

    private void assertMappingPatterns(Class handlerType, Method actionMethod, String... expectedPatterns) {
        Set<String> patterns = getSortedMappingPatterns(actionMethod, handlerType);
        Set<String> expected = asSortedSet(expectedPatterns);
        assertEquals(expected, patterns);
    }

    private void assertNoMappings(Class handlerType, Method actionMethod) {
        assertNull(mapping.getMappingForMethod(actionMethod, handlerType));
    }

    private Set<String> getSortedMappingPatterns(Method actionMethod, Class handlerType) {
        return new TreeSet<String>(mapping.getMappingForMethod(actionMethod, handlerType)
                .getPatternsCondition().getPatterns());
    }

    private static <T> Set<T> asSortedSet(T... arr) {
        return new TreeSet<T>(Arrays.asList(arr));
    }
}

