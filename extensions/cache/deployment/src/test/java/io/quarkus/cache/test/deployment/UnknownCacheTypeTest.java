package io.quarkus.cache.test.deployment;

import static org.junit.jupiter.api.Assertions.fail;

import javax.enterprise.inject.spi.DeploymentException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;

public class UnknownCacheTypeTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addAsResource("unknown-cache-type-test.properties",
                    "application.properties"))
            .setExpectedException(DeploymentException.class);

    @Test
    public void shouldNotBeInvoked() {
        fail("This method should not be invoked");
    }
}
