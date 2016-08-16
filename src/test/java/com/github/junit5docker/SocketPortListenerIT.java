package com.github.junit5docker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SocketPortListenerIT {

    private static final long TIME_BEFORE_LISTENING = 500;

    private SocketPortListener socketPortListener = new SocketPortListener();

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Test
    void shouldBlockUntilPortIsListenedTo() {
        listenToPort(8080).in(TIME_BEFORE_LISTENING);
        long duration = durationFor(() -> socketPortListener.waitForPortsToBeOpened(8080));
        assertTrue(duration >= TIME_BEFORE_LISTENING);
    }

    @Test
    void shouldBlockUntilAllPortAreListenedTo() {
        listenToPort(8080).in(TIME_BEFORE_LISTENING / 2);
        listenToPort(8081).in(TIME_BEFORE_LISTENING);
        long duration = durationFor(() -> socketPortListener.waitForPortsToBeOpened(8080, 8081));
        assertTrue(duration >= TIME_BEFORE_LISTENING);
    }

    @Test
    void shouldInterruptIfAskedTo() throws InterruptedException {
        executorService.submit(() -> {
            try {
                socketPortListener.waitForPortsToBeOpened(8081);
            } catch (InterruptedException e) {
                return;
            }
        });
        executorService.shutdownNow();
        assertTrue(executorService.awaitTermination(1000, TimeUnit.MILLISECONDS), "Waiting should have stopped");
    }

    @AfterEach
    void terminateExecutor() throws InterruptedException {
        executorService.shutdownNow();
        executorService.awaitTermination(1, TimeUnit.MILLISECONDS);
    }

    private long durationFor(RunnableWithException runnable) {
        try {
            long startTime = System.currentTimeMillis();
            runnable.run();
            return System.currentTimeMillis() - startTime;
        } catch (Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }

    private ServerDSL listenToPort(int port) {
        return new ServerDSL(port);
    }

    private class ServerDSL {
        private int port;

        ServerDSL(int port) {
            this.port = port;
        }

        public void in(long timeBeforeListening) {
            executorService.submit(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(timeBeforeListening);
                    ServerSocket serverSocket = new ServerSocket(port);
                    Socket accept = serverSocket.accept();
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException | IOException e) {
                    throw new AssertionError(e.getMessage());
                }
            });
        }
    }

    private interface RunnableWithException {
        void run() throws Exception;
    }
}