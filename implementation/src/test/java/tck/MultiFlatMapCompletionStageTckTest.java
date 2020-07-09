package tck;

import static tck.Await.await;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.reactivestreams.Publisher;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class MultiFlatMapCompletionStageTckTest extends AbstractPublisherTck<Integer> {

    @Test
    public void flatMapCsStageShouldMapFutures() {
        CompletableFuture<Integer> one = new CompletableFuture<>();
        CompletableFuture<Integer> two = new CompletableFuture<>();
        CompletableFuture<Integer> three = new CompletableFuture<>();
        CompletionStage<List<Integer>> result = Multi.createFrom().<CompletionStage<Integer>> items(one, two, three)
                .onItem().transformToUni(i -> Uni.createFrom().completionStage(i)).merge()
                .collectItems().asList()
                .subscribeAsCompletionStage();
        one.complete(1);
        two.complete(2);
        three.complete(3);
        Assert.assertEquals(await(result), Arrays.asList(1, 2, 3));
    }

    @Override
    public Publisher<Integer> createPublisher(long elements) {
        return Multi.createFrom().items(IntStream.rangeClosed(1, (int) elements).boxed())
                // Use supplyAsync on purpose to check the concurrency.
                .onItem().transformToUni(i -> Uni.createFrom().completionStage(CompletableFuture.supplyAsync(() -> i)))
                .merge();
    }

    @Override
    public Publisher<Integer> createFailedPublisher() {
        return Multi.createFrom().<Integer> failure(new RuntimeException("failed"))
                // Use supplyAsync on purpose to check the concurrency.
                .onItem().transformToUni(i -> Uni.createFrom().completionStage(CompletableFuture.supplyAsync(() -> i)))
                .merge();
    }

    @Test
    public void flatMapCsStageShouldMaintainOrderOfFuturesIfConcurrencyIs1() throws Exception {
        CompletableFuture<Integer> one = new CompletableFuture<>();
        CompletableFuture<Integer> two = new CompletableFuture<>();
        CompletableFuture<Integer> three = new CompletableFuture<>();
        CompletionStage<List<Integer>> result = Multi.createFrom().<CompletionStage<Integer>> items(one, two, three)
                .onItem().transformToUni(i -> Uni.createFrom().completionStage(i)).merge(1)
                .collectItems().asList()
                .subscribeAsCompletionStage();
        three.complete(3);
        Thread.sleep(100L); //NOSONAR
        two.complete(2);
        Thread.sleep(100L); //NOSONAR
        one.complete(1);
        Assert.assertEquals(await(result), Arrays.asList(1, 2, 3));
    }

    @Test
    public void flatMapCsStageShouldOnlyMapOneElementAtATimeWithConcurrencyIs1() throws Exception {
        CompletableFuture<Integer> one = new CompletableFuture<>();
        CompletableFuture<Integer> two = new CompletableFuture<>();
        CompletableFuture<Integer> three = new CompletableFuture<>();
        AtomicInteger concurrentMaps = new AtomicInteger(0);
        CompletionStage<List<Integer>> result = Multi.createFrom().<CompletionStage<Integer>> items(one, two, three)
                .onItem().transformToUni(cs -> {
                    Assert.assertEquals(1, concurrentMaps.incrementAndGet());
                    return Uni.createFrom().completionStage(cs);
                }).merge(1)
                .collectItems().asList()
                .subscribeAsCompletionStage();
        Thread.sleep(100L); // NOSONAR
        concurrentMaps.decrementAndGet();
        one.complete(1);
        Thread.sleep(100L); // NOSONAR
        concurrentMaps.decrementAndGet();
        two.complete(2);
        Thread.sleep(100L); // NOSONAR
        concurrentMaps.decrementAndGet();
        three.complete(3);
        Assert.assertEquals(await(result), Arrays.asList(1, 2, 3));
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapCsStageShouldPropagateUpstreamErrors() {
        await(
                Multi.createFrom().failure(new QuietRuntimeException("failed"))
                        .onItem().transformToUni(i -> Uni.createFrom().completionStage(CompletableFuture.completedFuture(i)))
                        .merge()
                        .collectItems().asList()
                        .subscribeAsCompletionStage());
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapCsStageShouldHandleErrorsThrownByCallback() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = this.infiniteStream()
                .onTermination().invoke(() -> cancelled.complete(null))
                .onItem().transformToUni(i -> {
                    throw new QuietRuntimeException("failed");
                }).merge()
                .collectItems().asList()
                .subscribeAsCompletionStage();
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = QuietRuntimeException.class, expectedExceptionsMessageRegExp = "failed")
    public void flatMapCsStageShouldHandleFailedCompletionStages() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = this.infiniteStream()
                .onTermination().invoke(() -> cancelled.complete(null))
                .onItem().transformToUni(i -> {
                    CompletableFuture<Object> failed = new CompletableFuture<>();
                    failed.completeExceptionally(new QuietRuntimeException("failed"));
                    return Uni.createFrom().completionStage(failed);
                }).merge()
                .collectItems().asList()
                .subscribeAsCompletionStage();
        await(cancelled);
        await(result);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void flatMapCsStageShouldFailIfNullIsReturned() {
        CompletableFuture<Void> cancelled = new CompletableFuture<>();
        CompletionStage<List<Object>> result = this.infiniteStream()
                .onTermination().invoke(() -> cancelled.complete(null))
                .onItem().transformToUni(i -> Uni.createFrom().completionStage(CompletableFuture.completedFuture(null))
                        .onItem().ifNull().failWith(new NullPointerException()))
                .merge()
                .collectItems().asList()
                .subscribeAsCompletionStage();
        await(cancelled);
        await(result);
    }
}
