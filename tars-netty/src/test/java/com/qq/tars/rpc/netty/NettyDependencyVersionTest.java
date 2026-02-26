package com.qq.tars.rpc.netty;

import io.netty.util.Version;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class NettyDependencyVersionTest {

    @Test
    public void testNettyRuntimeVersion() {
        Map<String, Version> versions = Version.identify();
        Assert.assertFalse(versions.isEmpty());

        boolean hasExpectedVersion = versions.values().stream()
                .map(Version::artifactVersion)
                .anyMatch(version -> version.startsWith("4.2.10"));

        Assert.assertTrue("expected netty 4.2.10 on classpath", hasExpectedVersion);
    }
}
