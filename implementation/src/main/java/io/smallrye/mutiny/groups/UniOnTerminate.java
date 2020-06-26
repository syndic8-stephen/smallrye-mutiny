package io.smallrye.mutiny.groups;

import static io.smallrye.mutiny.helpers.ParameterValidation.nonNull;

import java.util.function.Supplier;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.ParameterValidation;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.operators.UniOnTermination;
import io.smallrye.mutiny.operators.UniOnTerminationInvokeUni;
import io.smallrye.mutiny.tuples.Functions;

public class UniOnTerminate<T> {

    private final Uni<T> upstream;

    public UniOnTerminate(Uni<T> upstream) {
        this.upstream = nonNull(upstream, "upstream");
    }

    /**
     * Attaches an action that is executed when the {@link Uni} emits an item or a failure or when the subscriber
     * cancels the subscription.
     *
     * @param consumer the consumer receiving the item, the failure and a boolean indicating whether the termination
     *        is due to a cancellation (the 2 first parameters would be {@code null} in this case). Must not
     *        be {@code null} If the second parameter (the failure) is not {@code null}, the first is
     *        necessary {@code null} and the third is necessary {@code false} as it indicates a termination
     *        due to a failure.
     * @return the new {@link Uni}
     */
    public Uni<T> invoke(Functions.TriConsumer<T, Throwable, Boolean> consumer) {
        return Infrastructure.onUniCreation(new UniOnTermination<>(upstream, nonNull(consumer, "consumer")));
    }

    /**
     * Attaches an action that is executed when the {@link Uni} emits an item or a failure or when the subscriber
     * cancels the subscription. Unlike {@link #invoke(Functions.TriConsumer)} (Functions.TriConsumer)}, the callback does not
     * receive
     * the item, failure or cancellation.
     *
     * @param action the action to run, must not be {@code null}
     * @return the new {@link Uni}
     */
    public Uni<T> invoke(Runnable action) {
        Runnable runnable = nonNull(action, "action");
        return Infrastructure.onUniCreation(new UniOnTermination<>(upstream, (i, f, c) -> runnable.run()));
    }

    public Uni<T> invokeUni(Functions.Function3<? super T, Throwable, Boolean, Uni<?>> mapper) {
        return Infrastructure
                .onUniCreation(new UniOnTerminationInvokeUni<>(upstream, ParameterValidation.nonNull(mapper, "mapper")));
    }

    public Uni<T> invokeUni(Supplier<Uni<?>> supplier) {
        nonNull(supplier, "supplier");
        return invokeUni((i, f, c) -> supplier.get());
    }
}
