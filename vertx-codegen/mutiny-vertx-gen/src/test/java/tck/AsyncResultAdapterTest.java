package tck;

import io.vertx.mutiny.codegen.extra.MethodWithCompletable;
import io.vertx.mutiny.codegen.extra.MethodWithMaybeString;
import io.vertx.mutiny.codegen.extra.MethodWithSingleString;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

import java.util.concurrent.CompletionStage;

public class AsyncResultAdapterTest extends VertxTestBase {

    @Test
    public void testSingleReportingSubscribeUncheckedException() {
        RuntimeException cause = new RuntimeException();
        MethodWithSingleString meth = new MethodWithSingleString(handler -> {
            throw cause;
        });
        CompletionStage<String> single = meth.doSomethingWithResult().subscribeAsCompletionStage();
        single.whenComplete((result, err) -> {
            assertNull(result);
            assertNotNull(err);
            testComplete();
        });
        await();
    }

    @Test
    public void testMaybeReportingSubscribeUncheckedException() {
        RuntimeException cause = new RuntimeException();
        MethodWithMaybeString meth = new MethodWithMaybeString(handler -> {
            throw cause;
        });
        CompletionStage<String> single = meth.doSomethingWithMaybeResult().subscribeAsCompletionStage();
        single.whenComplete((result, err) -> {
            assertNull(result);
            assertNotNull(err);
            testComplete();
        });
        await();
    }

    @Test
    public void testCompletableReportingSubscribeUncheckedException() {
        RuntimeException cause = new RuntimeException();
        MethodWithCompletable meth = new MethodWithCompletable(handler -> {
            throw cause;
        });
        CompletionStage<Void> single = meth.doSomethingWithResult().subscribeAsCompletionStage();
        single.whenComplete((result, err) -> {
            assertNull(result);
            assertNotNull(err);
            testComplete();
        });
        await();
    }
}
