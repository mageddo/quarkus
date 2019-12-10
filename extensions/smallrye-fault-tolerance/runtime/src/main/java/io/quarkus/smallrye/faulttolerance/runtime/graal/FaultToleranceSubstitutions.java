package io.quarkus.smallrye.faulttolerance.runtime.graal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;

import org.apache.commons.configuration.AbstractConfiguration;

import com.netflix.config.jmx.ConfigMBean;
import com.oracle.svm.core.annotate.Delete;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import io.smallrye.faulttolerance.HystrixInitializer;

public class FaultToleranceSubstitutions {
}

@TargetClass(className = "com.netflix.config.jmx.ConfigJMXManager")
final class Target_com_netflix_config_jmx_ConfigJMXManager {

    @Substitute
    public static ConfigMBean registerConfigMbean(AbstractConfiguration config) {
        return null;
    }

    @Substitute
    public static void unRegisterConfigMBean(AbstractConfiguration config, ConfigMBean mbean) {

    }
}

@TargetClass(className = "io.smallrye.faulttolerance.DefaultMethodFallbackProvider")
final class Target_io_smallrye_faulttolerance_DefaultMethodFallbackProvider {

    @TargetClass(className = "io.smallrye.faulttolerance.ExecutionContextWithInvocationContext")
    static final class Target_io_smallrye_faulttolerance_ExecutionContextWithInvocationContext {

    }

    @Substitute
    static Object getFallback(Method fallbackMethod,
            Target_io_smallrye_faulttolerance_ExecutionContextWithInvocationContext ctx)
            throws IllegalAccessException, InstantiationException, IllegalArgumentException, InvocationTargetException,
            Throwable {
        throw new RuntimeException("Not implemented in native mode");
    }
}

@TargetClass(HystrixInitializer.class)
final class Target_io_smallrye_faulttolerance_HystrixInitializer {
    
    @Delete
    void init(Object event) {
    }
}
