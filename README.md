# Spring Boot Web Reactive Starter

This experimental project provides a simple way to try the new
[Web Reactive support in Spring Framework 5.0](http://docs.spring.io/spring-framework/docs/5.0.0.M1/spring-framework-reference/html/web-reactive.html).

## Quickstart

Go to [start.spring.io](https://start.spring.io), set the Spring Boot version to 1.4+ and add the "Reactive Web" starter.

From there you can take a look at the `spring-boot-sample-web-reactive` sample application inside this repository to see
a few examples of Controllers, `WebClient` usage, etc.

## Web Server Runtime

By default, this Boot Starter brings Embedded Tomcat as the default Web runtime.

Choosing a different one is really easy, you just need to exclude the Tomcat
starter from the reactive starter dependency and add the one you want - here,
[Reactor Netty](https://github.com/reactor/reactor-ipc):

```xml
<dependency>
	<groupId>org.springframework.boot.experimental</groupId>
	<artifactId>spring-boot-starter-web-reactive</artifactId>
	<exclusions>
		<exclusion>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<dependency>
	<groupId>io.projectreactor.ipc</groupId>
	<artifactId>reactor-netty</artifactId>
</dependency>
```

Doing the same with Gradle, for [RxNetty](https://github.com/ReactiveX/RxNetty):

```groovy
compile('org.springframework.boot.experimental:spring-boot-starter-web-reactive') {
	exclude group: 'org.springframework.boot', module: 'spring-boot-starter-tomcat'
}
compile "io.reactivex:rxnetty-common"
compile "io.reactivex:rxnetty-http"
```

Here are all the supported runtime (check out the sample project build to see the dependencies):

* Reactor Netty
* Tomcat
* Jetty
* RxNetty
* Undertow

## Web Client Runtime

Spring Web Reactive also provides a new reactive `WebClient`.
For now, only the Reactor Netty implementation is provided, but other variants should
be introduced in Spring Framework (such as RxNetty and Jetty).

## Resources

The Spring team has published a few blog posts on the subject:

* [Reactive Spring](https://spring.io/blog/2016/02/09/reactive-spring)
* [Understanding Reactive types](https://spring.io/blog/2016/04/19/understanding-reactive-types)
* Notes on Reactive Programming [part I](https://spring.io/blog/2016/06/07/notes-on-reactive-programming-part-i-the-reactive-landscape),
[part II](https://spring.io/blog/2016/06/13/notes-on-reactive-programming-part-ii-writing-some-code) and [part III](https://spring.io/blog/2016/07/20/notes-on-reactive-programming-part-iii-a-simple-http-server-application)

## Known issues

This starter doesn't work with plain `@SpringBootTest`s (i.e. with a `WebEnvironment.MOCK`, which is the default):

```
Caused by: java.lang.IllegalArgumentException: Unable to call initializer.
Object of class [org.springframework.boot.context.embedded.ReactiveWebApplicationContext]
must be an instance of interface org.springframework.web.context.ConfigurableWebApplicationContext
```

Web integration tests `@SpringBootTest`s are working properly,
for example `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)`.

## FAQ

**I've found an issue, I have a question - where should I report it?**

* You can report issues in this Github project or on https://jira.spring.io if this is a Spring Framework issue
* You can ask questions in Github issues as well or join the [Gitter chat](https://gitter.im/spring-projects/spring-boot)

**Will this be available in Spring Boot? When?**

Yes! Check out the [Spring Boot milestones](https://github.com/spring-projects/spring-boot/milestones) and especially
[the dedicated issue](https://github.com/spring-projects/spring-boot/issues/4908).

**Is Spring Boot required to run Spring Web Reactive?**

It certainly makes things easier, but it's not mandatory.
For manual bootstrapping, please [read the reference documentation]
(http://docs.spring.io/spring-framework/docs/5.0.0.BUILD-SNAPSHOT/spring-framework-reference/html/web-reactive.html#web-reactive-getting-started-manual).