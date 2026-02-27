package com.qq.tars.aot.api;

import java.util.Collections;
import java.util.List;

/**
 * SPI interface for modules to declare their GraalVM native image metadata.
 *
 * <p>Implementations are discovered via {@link java.util.ServiceLoader} and
 * contribute reflection, proxy, and resource configuration entries.</p>
 */
public interface NativeDescriberRegistrar {

    /**
     * Returns types that need reflection registration.
     */
    default List<TypeDescriber> getTypeDescribers() {
        return Collections.emptyList();
    }

    /**
     * Returns JDK dynamic proxy configurations.
     */
    default List<JdkProxyDescriber> getProxyDescribers() {
        return Collections.emptyList();
    }

    /**
     * Returns resource patterns to include.
     */
    default List<ResourceDescriber> getResourceDescribers() {
        return Collections.emptyList();
    }
}
