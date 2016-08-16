package com.github.junit5docker;

interface PortListener {
    void waitForPortsToBeOpened(int... port) throws InterruptedException;
}
