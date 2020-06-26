package io.smallrye.mutiny.operators;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Subscription;
import org.testng.annotations.Test;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.test.MultiAssertSubscriber;

public class MultiOnTerminationUniInvokeTest {

    @Test
    public void testTerminationWhenItemIsEmitted() {
        MultiAssertSubscriber<Integer> ts = MultiAssertSubscriber.create();

        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicReference<Integer> item = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean completion = new AtomicBoolean();
        AtomicLong requests = new AtomicLong();
        AtomicBoolean cancellation = new AtomicBoolean();

        AtomicBoolean termination = new AtomicBoolean();
        AtomicReference<Throwable> terminationException = new AtomicReference<>();
        AtomicBoolean terminationCancelledFlag = new AtomicBoolean();

        Multi.createFrom().item(1)
                .onSubscribe().invoke(subscription::set)
                .on().item().invoke(item::set)
                .on().failure().invoke(failure::set)
                .on().completion(() -> completion.set(true))
                .onTermination().invokeUni((t, c) -> {
                    termination.set(true);
                    terminationException.set(t);
                    terminationCancelledFlag.set(c);
                    return Uni.createFrom().item(69);
                })
                .on().request(requests::set)
                .on().cancellation(() -> cancellation.set(true))
                .subscribe(ts);

        ts
                .request(20)
                .assertCompletedSuccessfully()
                .assertReceived(1);

        assertThat(subscription.get()).isNotNull();
        assertThat(item.get()).isEqualTo(1);
        assertThat(failure.get()).isNull();
        assertThat(completion.get()).isTrue();
        assertThat(requests.get()).isEqualTo(20);
        assertThat(cancellation.get()).isFalse();

        assertThat(termination.get()).isTrue();
        assertThat(terminationException.get()).isNull();
        assertThat(terminationCancelledFlag.get()).isFalse();
    }

    @Test
    public void testTerminationWhenErrorIsEmitted() {
        MultiAssertSubscriber<Object> ts = MultiAssertSubscriber.create();

        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicReference<Object> item = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean completion = new AtomicBoolean();
        AtomicLong requests = new AtomicLong();
        AtomicBoolean cancellation = new AtomicBoolean();

        AtomicBoolean termination = new AtomicBoolean();
        AtomicReference<Throwable> terminationException = new AtomicReference<>();
        AtomicBoolean terminationCancelledFlag = new AtomicBoolean();

        Multi.createFrom().failure(new IOException("boom"))
                .onSubscribe().invoke(subscription::set)
                .on().item().invoke(item::set)
                .on().failure().invoke(failure::set)
                .on().completion(() -> completion.set(true))
                .onTermination().invokeUni((t, c) -> {
                    termination.set(true);
                    terminationException.set(t);
                    terminationCancelledFlag.set(c);
                    return Uni.createFrom().item(69);
                })
                .on().request(requests::set)
                .on().cancellation(() -> cancellation.set(true))
                .subscribe(ts);

        ts
                .request(20)
                .assertHasNotReceivedAnyItem()
                .assertHasFailedWith(IOException.class, "boom");

        assertThat(subscription.get()).isNotNull();
        assertThat(item.get()).isNull();
        assertThat(failure.get()).isNotNull().isInstanceOf(IOException.class).hasMessageContaining("boom");
        assertThat(completion.get()).isFalse();
        assertThat(requests.get()).isEqualTo(0L);
        assertThat(cancellation.get()).isFalse();

        assertThat(termination.get()).isTrue();
        assertThat(terminationException.get()).isNotNull().isInstanceOf(IOException.class).hasMessageContaining("boom");
        assertThat(terminationCancelledFlag.get()).isFalse();
    }

    @Test
    public void testTerminationWhenItemIsEmittedButUniInvokeIsFailed() {
        MultiAssertSubscriber<Integer> ts = MultiAssertSubscriber.create();

        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicReference<Integer> item = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean completion = new AtomicBoolean();
        AtomicLong requests = new AtomicLong();
        AtomicBoolean cancellation = new AtomicBoolean();

        AtomicBoolean termination = new AtomicBoolean();
        AtomicReference<Throwable> terminationException = new AtomicReference<>();
        AtomicBoolean terminationCancelledFlag = new AtomicBoolean();

        Multi.createFrom().item(1)
                .onSubscribe().invoke(subscription::set)
                .on().item().invoke(item::set)
                .on().failure().invoke(failure::set)
                .on().completion(() -> completion.set(true))
                .onTermination().invokeUni((t, c) -> {
                    termination.set(true);
                    terminationException.set(t);
                    terminationCancelledFlag.set(c);
                    return Uni.createFrom().failure(new IOException("bam"));
                })
                .on().request(requests::set)
                .on().cancellation(() -> cancellation.set(true))
                .subscribe(ts);

        ts
                .request(20)
                .assertReceived(1)
                .assertHasFailedWith(IOException.class, "bam");

        assertThat(subscription.get()).isNotNull();
        assertThat(item.get()).isEqualTo(1);
        assertThat(failure.get()).isNull();
        assertThat(completion.get()).isTrue();
        assertThat(requests.get()).isEqualTo(20);
        assertThat(cancellation.get()).isFalse();

        assertThat(termination.get()).isTrue();
        assertThat(terminationException.get()).isNull();
        assertThat(terminationCancelledFlag.get()).isFalse();
    }

    @Test
    public void testTerminationWhenItemIsEmittedButUniInvokeThrowsException() {
        MultiAssertSubscriber<Integer> ts = MultiAssertSubscriber.create();

        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicReference<Integer> item = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean completion = new AtomicBoolean();
        AtomicLong requests = new AtomicLong();
        AtomicBoolean cancellation = new AtomicBoolean();

        AtomicBoolean termination = new AtomicBoolean();
        AtomicReference<Throwable> terminationException = new AtomicReference<>();
        AtomicBoolean terminationCancelledFlag = new AtomicBoolean();

        Multi.createFrom().item(1)
                .onSubscribe().invoke(subscription::set)
                .on().item().invoke(item::set)
                .on().failure().invoke(failure::set)
                .on().completion(() -> completion.set(true))
                .onTermination().invokeUni((t, c) -> {
                    termination.set(true);
                    terminationException.set(t);
                    terminationCancelledFlag.set(c);
                    throw new RuntimeException("bam");
                })
                .on().request(requests::set)
                .on().cancellation(() -> cancellation.set(true))
                .subscribe(ts);

        ts
                .request(20)
                .assertReceived(1)
                .assertHasFailedWith(RuntimeException.class, "bam");

        assertThat(subscription.get()).isNotNull();
        assertThat(item.get()).isEqualTo(1);
        assertThat(failure.get()).isNull();
        assertThat(completion.get()).isTrue();
        assertThat(requests.get()).isEqualTo(20);
        assertThat(cancellation.get()).isFalse();

        assertThat(termination.get()).isTrue();
        assertThat(terminationException.get()).isNull();
        assertThat(terminationCancelledFlag.get()).isFalse();
    }

    @Test
    public void testTerminationWithCancellation() {
        MultiAssertSubscriber<Integer> ts = MultiAssertSubscriber.create();

        AtomicReference<Integer> item = new AtomicReference<>();
        AtomicBoolean cancellation = new AtomicBoolean();

        AtomicBoolean termination = new AtomicBoolean();
        AtomicReference<Throwable> terminationException = new AtomicReference<>();
        AtomicBoolean terminationCancelledFlag = new AtomicBoolean();

        Multi.createFrom().item(1)
                .on().item().invoke(item::set)
                .onTermination().invokeUni((t, c) -> {
                    termination.set(true);
                    terminationException.set(t);
                    terminationCancelledFlag.set(c);
                    return Uni.createFrom().item(100);
                })
                .on().cancellation(() -> cancellation.set(true))
                .subscribe(ts);

        ts.cancel()
                .assertHasNotReceivedAnyItem()
                .assertHasNotCompleted();

        assertThat(item.get()).isNull();
        assertThat(cancellation.get()).isTrue();
        assertThat(termination.get()).isTrue();
        assertThat(terminationException.get()).isNull();
        assertThat(terminationCancelledFlag.get()).isTrue();
    }
}
