package com.a2a.kafka.agents.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

public final class RetryUtils {
    private static final Logger log = LoggerFactory.getLogger(RetryUtils.class);

    private RetryUtils() {}

    public static <T> T runWithRetry(Supplier<T> supplier, int maxAttempts, Duration initialBackoff, double multiplier) {
        Objects.requireNonNull(supplier, "supplier");
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
        if (initialBackoff == null || initialBackoff.isNegative() || initialBackoff.isZero()) {
            initialBackoff = Duration.ofMillis(100);
        }
        if (multiplier < 1.0) multiplier = 1.0;

        Duration backoff = initialBackoff;
        int attempt = 0;
        RuntimeException lastException = null;
        while (attempt < maxAttempts) {
            attempt++;
            try {
                return supplier.get();
            } catch (RuntimeException ex) {
                lastException = ex;
                if (attempt >= maxAttempts) {
                    break;
                }
                log.warn("Retry attempt {}/{} failed: {}", attempt, maxAttempts, ex.toString());
                try {
                    Thread.sleep(backoff.toMillis());
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
                backoff = Duration.ofMillis((long) (backoff.toMillis() * multiplier));
            }
        }
        throw new RuntimeException("All retry attempts failed", lastException);
    }

    public static void runWithRetry(Runnable runnable, int maxAttempts, Duration initialBackoff, double multiplier) {
        runWithRetry(() -> { runnable.run(); return null; }, maxAttempts, initialBackoff, multiplier);
    }
}
