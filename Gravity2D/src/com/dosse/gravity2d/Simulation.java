/*
 * GNU LGPLv3
 */
package com.dosse.gravity2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Simulates gravity attraction between all Points in a 2D environment.<br>
 * ~98% parallelization
 *
 * @author Federico
 */
public class Simulation extends Thread {

    /**
     * this list contains ALL the points of the simulation
     */
    protected final ArrayList<Point> points = new ArrayList<Point>();

    /**
     * this list contains a bunch of couples (Point, Point): each couple
     * represents 2 Points that are colliding.<br>
     * it is filled by several SlaveThreads and used by handleCollisions() to
     * join all colliding Points (even if there are more than 2 colliding
     * together)<br>
     */
    private final ArrayList<Point[]> collisionCouples = new ArrayList<Point[]>();

    /**
     * set to true to safely stop the thread, then wait until isAlive()==false
     */
    public boolean stopASAP = false;

    /**
     * set to true to pause the simulation (it will pause after the current step
     * is completed)<br>
     * add/remove operations are handled even while paused.
     */
    public boolean pause = false;

    /**
     * List of Slave threads
     */
    private SlaveThread[] slaves;

    /**
     * Slave threads do all the dirty work coming from the Master thread.<br>
     * Each SlaveThread manages some of the Points in the simulation.<br>
     * Communication with Master is done via the setState(int) and
     * getThreadState() methods.<br>
     * STATE_READY means that the thread is ready to receive orders. when a
     * thread is started, it is in this state. add/remove operations can only be
     * done when the thread is in this state<br>
     * STATE_CALCULATE_GRAVITY generates attraction vectors for all assigned
     * Points<br>
     * STATE_APPLY_GRAVITY applies the calculated attraction vectors<br>
     * STATE_DETECT_COLLISIONS runs collision detection for all assigned
     * Points.<br>
     * Note that the actual code of these operations is in the Point class.<br>
     *
     */
    public class SlaveThread extends Thread {

        /**
         * List of Points assigned to this thread
         */
        private ArrayList<Point> threadPoints = new ArrayList<Point>();

        /**
         * STATE_READY means that the thread is ready to receive orders. when a
         * thread is started, it is in this state<br>
         * STATE_CALCULATE_GRAVITY generates attraction vectors for all assigned
         * Points<br>
         * STATE_APPLY_GRAVITY applies the calculated attraction vectors<br>
         * STATE_DETECT_COLLISIONS runs collision detection for all assigned
         * Points.<br>
         */
        public static final int STATE_READY = 0, STATE_CALCULATE_GRAVITY = 1, STATE_APPLY_GRAVITY = 2, STATE_DETECT_COLLISIONS = 3;
        /**
         * current state
         */
        private int state = STATE_READY;

        /**
         * get current state<br>
         * STATE_READY means that the thread is ready to receive orders. when a
         * thread is started, it is in this state<br>
         * STATE_CALCULATE_GRAVITY generates attraction vectors for all assigned
         * Points<br>
         * STATE_APPLY_GRAVITY applies the calculated attraction vectors<br>
         * STATE_DETECT_COLLISIONS runs collision detection for all assigned
         * Points.<br>
         *
         * @return state
         */
        public int getThreadState() {
            return state;
        }

        /**
         * set new state<br/>
         * can only be used in STATE_READY
         *
         * @param newState
         */
        public void setState(int newState) {
            synchronized (threadPoints) {
                if (state == STATE_READY) {
                    state = newState;
                    if (Settings.INTERRUPT_SLAVE_THREADS) {
                        interrupt();
                    }
                } else {
                    throw new IllegalStateException("Tried setting state while an operation was in progress");
                }
            }
        }

        /**
         * set to true to safely stop thread, then wait until isAlive()==false
         */
        public boolean stopASAP = false;

        /**
         * creates and starts a new SlaveThread, with no assigned points and in
         * STATE_READY
         */
        public SlaveThread() {
            start();
        }

        /**
         * add a Point<br>
         * can only be used in STATE_READY
         *
         * @param p Point to add
         */
        public void add(Point p) {
            synchronized (threadPoints) {
                if (state == STATE_READY) {
                    threadPoints.add(p);
                } else {
                    throw new IllegalStateException("Tried adding a Point while doing calculations");
                }
            }
        }

        /**
         * remove a Point from thread list<br>
         * can only be used in STATE_READY
         *
         * @param p Point to remove
         */
        public void remove(Point p) {
            synchronized (threadPoints) {
                if (state == STATE_READY) {
                    threadPoints.remove(p);
                } else {
                    throw new IllegalStateException("Tried removing a Point while doing calculations");
                }
            }
        }

        /**
         * removes all Points from thread list<br>
         * can only be used in STATE_READY
         *
         */
        public void reset() {
            synchronized (threadPoints) {
                if (state == STATE_READY) {
                    threadPoints.clear();
                } else {
                    throw new IllegalStateException("Tried resetting while doing calculations");
                }
            }
        }

        /**
         * Here lies the actual code of the Slave
         */
        @Override
        public void run() {
            setName("Gravity - Slave Thread");
            for (;;) {
                if (state == STATE_READY) {
                    //nothing to do
                    setPriority(Settings.SLAVE_PRIORITY_INACTIVE);
                    if (Settings.INTERRUPT_SLAVE_THREADS) {
                        Utils.sleep(10, 0);
                    } else {
                        Utils.releaseCPU();
                    }
                    if (stopASAP) {
                        return;
                    }
                }
                if (state == STATE_CALCULATE_GRAVITY) {
                    //calculate attraction vectors
                    setPriority(Settings.SLAVE_PRIORITY_ACTIVE);
                    for (Point p : threadPoints) {
                        p.calculateAttraction(points);
                    }
                    state = STATE_READY;
                }
                if (state == STATE_APPLY_GRAVITY) {
                    //apply attraction vectors
                    setPriority(Settings.SLAVE_PRIORITY_ACTIVE);
                    for (Point p : threadPoints) {
                        p.applyAttractionVector();
                    }
                    state = STATE_READY;
                }
                if (state == STATE_DETECT_COLLISIONS) {
                    //detect collisions
                    setPriority(Settings.SLAVE_PRIORITY_ACTIVE);
                    final LinkedList<Point[]> collisions = new LinkedList<Point[]>();
                    for (Point p : threadPoints) {
                        for (Point p2 : points) {
                            if (p == p2) {
                                continue;
                            }
                            if (p.collidesWith(p2)) {
                                collisions.add(new Point[]{p, p2});
                            }
                        }
                    }
                    if (!collisions.isEmpty()) {
                        //add all detected collision couples to main list. collisions are first stored in a temporary list so synchronization has to be done only once instead of once per couple
                        synchronized (collisionCouples) {
                            collisionCouples.addAll(collisions);
                        }
                    }
                    state = STATE_READY;
                }
            }
        }
    }

    /**
     * Create a new simulation. Use start() to actually start it<br>
     * By default, it uses nCores slave threads
     */
    public Simulation() {
        int nCores = Runtime.getRuntime().availableProcessors();
        int nThreads = nCores * 2;//may seem stupid, but it slightly improves performance
        slaves = new SlaveThread[nThreads];
        for (int i = 0; i < nThreads; i++) {
            slaves[i] = new SlaveThread();
        }
    }

    /**
     * waits for all slave threads to be in STATE_READY
     */
    private void sync() {
        setPriority(Settings.MASTER_PRIORITY_INACTIVE);
        for (SlaveThread t : slaves) {
            while (t.getThreadState() != SlaveThread.STATE_READY) {
                Utils.releaseCPU();
            }
        }
        setPriority(Settings.MASTER_PRIORITY_ACTIVE);
    }

    /**
     * Lists of pending add/remove operations that will be done after the
     * current simulation step is complete. Instead of having the add/remove
     * methods wait for mutex lock, they just queue the operations in these
     * lists, and then the master will do the operations whenever possible.
     */
    private final ArrayList<Point> pendingAdds = new ArrayList<Point>(), pendingRemovals = new ArrayList<Point>();

    /**
     * current Simulations Per Second. updated by the master thread after each
     * step
     */
    private int SPS = 0;

    /**
     * Master thread code
     */
    @Override
    public void run() {
        setName("Gravity - Master Thread");
        for (;;) {
            long timestamp = System.nanoTime();
            if (stopASAP) {
                for (SlaveThread t : slaves) {
                    t.stopASAP = true;
                    while (t.isAlive()) {
                        Utils.releaseCPU();
                    }
                }
                return;
            }
            boolean doPointRedist = false;
            synchronized (points) {
                //do queued add/remove operations
                synchronized (pendingAdds) {
                    if (!pendingAdds.isEmpty()) {
                        for (Point p : pendingAdds) {
                            addPoint(p);
                        }
                        pendingAdds.clear();
                        doPointRedist = true;
                    }
                }
                synchronized (pendingRemovals) {
                    if (!pendingRemovals.isEmpty()) {
                        for (Point p : pendingRemovals) {
                            removePoint(p);
                            p.onDestroy();
                        }
                        pendingRemovals.clear();
                        doPointRedist = true;
                    }
                }
                if (pause) {
                    Utils.releaseCPU();
                    continue;
                }
                //see if some points are colliding
                collisionCouples.clear();
                for (SlaveThread t : slaves) {
                    t.setState(SlaveThread.STATE_DETECT_COLLISIONS);
                }
                sync();
                if (!collisionCouples.isEmpty()) {
                    doPointRedist = true;
                }
                //join colliding points
                handleCollisions();
                //calculate attraction vectors
                for (SlaveThread t : slaves) {
                    t.setState(SlaveThread.STATE_CALCULATE_GRAVITY);
                }
                sync();
                //apply attraction vectors
                for (SlaveThread t : slaves) {
                    t.setState(SlaveThread.STATE_APPLY_GRAVITY);
                }
                sync();
                //if point redist is enabled, and redist may be necessary, balances load by redistributes points across threads to keep them at about the same amount of points
                if (Settings.ENABLE_POINT_REDIST && doPointRedist) {
                    pointRedist();
                }
            }
            onStepComplete(System.nanoTime() - timestamp); //step completed. callback method
            //apply SPS limit
            if (Settings.SPS_LIMIT >= 1) {
                final long minT = 1000000000L / Settings.SPS_LIMIT;
                final long tDiff = System.nanoTime() - timestamp;
                if (tDiff < minT) {
                    Utils.sleep(minT - tDiff);
                }
            }
            //update SPS
            SPS = (int) (1000000000L / (System.nanoTime() - timestamp));
        }
    }

    /**
     * this method analyzes the list of collision couples to detects collsion
     * groups and joins them. a collision group is a bunch of points that are
     * currently colliding (2+ points, of course).<br>
     * detection is done in this way:<br>
     * -take first collision couple and remove it from list<br>
     * -add those 2 points to collision group<br>
     * -for each collision couple, if one of the points in the couple is already
     * in the collision group, add the other one to the collision group and
     * remove the couple from the list<br>
     * -repeat until no new points are added<br>
     * after detection, the points are joined:<br>
     * -all the points in the collision group are removed from all lists<br>
     * -coordinates, mass, velocity and density for the joined point are
     * calculated and assigned to the most massive point in the collision
     * group<br>
     * -the point is added back<br>
     * detection and joining is repeated until the list of collision couples is
     * empty.<br>
     * this may seem complex but it is not run very often, and no, it cannot be
     * done with multiple threads
     *
     */
    private void handleCollisions() {
        while (!collisionCouples.isEmpty()) {
            int nElements = 0;
            final ArrayList<Point> currentCollisionGroup = new ArrayList<Point>();
            final Point[] firstCouple = collisionCouples.get(0);
            collisionCouples.remove(0);
            currentCollisionGroup.add(firstCouple[0]);
            currentCollisionGroup.add(firstCouple[1]);
            do {
                nElements = currentCollisionGroup.size();
                for (int i = 0; i < currentCollisionGroup.size(); i++) {
                    final Point p = currentCollisionGroup.get(i);
                    for (int j = 0; j < collisionCouples.size(); j++) {
                        final Point[] couple = collisionCouples.get(j);
                        if (couple[0] == p) {
                            currentCollisionGroup.add(couple[1]);
                            collisionCouples.remove(j--);
                        } else if (couple[1] == p) {
                            currentCollisionGroup.add(couple[0]);
                            collisionCouples.remove(j--);
                        }
                    }
                }
            } while (nElements != currentCollisionGroup.size() && nElements != 0);
            for (int i = 0; i < currentCollisionGroup.size(); i++) {
                for (int j = 0; j < currentCollisionGroup.size(); j++) {
                    if (i != j && currentCollisionGroup.get(i) == currentCollisionGroup.get(j)) {
                        currentCollisionGroup.remove(j--);
                    }
                }
            }
            double totalMass = 0, newX = 0, newY = 0, newVX = 0, newVY = 0, newDensity = 0;
            Point newP = null;
            for (Point p : currentCollisionGroup) {
                totalMass += p.getMass();
                if (newP == null || p.getMass() > newP.getMass()) {
                    newP = p;
                }
            }
            for (Point p : currentCollisionGroup) {
                final double f = p.getMass() / totalMass;
                newX += p.getX() * f;
                newY += p.getY() * f;
                newVX += p.getVelX() * f;
                newVY += p.getVelY() * f;
                newDensity += p.getDensity() * f;
            }
            newP.setX(newX);
            newP.setY(newY);
            newP.setVelX(newVX);
            newP.setVelY(newVY);
            newP.setMass(totalMass);
            newP.setDensity(newDensity);
            for (Point p : currentCollisionGroup) {
                removePoint(p);
                if (p != newP) {
                    p.onDestroy(); //onDestroy is a callback method
                }
            }
            addPoint(newP);
        }
    }

    /**
     * this method balances load between slave threads<br>
     * balancing is done by redistributing points across threads to keep them at
     * about the same amount of points.
     */
    private void pointRedist() {
        if (points.size() > slaves.length * Settings.POINT_REDIST_MIN_POINTS) {
            int min = slaves[0].threadPoints.size(), max = slaves[0].threadPoints.size();
            for (int i = 1; i < slaves.length; i++) {
                final int s = slaves[i].threadPoints.size();
                if (s < min) {
                    min = s;
                }
                if (s > max) {
                    max = s;
                }
            }
            if (min == 0 || (float) max / (float) min >= Settings.POINT_REDIST_THRESHOLD) {
                for (SlaveThread t : slaves) {
                    t.threadPoints.clear();
                }
                int i = 0;
                for (Point p : points) {
                    slaves[i++ % slaves.length].threadPoints.add(p);
                }
            }
        }
    }

    /**
     * add point and assign it to a slave. this method is NOT thread safe and is
     * only used internally! use add(Point) instead
     *
     * @param p point to add
     */
    private void addPoint(Point p) {
        points.add(p);
        slaves[Settings.ENABLE_POINT_REDIST ? 0 : (int) (slaves.length * Math.random())].add(p);
    }

    /**
     * remove point this method is NOT thread safe and is only used internally!
     * use remove(Point) instead
     *
     * @param p point to remove
     */
    private void removePoint(Point p) {
        points.remove(p);
        for (SlaveThread t : slaves) {
            t.remove(p);
        }
    }

    /**
     * add a Point
     *
     * @param p point to add
     */
    public void add(Point p) {
        synchronized (pendingAdds) {
            pendingAdds.add(p);
        }
    }

    /**
     * remove a Point
     *
     * @param p point to remove
     */
    public void remove(Point p) {
        synchronized (pendingRemovals) {
            pendingRemovals.add(p);
        }
    }

    /**
     * removes all points, clears all lists and runs GC
     */
    public void reset() {
        synchronized (pendingAdds) {
            pendingAdds.clear();
        }
        synchronized (pendingRemovals) {
            pendingRemovals.clear();
        }
        synchronized (points) {
            points.clear();
            for (SlaveThread t : slaves) {
                t.threadPoints.clear();
            }

        }
    }

    /**
     * get a copy of the list of points in the simulation.<br>
     * slows down the simulation if called too often.
     *
     * @return list of points
     */
    public List<Point> getPoints() {
        synchronized (points) {
            return (List<Point>) (points.clone());
        }
    }

    /**
     * get a copy of the list of points in the simulation. Points are
     * cloned.<br>
     * slows down the simulation if called too often.
     *
     * @return saved state
     */
    public Point[] saveState() {
        synchronized (points) {
            Point[] state = new Point[points.size()];
            int i = 0;
            for (Point p : points) {
                state[i++] = p.clone();
            }
            return state;
        }
    }

    /**
     * load a saved state.
     *
     * @param state saved state
     */
    public void loadState(Point[] state) {
        final boolean paused = pause;
        pause = true;
        reset();
        pendingAdds.addAll(Arrays.asList(state));
        pause = paused;
    }

    /**
     *
     * @return Simulations Per Second
     */
    public int getSPS() {
        return SPS;
    }

    /**
     *
     * @return number of points
     */
    public int getNPoints() {
        synchronized (points) {
            return points.size();
        }
    }

    /**
     *
     * @return number of slave threads
     */
    public int getNThreads() {
        return slaves == null ? 0 : slaves.length;
    }

    /**
     * callback method, called after each simulation step
     *
     * @param nanoSeconds time taken to run all calculations
     */
    public void onStepComplete(long nanoSeconds) {

    }

}
