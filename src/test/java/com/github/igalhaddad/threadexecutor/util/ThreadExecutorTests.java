package com.github.igalhaddad.threadexecutor.util;

import static org.junit.jupiter.api.Assertions.*;

import com.github.igalhaddad.threadexecutor.timing.TimingExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@ExtendWith(TimingExtension.class)
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Test ThreadExecutor utility")
public class ThreadExecutorTests {
    private static final Logger logger = Logger.getLogger(ThreadExecutorTests.class.getName());
    private static final int SEQUENCE_LENGTH = 1000000;

    private static List<long[]> fibonacciSequences = new ArrayList<>();
    private long[] fibonacciSequence;

    @BeforeAll
    static void initAll() {
        logger.info(() -> "Number of available processors: " + Runtime.getRuntime().availableProcessors());
    }

    @BeforeEach
    void init() {
        this.fibonacciSequence = new long[SEQUENCE_LENGTH];
        fibonacciSequences.add(fibonacciSequence);
    }

    @AfterEach
    void tearDown() {
        int firstX = 10;
        logger.info(() -> "First " + firstX + " numbers: " + Arrays.stream(this.fibonacciSequence)
                .limit(firstX)
                .mapToObj(Long::toString)
                .collect(Collectors.joining(",", "[", ",...]")));
        int n = SEQUENCE_LENGTH - 1; // Last number
        assertFn(n);
        assertFn(n / 2);
        assertFn(n / 3);
        assertFn(n / 5);
        assertFn(n / 10);
        assertFn((n / 3) * 2);
        assertFn((n / 5) * 4);
    }

    private void assertFn(int n) {
        assertEquals(fibonacciSequence[n - 1] + fibonacciSequence[n - 2], fibonacciSequence[n]);
    }

    @AfterAll
    static void tearDownAll() {
        long[] fibonacciSequence = fibonacciSequences.iterator().next();
        for (int i = 1; i < fibonacciSequences.size(); i++) {
            assertArrayEquals(fibonacciSequence, fibonacciSequences.get(i));
        }
    }

    @Test
    @Order(1)
    @DisplayName("Calculate Fibonacci sequence sequentially")
    public void testSequential() {
        logger.info(() -> "Running sequentially. No parallelism");
        for (int i = 0; i < fibonacciSequence.length; i++) {
            fibonacciSequence[i] = Fibonacci.compute(i);
        }
    }

    @Test
    @Order(2)
    @DisplayName("Calculate Fibonacci sequence concurrently on all processors")
    public void testParallel1() {
        testParallel(Runtime.getRuntime().availableProcessors());
    }

    @Test
    @Order(3)
    @DisplayName("Calculate Fibonacci sequence concurrently on half of the processors")
    public void testParallel2() {
        testParallel(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
    }

    private void testParallel(int parallelism) {
        logger.info(() -> String.format("Running in parallel on %d processors", parallelism));
        ThreadExecutor.execute(parallelism, "FibonacciTask", fibonacciSequence,
                (long[] fibonacciSequence) -> Arrays.parallelSetAll(fibonacciSequence, Fibonacci::compute)
        );
    }

    static class Fibonacci {
        public static long compute(int n) {
            if (n <= 1)
                return n;
            long a = 0, b = 1;
            long sum = a + b; // for n == 2
            for (int i = 3; i <= n; i++) {
                a = sum; // using `a` for temporary storage
                sum += b;
                b = a;
            }
            return sum;
        }
    }
}
