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

package xink.spring.web;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import xink.spring.web.annotation.NoMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * Convention over configuration HandlerMapping<br/>
 *
 * Beans following naming convention, say "XxxController", will be treated as Controllers,
 * and each {@code public} method of a Controller will be mapping to an URL request
 *
 * <p>The default URL pattern is: {@code /<controller>/<action>}.<p/>
 *
 * @author ywu
 */
public class ConventionalHandlerMapping extends RequestMappingHandlerMapping {

    /**
     * Controller naming convention, default is 'XxxController'
     */
    private String controllerNameSuffix = "Controller";

    /**
     * Use snake-case URL, default is false (use camel-case)
     */
    private boolean useSnakeCase = false;

    /**
     * Whether to create mapping for annotationed methods, default is false to avoid producing duplicated mappings
     */
    private boolean mapAnnotationedMethod = false;

    /**
     * Controller naming convention, the suffix will be removed before producing mapping, default is 'Controller'
     */
    public void setControllerNameSuffix(String controllerNameSuffix) {
        this.controllerNameSuffix = controllerNameSuffix;
    }

    /**
     * Use snake-case URL, default is false (use camel-case)
     */
    public void setUseSnakeCase(boolean useSnakeCase) {
        this.useSnakeCase = useSnakeCase;
    }

    /**
     * Whether to create mapping for annotationed methods, default is false to avoid producing duplicated mappings
     */
    public void setMapAnnotationedMethod(boolean mapAnnotationedMethod) {
        this.mapAnnotationedMethod = mapAnnotationedMethod;
    }

    @Override
    protected boolean isHandler(Class<?> beanType) {
        return findAnnotation(beanType, NoMapping.class) == null &&
                (beanType.getSimpleName().endsWith(controllerNameSuffix) || super.isHandler(beanType));
    }

    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        if (!isAction(method)) return null;

        // create mapping info for method and type level, and combine them
        RequestMappingInfo actionMapping = createActionMapping(method);
        return actionMapping != null ? createControllerMapping(handlerType).combine(actionMapping) : null;
    }

    /*
     * determine whether the method should be treated as an Action
     */
    private boolean isAction(Method method) {
        String name = method.getName();
        return Modifier.isPublic(method.getModifiers()) && !(
                name.matches("^(equals|hashCode|toString|clone|notify.*|wait|getClass)")           // methods declared in Object
                        || name.matches("^(init|destroy)")                                         // common lifecycle methods
                        || name.matches("^([sg]et(MetaClass|Property)|.*\\$.*|invokeMethod)")      // methods of groovy object
                        || findAnnotation(method, NoMapping.class) != null
        );
    }

    /*
     * create method-level mapping (an Action)
     */
    private RequestMappingInfo createActionMapping(Method method) {
        RequestMapping mapping = findAnnotation(method, RequestMapping.class);
        if (mapping == null)
            return createConventionalActionMapping(method);
        else
            return mapAnnotationedMethod ? createActionMapping(mapping, method) : null;
    }

    private RequestMappingInfo createConventionalActionMapping(Method method) {
        return new RequestMappingInfo(
                createPatternRequestCondition(buildConventionalActions(method)),
                null, null, null, null, null, getCustomMethodCondition(method));
    }

    private String[] buildConventionalActions(Method method) {
        // method named 'index' will be mapped to '/'
        String action = "index".equals(method.getName()) ? "" : method.getName();
        return new String[]{ useSnakeCase ? toSnakeCase(action) : toCamelCase(action) };
    }

    private RequestMappingInfo createActionMapping(RequestMapping mapping, Method method) {
        return createRequestMapping(mapping, getCustomMethodCondition(method));
    }

    /*
     * create type-level mapping (a Controller)
     */
    private RequestMappingInfo createControllerMapping(Class<?> handlerType) {
        RequestMapping mapping = findAnnotation(handlerType, RequestMapping.class);
        return mapping == null ? createConventionalControllerMapping(handlerType) : createControllerMapping(mapping, handlerType);
    }

    private RequestMappingInfo createConventionalControllerMapping(Class handlerType) {
        return new RequestMappingInfo(
                createPatternRequestCondition(buildConventionalControllerPaths(handlerType)),
                null, null, null, null, null, getCustomTypeCondition(handlerType));
    }

    private String[] buildConventionalControllerPaths(Class handlerType) {
        String path = handlerType.getSimpleName();
        if (path.endsWith(controllerNameSuffix)) path = path.substring(0, path.lastIndexOf(controllerNameSuffix));
        return new String[]{'/' + (useSnakeCase ? toSnakeCase(path) : toCamelCase(path))};
    }

    private RequestMappingInfo createControllerMapping(RequestMapping mapping, Class<?> handlerType) {
        return createRequestMapping(mapping, getCustomTypeCondition(handlerType));
    }

    private PatternsRequestCondition createPatternRequestCondition(String[] patterns) {
        return new PatternsRequestCondition(patterns, this.getUrlPathHelper(), this.getPathMatcher(), useSuffixPatternMatch(), useTrailingSlashMatch());
    }

    private RequestMappingInfo createRequestMapping(RequestMapping annotation, RequestCondition<?> customCondition) {
        return new RequestMappingInfo(
                createPatternRequestCondition(annotation.value()),
                new RequestMethodsRequestCondition(annotation.method()),
                new ParamsRequestCondition(annotation.params()),
                new HeadersRequestCondition(annotation.headers()),
                new ConsumesRequestCondition(annotation.consumes(), annotation.headers()),
                new ProducesRequestCondition(annotation.produces(), annotation.headers()),
                customCondition);
    }

    private String toSnakeCase(String str) {
        return str.replaceAll("([A-Z])", "-$1").toLowerCase().replaceAll("^-", "");
    }

    private String toCamelCase(String str) {
        if (!StringUtils.hasText(str)) return str;
        return Character.toLowerCase(str.charAt(0)) + (str.length() > 1 ? str.substring(1) : "");
    }

}
