/*
 * GNU LGPLv3
 */
package com.dosse.gravity2d;

/**
 * Settings used in the gravity simulation
 *
 * @author Federico
 */
public class Settings {

    /**
     * this is supposed to be the universal gravitational constant, but you can
     * consider it as a speed/precision knob: the closer it is to 0, the more
     * precise the simulation gets, but also slower. values above 1 are not
     * recommended. negative values make gravity repulsive.
     */
    public static final double G = 0.15;
    /**
     * limits the number of simulations per second. invalid values = no limit
     */
    public static final long SPS_LIMIT = 100;
    /**
     * load balancing enabled
     */
    public static final boolean ENABLE_POINT_REDIST = true;
    /**
     * load balancing setting
     */
    public static final float POINT_REDIST_THRESHOLD = 1.1f;
    /**
     * load balancing setting
     */
    public static final int POINT_REDIST_MIN_POINTS = 10;
    /**
     * if set to true, threads will release the CPU using a 1ns sleep instead of
     * Thread.yield()
     */
    public static final boolean CPU_RELEASE_WORKAROUND = false;
    /**
     * if set to true, slave threads will receive interrupt() calls from their
     * master when they have something to do, resulting in reduced CPU usage and
     * increased performance.<br>
     * Only tested on MS Windows.
     */
    public static final boolean INTERRUPT_SLAVE_THREADS = true;
    /**
     * priority of slave threads while doing calculations
     */
    public static final int SLAVE_PRIORITY_ACTIVE = Thread.NORM_PRIORITY + 1;
    /**
     * priority of slave threads while waiting
     */
    public static final int SLAVE_PRIORITY_INACTIVE = Thread.MIN_PRIORITY;
    /**
     * priority of master thread while doing calculations
     */
    public static final int MASTER_PRIORITY_ACTIVE = Thread.NORM_PRIORITY + 2;
    /**
     * priority of master thread while waiting
     */
    public static final int MASTER_PRIORITY_INACTIVE = Thread.MIN_PRIORITY;
}
