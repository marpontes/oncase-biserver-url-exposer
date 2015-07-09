# What is it?

This project delivers a bean that helps you to easily expose some Pentaho BI Server urls to non-authenticated users.

Actually, exposing some URLs to anonymous requests, is not such a hard task to do.

Our goal here is to make it easier to do it from within your plugin without having to change any system XML file.

# How it works?

Into `applicationContext-spring-security.xml`, you have defined a `org.springframework.security.util.FilterChainProxy`.

There you'll find general URL patterns and a chain of filters and interceptors for each of those URLs.

If we only wanted to expose a single - or some - url by changing this file, we would only need to change the filterInvocationInterceptor.objectDefinitionSource which is contained into the same XML. We can also change this bean programmatically, however, since it's not possible to get the current `objectDefinitionSource` at runtime, it's therefore not possible to inject rules there. Also, it's too dangerous to get this `objectDefinitionSource` from the xml because someone trying to inject rules the same way we do would overwrite our rules.

So the way we're doing this permission is by copying the general rule [/**] for the FilterChainProxy at runtime and strategically placing it with the same chain, but our rule and our own FilterSecurityInterceptor.

This way, no matter how many plugins are trying to inject their rules, all of them can coexist.

# Building your jar

This repo is an Eclipse project. 

TODO: list dependencies and create resolve task.

# Using it in your plugin

First off, add the libs to your plugin the way you prefer.

To expose an api that your plugin delivers, for example, you simple need to define into your plugin.spring.xml, for example:

```xml
    <bean class="oncas.biserver.security.helper.URLExposer">
      <property name="filterChainUrlPattern" value="/content/tapa/resources/templates/*/assets/**" />
      <property name="interceptorDefifnintion">
        <value>
          <![CDATA[
CONVERT_URL_TO_LOWERCASE_BEFORE_COMPARISON
\A/content/tapa/resources/templates/([\w\-\_]+)/assets/.*\Z=Anonymous,Authenticated
\A/.*\Z=Authenticated]]>
        </value>
      </property>
    </bean>
```

In this case, we're exposing all the content from `/content/tapa/resources/templates/*/assets/**` meaning that this single `*` is any folder name and this double `**` translates to anything. Then our example URL:
```
http://my-server:8080/pentaho/content/tapa/resources/templates/MYTEMPLATE/assets/js/angularjs.js
```

Would match and be public.

# Contributions

If somebody sees any improvement opportunity, please feel free to suggest and contribute.

