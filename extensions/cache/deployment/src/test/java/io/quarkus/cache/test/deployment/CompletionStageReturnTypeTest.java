package io.quarkus.cache.test.deployment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.CompletionStage;

import javax.enterprise.inject.spi.DeploymentException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.cache.CacheResult;
import io.quarkus.cache.deployment.exception.NonBlockingReturnTypeException;
import io.quarkus.test.QuarkusUnitTest;

/**
 * The {@link CacheResult} annotation is not allowed on methods returning {@link CompletionStage} for now. This test should be
 * removed as soon as the cache supports non-blocking caching operations.
 */
public class CompletionStageReturnTypeTest {

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClass(TestResource.class)).assertException(e -> {
                assertEquals(DeploymentException.class, e.getClass());
                assertEquals(NonBlockingReturnTypeException.class, e.getCause().getClass());
            });

    @Test
    public void shouldNotBeInvoked() {
        fail("This method should not be invoked");
    }

    @Path("/test")
    static class TestResource {

        @GET
        @CacheResult(cacheName = "completionStageReturnTypeCache")
        public CompletionStage<String> shouldThrowDeploymentException(String key) {
            return null;
        }
    }
}
