Tars Java - An RPC library and framework
========================================
[![Latest release](https://img.shields.io/github/v/release/tarsCloud/TarsJava)](https://github.com/TarsCloud/TarsJava/releases/latest)

This project is the source code of the Tars RPC framework Java language.

<table>
  <tr>
    <td><b>Homepage:</b></td>
    <td><a href="https://tarscloud.org">tarscloud.org</a></td>
  </tr>
    <tr>
      <td><b>中文版:</b></td>
      <td><a href="README.zh.md">点我查看中文版</a></td>
    </tr>
</table>

### Environmental dependence

- JDK1.8 or above
- Maven 3.5 or above

###

## TarsFramework deployment by Docker

This guide uses Docker to complete the deployment of Tars.[Macos、 Linux]

** Start TarsFramework in Docker**

```bash
docker pull tarscloud/framework:latest
docker pull tarscloud/tars-node:latest
docker pull   mysql:5.6
docker network create -d bridge --subnet=172.25.0.0/16 --gateway=172.25.0.1 tars
docker run -d \
--net=tars \
-e MYSQL_ROOT_PASSWORD="root@appinside" \
--ip="172.25.0.2" \
--name=tars-mysql \
mysql:5.6
sleep 30s
docker run -d \
--net=tars \
-e MYSQL_HOST=172.25.0.2 \
-e MYSQL_ROOT_PASSWORD='root@appinside' \
-eREBUILD=false  -eSLAVE=false \
-e INET=eth0 \
--ip="172.25.0.4" \
-p 3000-3001:3000-3001 \
tarscloud/framework
sleep 60s
docker run -d --net=tars --ip="172.25.0.3"  -eWEB_HOST=http://172.25.0.4:3000        tarscloud/tars-node
```

**Note: - P 18600-18700:18600-18700 parameter opens 18600-18700 port for application. You can add more ports if
necessary**

# Quick Start To TarsServer(Provider)

This guide gives you a quick introduction to Tars in Java through simple
server  [example server](./examples/tars-spring-boot-server)

#### Project structure

```text
├── pom.xml
└── src
   └── main
       ├── java
       │   └── tars
       │       └── testapp
       │          ├── HelloServant.java
       │          ├── QuickStartApplication.java
       │          └── impl
       │                └── HelloServantImpl.java
       └── resources
           └── hello.tars
       
```

#### Dependency configuration

The following configuration needs to be added in pom.xml:

**Spring boot and Tars framework dependency**

```xml

<properties>
    <spring-boot.version>2.3.5.RELEASE</spring-boot.version>
</properties>

<dependencyManagement>
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencies>
</dependencyManagement>

<dependencies>
<dependency>
    <groupId>com.tencent.tars</groupId>
    <artifactId>tars-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
</dependencies>
```

**Plugin dependency**

```xml
<!--tars2java plugin-->
<plugin>
    <groupId>com.tencent.tars</groupId>
    <artifactId>tars-maven-plugin</artifactId>
    <version>2.0.0</version>
    <configuration>
        <tars2JavaConfig>
            <!-- tars file location -->
            <tarsFiles>
                <tarsFile>${basedir}/src/main/resources/hello.tars</tarsFile>
            </tarsFiles>
            <!-- Source file encoding -->
            <tarsFileCharset>UTF-8</tarsFileCharset>
            <!-- Generate server code -->
            <servant>true</servant>
            <!-- Generated source code encoding -->
            <charset>UTF-8</charset>
            <!-- Generated source code directory -->
            <srcPath>${basedir}/src/main/java</srcPath>
            <!-- Generated source code package prefix -->
            <packagePrefixName>com.qq.tars.quickstart.server.</packagePrefixName>
        </tars2JavaConfig>
    </configuration>
</plugin>
        <!--package plugin-->
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-jar-plugin</artifactId>
<version>2.6</version>
<configuration>
    <archive>
        <manifestEntries>
            <Class-Path>conf/</Class-Path>
        </manifestEntries>
    </archive>
</configuration>
</plugin>
<plugin>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-maven-plugin</artifactId>
<configuration>
    <!--set mainclass-->
    <mainClass>com.qq.tars.quickstart.server.QuickStartApplication</mainClass>
</configuration>
<executions>
    <execution>
    <goals>
        <goal>repackage</goal>
    </goals>
</executions>
</plugin>
```

#### Service development

##### Tars interface file definition

Tars has its own interface file format. First, we need to define the Tars interface file. Create a new hello.tars file
in the resources directory with the following content:

```text
module TestApp
{
	interface Hello
	{
	    string hello(int no, string name);
	};
};
```

##### Interface file compilation

Then we need to convert the Tars interface file to the server interface code using the tars-maven-plugin. In the project
root directory, execute `mvn tars: tars2java` to get HelloServant.java, the content is as follows:

```java

@Servant
public interface HelloServant {

    public String hello(int no, String name);
}
```

##### Interface implementation

Next we need to implement the generated server interface. Create a new HelloServantImpl.java file, implement the
HelloServant.java interface, and expose the service through the @TarsServant annotation, where 'HelloObj' is the servant
name, corresponding to the name in the web management platform.

```java

@TarsServant("HelloObj")
public class HelloServantImpl implements HelloServant {

    @Override
    public String hello(int no, String name) {
        return String.format("hello no=%s, name=%s, time=%s", no, name, System.currentTimeMillis());
    }
}
```

##### Tars service enabling

Finally, add @EnableTarsServer annotation in the spring boot startup class QuickStartApplication to enable Tars service:

```java

@SpringBootApplication
@EnableTarsServer
public class QuickStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuickStartApplication.class, args);
    }
}
```

Using spring-boot-maven-plugin, execute `mvn package` in the root directory to package it into a jar.

### Client(Consumer) development

This guide gives you a quick introduction to Tars in Java through simple
client [example client](./examples/tars-spring-boot-server)

#### Project structure

```text
├── pom.xml
└── src
   └── main
       ├── java
       │   └── tars
       │       └── testapp
       │          ├── HelloPrx.java
       │          ├── HelloPrxCallback.java
       │          ├── App.java
       │          └── impl
       │                └── ClientServantImpl.java
       └── resources
           ├── hello.tars
           └── client.tars
       
```

#### Dependency configuration

The following configuration needs to be added in pom.xml:

**Spring boot and Tars framework dependency**

```xml

<properties>
    <spring-boot.version>2.0.3.RELEASE</spring-boot.version>
</properties>

<dependencyManagement>
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
</dependencies>
</dependencyManagement>

<dependencies>
<dependency>
    <groupId>com.tencent.tars</groupId>
    <artifactId>tars-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
</dependencies>
```

**Plugin dependency**

```xml
<!--tars2java plugin-->
<plugin>
    <groupId>com.tencent.tars</groupId>
    <artifactId>tars-maven-plugin</artifactId>
    <version>2.0.0</version>
    <configuration>
        <tars2JavaConfig>
            <!-- tars file location -->
            <tarsFiles>
                <tarsFile>${basedir}/src/main/resources/hello.tars</tarsFile>
            </tarsFiles>
            <!-- Source file encoding -->
            <tarsFileCharset>UTF-8</tarsFileCharset>
            <!-- Generate server code -->
            <servant>false</servant>
            <!-- Generated source code encoding -->
            <charset>UTF-8</charset>
            <!-- Generated source code directory -->
            <srcPath>${basedir}/src/main/java</srcPath>
            <!-- Generated source code package prefix -->
            <packagePrefixName>com.tencent.tars.client.</packagePrefixName>
        </tars2JavaConfig>
    </configuration>
</plugin>
        <!--package plugin-->
<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-jar-plugin</artifactId>
<version>2.6</version>
<configuration>
    <archive>
        <manifestEntries>
            <Class-Path>conf/</Class-Path>
        </manifestEntries>
    </archive>
</configuration>
</plugin>
<plugin>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-maven-plugin</artifactId>
<configuration>
    <!--set mainclass-->
    <mainClass>com.tencent.tars.App</mainClass>
</configuration>
<executions>
    <execution>
    <goals>
        <goal>repackage</goal>
    </goals>
</executions>
</plugin>
```

#### Service development

##### Server interface file compilation

After the server service development is completed, we first need to obtain the client interface code of the server
service. Copy the hello.tars file on the server side to the resources directory, and execute `mvn tars: tars2java` in
the project root directory to get HelloPrx.java. At this time, the proxy interface of the server service is obtained,
and three calling methods are provided, namely synchronous call, asynchronous call and promise call.

```java

@Servant
public interface HelloPrx {

    String hello(int no, String name);

    CompletableFuture<String> promise_hello(int no, String name);

    String hello(int no, String name, @TarsContext java.util.Map<String, String> ctx);

    void async_hello(@TarsCallback HelloPrxCallback callback, int no, String name);

    void async_hello(@TarsCallback HelloPrxCallback callback, int no, String name, @TarsContext java.util.Map<String, String> ctx);
}
```

The promise call is a new feature of Tars v2.0.0. For specific use, please refer to
the [Tars file reference](./docs-en/tars-reference.md).

##### Client interface file definition

Then define the interface file of the client service. Create a new client.tars file in the resources directory with the
following content:

```text
module TestApp
{
	interface Client
	{
	    string rpcHello(int no, string name);
	};
};
```

##### Client interface file compilation

Next, we need to use the tars-maven-plugin to generate client service interface code. Modify the tars2java plugin
dependency of pom.xml as follows. Note that `<servant> </ servant>` is set to true.

```xml
<!--tars2java plugin-->
<plugin>
    <groupId>com.tencent.tars</groupId>
    <artifactId>tars-maven-plugin</artifactId>
    <version>2.0.0</version>
    <configuration>
        <tars2JavaConfig>
            <!-- tars file location -->
            <tarsFiles>
                <tarsFile>${basedir}/src/main/resources/client.tars</tarsFile>
            </tarsFiles>
            <!-- Source file encoding -->
            <tarsFileCharset>UTF-8</tarsFileCharset>
            <!-- Generate server code -->
            <servant>true</servant>
            <!-- Generated source code encoding -->
            <charset>UTF-8</charset>
            <!-- Generated source code directory -->
            <srcPath>${basedir}/src/main/java</srcPath>
            <!-- Generated source code package prefix -->
            <packagePrefixName>com.tencent.tars.client.</packagePrefixName>
        </tars2JavaConfig>
    </configuration>
</plugin>
```

In the root directory of the project, execute `mvn tars: tars2java` again to get ClientServant.java. The contents are as
follows:

```java

@Servant
public interface ClientServant {
    public String rpcHello(int no, String name);
}
```

##### Interface implementation

We need to implement the generated client service interface. Create a new ClientServantImpl.java file, implement the
HelloServant.java interface, and expose the service through the @TarsServant annotation, where 'HelloObj' is the servant
name, corresponding to the name in the web management platform.

By adding the @TarsClient annotation to the client properties, the corresponding service can be automatically injected.
If only the Obj name is filled, the default value is used to inject the client. In addition, the client configuration
can be customized in the annotation, such as setting the synchronous call timeout time.

```java

@TarsServant("ClientObj")
public class ClientServantImpl implements ClientServant {
    @TarsClient("TestServer.HelloServer.HelloObj")
    HelloPrx helloPrx;

    String res = "";

    @Override
    public String rpcHello(int no, String name) {
        //sync call
        String syncres = helloPrx.hello(1000, "Hello World");
        res += "sync_res: " + syncres + " ";
        //async call
        helloPrx.async_hello(new HelloPrxCallback() {

            @Override
            public void callback_expired() {
            }

            @Override
            public void callback_exception(Throwable ex) {
            }

            @Override
            public void callback_hello(String ret) {
                res += "async_res: " + ret + " ";

            }
        }, 1000, "HelloWorld");
        //promise call
        helloPrx.promise_hello(1000, "hello world").thenCompose(x -> {
            res += "promise_res: " + x;
            return CompletableFuture.completedFuture(0);
        });
        return res;
    }
}

```

##### Tars service enabling

Finally, add @EnableTarsServer annotation in the spring boot startup class App to enable Tars service:

```java

@SpringBootApplication
@EnableTarsServer
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
```

##### Service packaging

Using spring-boot-maven-plugin, execute `mvn package` in the root directory to package it into a jar.

### Function description

Tars-java is compatible with the Spring Cloud system, users can integrate the Tars-java into Spring Cloud. 

| Directory |Features | 
| :----- | :----- | 
| net | Source code implementation of Java language net framework |
| core | Source code implementation of Java language rpc framework | 
| tools | Source code implementation of framework tools, maven plug-ins, etc | 
| examples | Sample code for the Java language framework | 
| distributedContext | Source code implementation of Java language framework's distributed context | 
| protobuf | Source code implementation of pb protocol support |
| spring | Source code implementation of spring framework support |




