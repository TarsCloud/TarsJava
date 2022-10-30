

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


###  Environmental dependence
- JDK1.8 or above
- Maven 3.5 or above

###




## TarsFramework deployment by Docker

This guide uses Docker to complete the deployment of Tars.[Macos、 Linux]

**2. Start TarsFramework in Docker**

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

**Note: - P 18600-18700:18600-18700 parameter opens 18600-18700 port for application. You can add more ports if necessary**


# Quick Start To TarsServer

This guide gives you a quick introduction to Tars in Java through simple server

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
            <version>1.7.3</version>
        </dependency>
    </dependencies>
```

**Plugin dependency**

```xml
<!--tars2java plugin-->
<plugin>
	<groupId>com.tencent.tars</groupId>
	<artifactId>tars-maven-plugin</artifactId>
	<version>1.7.0</version>
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

Tars has its own interface file format. First, we need to define the Tars interface file. Create a new hello.tars file in the resources directory with the following content:

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

Then we need to convert the Tars interface file to the server interface code using the tars-maven-plugin. In the project root directory, execute `mvn tars: tars2java` to get HelloServant.java, the content is as follows:

```java
@Servant
public interface HelloServant {

	public String hello(int no, String name);
}
```

##### Interface implementation

Next we need to implement the generated server interface. Create a new HelloServantImpl.java file, implement the HelloServant.java interface, and expose the service through the @TarsServant annotation, where 'HelloObj' is the servant name, corresponding to the name in the web management platform.

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




| Directory               | Features               |
| ------------------ | ---------------- |
| net                | Source code implementation of Java language net framework         |
| core               | Source code implementation of Java language rpc framework         |
| tools              | Source code implementation of framework tools, maven plug-ins, etc |
| examples           | Sample code for the Java language framework          |
| distributedContext | Source code implementation of Java language framework's distributed context       |
| protobuf           | Source code implementation of pb protocol support        |
| spring             | Source code implementation of spring framework support      |




