# tars-netty

The netty implementation for tars transporter layer.

Usage
------

1. Use `tars-core` directly. In this case, you need to import `tars-netty` manually.

```xml
<?xml version="1.0" encoding="utf-8" ?>
<dependencies>
    <dependency>
        <groupId>com.tencent.tars</groupId>
        <artifactId>tars-core</artifactId>
        <version>2.0.0</version>
    </dependency>
    <dependency>
        <groupId>com.tencent.tars</groupId>
        <artifactId>tars-netty</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

2. Use `tars-spring` or `tars-spring-boot-starter`. `tars-netty` will be imported automatically, so no need to change
   your pom.xml.

```xml
<?xml version="1.0" encoding="utf-8" ?>
<dependencies>
    <dependency>
        <groupId>com.tencent.tars</groupId>
        <artifactId>tars-spring-boot-starter</artifactId>
        <version>2.0.0</version>
    </dependency>
</dependencies>
```

Implementation Details
------

`TransporterAbstractFactory` in tars-core use **SPI** to load the implementation for `TransporterFactory`, which will be
used to connect to a tars server node(client-side) or start listening the ip:port(server-side).

`tars-netty` provides three core classes: `NettyTransporterFactory`, `NettyTransporterServer` and `NettyServantClient`,
which are used to adapt **netty** and **tars**.

For more details, please refer to the javadoc.