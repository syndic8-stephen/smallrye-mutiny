package io.smallrye.mutiny.groups;

import static io.smallrye.mutiny.helpers.ParameterValidation.nonNull;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.ParameterValidation;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.operators.multi.MultiOnTerminationInvokeUni;
import io.smallrye.mutiny.operators.multi.MultiSignalConsumerOp;

public class MultiOnTerminate<T> {

    private final Multi<T> upstream;

    public MultiOnTerminate(Multi<T> upstream) {
        this.upstream = ParameterValidation.nonNull(upstream, "upstream");
    }

    /**
     * Attaches an action that is executed when the {@link Multi} emits a completion or a failure or when the subscriber
     * cancels the subscription.
     *
     * @param callback the consumer receiving the failure if any and a boolean indicating whether the termination
     *        is due to a cancellation (the failure parameter would be {@code null} in this case). Must not
     *        be {@code null}.
     * @return the new {@link Multi}
     */
    public Multi<T> invoke(BiConsumer<Throwable, Boolean> callback) {
        return Infrastructure.onMultiCreation(new MultiSignalConsumerOp<>(
                upstream,
                null,
                null,
                null,
                callback,
                null,
                null));
    }

    /**
     * Attaches an action that is executed when the {@link Multi} emits a completion or a failure or when the subscriber
     * cancels the subscription. Unlike {@link #invoke(BiConsumer)}, the callback does not receive the failure or
     * cancellation details.
     *
     * @param action the action to execute when the streams completes, fails or the subscription gets cancelled. Must
     *        not be {@code null}.
     * @return the new {@link Multi}
     */
    public Multi<T> invoke(Runnable action) {
        Runnable runnable = nonNull(action, "action");
        return Infrastructure.onMultiCreation(new MultiSignalConsumerOp<>(
                upstream,
                null,
                null,
                null,
                (f, c) -> runnable.run(),
                null,
                null));
    }

    public Multi<T> invokeUni(BiFunction<Throwable, Boolean, Uni<?>> mapper) {
        return Infrastructure.onMultiCreation(new MultiOnTerminationInvokeUni<>(upstream, mapper));
    }
}
