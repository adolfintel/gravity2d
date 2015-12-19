/*
 * GNU LGPLv3
 */
package com.dosse.gravity2d;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Federico
 */
public class Utils {

    /**
     * wait for ms milliseconds and ns nanoseconds
     *
     * @param ms milliseconds (0+)
     * @param ns nanoseconds (0-1000000)
     */
    public static final void sleep(long ms, int ns) {
        try {
            Thread.sleep(ms, ns);
        } catch (InterruptedException ex) {
        }
    }

    /**
     * wait for ns nanoseconds
     *
     * @param ns nanoseconds (0+)
     */
    public static final void sleep(long ns) {
        try {
            Thread.sleep(ns / 1000000L, (int) (ns % 1000000L));
        } catch (InterruptedException ex) {
        }
    }

    /**
     * release the CPU so other threads can be executed.<br>
     * Virtually equivalent to Thread.yield()
     */
    public static final void releaseCPU() {
        if (Settings.CPU_RELEASE_WORKAROUND) {
            try {
                Thread.sleep(0, 1); //a 1ns sleep may seem useless, but it works on systems where Thread.yield() doesn't.
            } catch (InterruptedException ex) {
            }
        } else {
            Thread.yield();
        }
    }

}
