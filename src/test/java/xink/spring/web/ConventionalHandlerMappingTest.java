/*
 * Copyright 2012 yingxinwu.g@gmail.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xink.spring.web;

import org.junit.Before;
import org.junit.Test;
import xink.spring.web.controllers.TestAnnoController;
import xink.spring.web.controllers.TestPlainController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.springframework.util.ReflectionUtils.MethodCallback;
import static org.springframework.util.ReflectionUtils.doWithMethods;

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
        mapping.setMapAnnotationedMethod(true);

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

