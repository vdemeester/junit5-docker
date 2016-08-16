package com.github.junit5docker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

class SocketPortListener implements PortListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketPortListener.class);

    @Override
    public void waitForPortsToBeOpened(int... ports) throws InterruptedException {
        for (int port : ports) {
            waitForPortToBeOpened(port);
        }
    }

    private void waitForPortToBeOpened(int port) throws InterruptedException {
        boolean connected = false;
        while (!connected) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Waiting for ports have been interrupted");
            }
            try (Socket localhost = new Socket("localhost", port)) {
                connected = true;
            } catch (ConnectException e) {
                LOGGER.info("Waiting for port {}", port);
                TimeUnit.MILLISECONDS.sleep(10);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
