package io.smallrye.mutiny.operators.multi;

import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.ParameterValidation;
import io.smallrye.mutiny.helpers.Subscriptions;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.subscription.MultiSubscriber;
import io.smallrye.mutiny.subscription.SerializedSubscriber;

/**
 * Emits items from upstream until another Publisher signals an event (items, failures or completion).
 * Once this signal is received, the subscription to upstream is cancelled.
 *
 * @param <T> the type of item from upstream
 * @param <U> the type of item from the other publisher
 */
public final class MultiTakeUntilOtherOp<T, U> extends AbstractMultiOperator<T, T> {

    private final Publisher<U> other;

    public MultiTakeUntilOtherOp(Multi<? extends T> upstream, Publisher<U> other) {
        super(upstream);
        this.other = ParameterValidation.nonNull(other, "other");
    }

    @Override
    public void subscribe(MultiSubscriber<? super T> actual) {
        TakeUntilMainProcessor<T> mainSubscriber = new TakeUntilMainProcessor<>(actual);
        TakeUntilOtherSubscriber<U> otherSubscriber = new TakeUntilOtherSubscriber<>(mainSubscriber);
        other.subscribe(Infrastructure.onMultiSubscription(other, otherSubscriber));
        upstream.subscribe(Infrastructure.onMultiSubscription(upstream, mainSubscriber));
    }

    static final class TakeUntilOtherSubscriber<U> implements MultiSubscriber<U> {
        final TakeUntilMainProcessor<?> main;
        boolean once;

        TakeUntilOtherSubscriber(TakeUntilMainProcessor<?> main) {
            this.main = main;
        }

        @Override
        public void onSubscribe(Subscription s) {
            main.setOtherSubscription(s);
        }

        @Override
        public void onItem(U t) {
            onCompletion();
        }

        @Override
        public void onFailure(Throwable t) {
            if (once) {
                return;
            }
            once = true;
            main.onFailure(t);
        }

        @Override
        public void onCompletion() {
            if (once) {
                return;
            }
            once = true;
            main.onCompletion();
        }
    }

    static final class TakeUntilMainProcessor<T> extends MultiOperatorProcessor<T, T> {

        private final AtomicReference<Subscription> other = new AtomicReference<>();

        TakeUntilMainProcessor(Subscriber<? super T> downstream) {
            super(new SerializedSubscriber<>(downstream));
        }

        void setOtherSubscription(Subscription s) {
            if (other.compareAndSet(null, s)) {
                s.request(1);
            } else {
                s.cancel();
            }
        }

        @Override
        public void cancel() {
            super.cancel();
            Subscriptions.cancel(other);
        }

        @Override
        public void onItem(T t) {
            if (!isDone()) {
                downstream.onItem(t);
            }
        }

        @Override
        public void onFailure(Throwable failure) {
            super.onFailure(failure);
            Subscriptions.cancel(other);
        }

        @Override
        public void onCompletion() {
            super.onCompletion();
            Subscriptions.cancel(other);
        }
    }
}
