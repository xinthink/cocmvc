# Convention Over Configuration extension to [Spring Web MVC](http://static.springsource.org/spring/docs/current/spring-framework-reference/html/mvc.html)

## Why we need this extension
Often, we develope web applications with the powerful Spring Web MVC in these two ways (or both of them):

1. [Convention over configuration support](http://static.springsource.org/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-coc)
  * help omitting many 'redundant' configurations
  * but we must have our controllers implementing a framework specific interface, reducing the flexibility
2. [Annotation-driven](http://static.springsource.org/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-controller)
  * very powerful request mappings, and flexible handler method declarations
  * but it's actual configurations, sometimes seems to be 'redundant'

Both of them are pretty good, but there's no way to put them together in a single controller, you have to make the choice, and tolerate the redundancy.

But now, with the cocmvc extension, one is allowed to write a controller like this:

```java
// maps to /hello
public class HelloController {
  
  // handling /hello
  public void index(Model modle) {
    modle.addAttribute("this's the home page of hello");
    // view-id = 'hello'
  }
  
  // handling /hello/delete?id=1
  public void delete(@RequestParam("id") int id) {
    ...
    // view-id = 'hello/delete
  }
  
  // handling /hello/show/1
  @RequestMapping("show/{id}")
  public void show(@PathVariable("id") int id) {
    ...
    // view-id = 'hello/show'
  }
}
```
Well, it combines the advantages of Spring's [COC support](http://static.springsource.org/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-coc) and [annotation-driven](http://static.springsource.org/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-controller) together, and I think it's also concise, and DRY.

Let's see how to do that.

## Developing by convention
### URL pattern
By convention, the URL pattern is:

    <context-root>/<module>/<controller>/<action>
* *module*
 - modularity, it's a good idea to package controllers into modules in a big application
 - can recursively contain other sub-modules, such as: `news/sports/football`
 - must provide a `basePackage` to enable modularity, the relative path (to the `basePackage`) of a controller will be used as it's module; otherwise, the module part will always be empty ('')
* *controller*
 - just like Spring or Grails, controller class named `AbcController` will be mapped to `/abc`
 - supports camel-case style (by default) or snake-case style, i.e. `helloWorld`, or `hello-world`
* *action*
 - mapped to action methods (request handlers), each public method of the controller will be mapped as an `action`
 - method named `index` is a special case, it will be treated as the home page of the controller (the `/` action)
 - also by convention, `init` and `destroy` are life-cycle methods of a Spring bean, the framework will NOT treat them as actions
 - you can choose camel-case style (by default) or snake-case style

### Controllers
#### Coding a controller
* Naming
 - Name your controller class with a 'Controller' suffix, for example: `LoginController`, `UserRegisterController`
 - A controller class may extends any base class or implements any interface
* Packaging
 - You can put your controllers under any packages as you need
 - By setting a `basePackage`, controllers will be grouped into modules, for example, the module name of `basePackage.abc.def.HelloController` is 'abc/def', and it will be responsible to the URL `/abc/def/hello`
* Write action methods
 - Public methods of a controller are all action methods, including methods inherited from base classes, excluding methods declared in `Object`, such as `toString`, `hashCode` ...
 - You can define an action method in any form supported by [Spring Web MVC](http://static.springsource.org/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-ann-methods)

#### Make controller a Spring bean
XML configuration, component scanning ... 

Feel free to choose the way most fit to your project, but the component scanning way seems to be more DRY.

### View templates
By convention, the view-id of each action is corresponding to the requesting URL, i.e. view-id for action `/hello/show` is 'hello/show'

So, you can ommit the view-id return value (or when instantiating the `ModelAndView`), if you comply with the conventional view templates layout

For example (assume you're using jsp):

    views                                   Root of your view templates, may be /WEB-INF/views
      |
      |-- <controller>                      Views folder for top-level controllers
      |     |-- <action>.jsp                View templates
      |     \-- ...                 
      |
      |-- <module>                          Views folder for module
      |     |-- <sub-module>                Sub-module, recursively
      |     |     |-- <controller>
      |     |     |     |-- <action>.jsp    
      |     |     |     \-- ...
      |     |     \-- ...
      |     |
      |     |-- <controller>                Views folder for controller
      |     |     |-- <action>.jsp          View templates
      |     |     \-- ...             
      |     \-- ...                  
      |
      |-- index.jsp                         Top-level view templates, home page, error page etc.
      \-- ...

## Overrides the convention
You can override the convention by using [annotations (configurations)](http://static.springsource.org/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-controller).

For example, I'd like the home page located at `/`, instead of `/home`

```java
@RequestMapping("/")
public class HomeController {

  // handling /
  public String index() {
    ...
    retrun "index"; // view-id = 'index'
  }
}
```

or, to use path variables

```java
public class PersonController {

  // handling /person/show
  @RequestMapping("show/{id}")
  public void show(@PathVariable("id") int id) {
    ...
    // view-id = 'person/show'
  }
}
```

and sometimes, some special public methods should not be treated as actions

```java
public class PersonController {

  @NoMapping
  @ModelAttribute
  public Person addModelAttribute() {
    ...
    return new Person();
  }
}
```

## Project dependencies
Before enjoy developing by convention, you should first add cocmvc to your project dependencies.

Add the following snippet in your gradle build script, or something equivalent in other dependencies management tools:

    repositories {
        mavenCentral()
    }

    dependencies {
        compile 'net.sf.cocmvc:spring-cocmvc-ext:1.0'
    }
*please refer to the [sample build file](https://github.com/xinthink/cocmvc/blob/master/sample/build.gradle)

## Configurations
Huh, there's also a little configuration job to be done, in order to override the default behavior of Spring framework.

```xml
<!-- close spring's annotation-driven to avoid duplicated URL mapping -->
<!--<mvc:annotation-driven />-->

<!-- add this line to make our action methods to be adapted as request Handlers -->
<bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter" />

<!-- Enable conventional developing mode,
  and provides a basePacakge to enabled the modularity feature
-->
<bean class="net.sf.cocmvc.ConventionalHandlerMapping" p:basePackage="com.abc.controller" />
```

Enjoy!

#### Please refer to the [sample project](https://github.com/xinthink/cocmvc/tree/master/sample) for more details.
