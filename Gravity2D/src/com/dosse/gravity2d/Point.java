/*
 * GNU LGPLv3
 */
package com.dosse.gravity2d;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A Point in the gravity simulation.<br>
 * Has x,y, velocity vector, mass and density.<br>
 * <br>
 * Also responsible for attraction calculations and collision detection
 *
 *
 * @author Federico
 */
public class Point implements Externalizable {

    /**
     * coordinates, velocity vector, mass and density
     */
    private double x, y, velX, velY, mass, density;

    /**
     * radius=sqrt(PI*mass/density) recalculated after each mass/density change
     */
    private double radius;

    /**
     * create a new point
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param velX velocity on x
     * @param velY velocity on y
     * @param mass mass >0
     * @param density density >0
     */
    public Point(double x, double y, double velX, double velY, double mass, double density) {
        if (mass <= 0) {
            throw new IllegalArgumentException("Mass must be >0");
        }
        if (density <= 0) {
            throw new IllegalArgumentException("Density must be >0");
        }
        this.x = x;
        this.y = y;
        this.velX = velX;
        this.velY = velY;
        this.mass = mass;
        this.density = density;
        this.mass = mass;
        recalcRadius();
    }

    public Point() {
        this(0, 0, 0, 0, 1, 1);
    }

    protected void recalcRadius() {
        radius = density == 0 ? 0 : Math.sqrt(Math.PI * mass / density);
    }

    /**
     *
     * @return density
     */
    public double getDensity() {
        return density;
    }

    /**
     * note:radius will be recalculated
     *
     * @param density density >0
     */
    public void setDensity(double density) {
        if (density <= 0) {
            throw new IllegalArgumentException("Density must be >0");
        }
        this.density = density;
        recalcRadius();
    }

    /**
     *
     * @return mass
     */
    public double getMass() {
        return mass;
    }

    /**
     * note: radius will be recalculated
     *
     * @param mass mass
     */
    public void setMass(double mass) {
        if (mass <= 0) {
            throw new IllegalArgumentException("Mass must be >0");
        }
        this.mass = mass;
        recalcRadius();
    }

    /**
     *
     * @return x coordinate
     */
    public double getX() {
        return x;
    }

    /**
     *
     * @return y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     *
     * @param x x coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     *
     * @param y y coordinate
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     *
     * @param velX velocity on x
     */
    public void setVelX(double velX) {
        this.velX = velX;
    }

    /**
     *
     * @param velY velocity on y
     */
    public void setVelY(double velY) {
        this.velY = velY;
    }

    /**
     *
     * @return velocity on x
     */
    public double getVelX() {
        return velX;
    }

    /**
     *
     * @return velocity on y
     */
    public double getVelY() {
        return velY;
    }

    /**
     * attraction vector, calculated by calculateAttraction(points)
     */
    private double dx, dy;

    /**
     * this method calculates attraction between this point and all the points
     * in the list, and stores it in the attraction vector of this point.<br>
     * in other words, it calculates how much the other points attract this
     * one<br>
     *
     * @param points points
     */
    protected void calculateAttraction(Iterable<Point> points) {
        double distance, diffX, diffY, distance3;
        dx = 0;
        dy = 0;
        for (Point p : points) {
            if (p == this) {
                continue;
            }
            diffX = p.x - x;
            diffY = p.y - y;
            distance = Math.sqrt(diffX * diffX + diffY * diffY);
            distance3 = distance * distance * distance;
            dx += (p.mass * diffX) / distance3;
            dy += (p.mass * diffY) / distance3;
        }
        dx *= Settings.G;
        dy *= Settings.G;
    }

    /**
     * applies the calculated attraction vector
     */
    protected void applyAttractionVector() {
        velX += dx;
        velY += dy;
        x += velX;
        y += velY;
    }

    /**
     * does this point collide with p?<br>
     * Implementation is a bit lousy, as it only detects a hit if the 2 points
     * are overlapping (in other words, if they're small and fast, they may pass
     * into each other)
     *
     * @param p other point
     * @return true if colliding, false otherwise
     */
    public boolean collidesWith(Point p) {
        final double diffX = p.x - x, diffY = p.y - y, rads = radius + p.radius;
        return diffX * diffX + diffY * diffY <= rads * rads;
    }

    /**
     * get radius of this point<br>
     * radius=sqrt(PI*mass/density)
     *
     * @return
     */
    public double getRadius() {
        return radius;
    }

    /**
     * callback method called when Simulation destroys a point after a collision
     */
    public void onDestroy() {

    }

    /**
     * clones this point
     *
     * @return a clone of this point
     */
    @Override
    public Point clone() {
        return new Point(x, y, velX, velY, mass, density);
    }

    /**
     *
     * @return hash
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.x) ^ (Double.doubleToLongBits(this.x) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.y) ^ (Double.doubleToLongBits(this.y) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.velX) ^ (Double.doubleToLongBits(this.velX) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.velY) ^ (Double.doubleToLongBits(this.velY) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.mass) ^ (Double.doubleToLongBits(this.mass) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.density) ^ (Double.doubleToLongBits(this.density) >>> 32));
        return hash;
    }

    /**
     *
     * @param o an Object
     * @return if o isn't a Point, it returns false, otherwise it returns true
     * if x,y,velocity, mass and density are exactly the same
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Point) {
            final Point p = (Point) o;
            return p.x == x && p.y == y && p.velX == velX && p.velY == velY && p.mass == mass && p.density == density;
        } else {
            return false;
        }
    }

    /**
     * custom serialization code
     *
     * @param out stream
     * @throws IOException in case of stream errors
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeChar('d'); //this save contains double-precision data
        out.writeDouble(x);
        out.writeDouble(y);
        out.writeDouble(velX);
        out.writeDouble(velY);
        out.writeDouble(mass);
        out.writeDouble(density);
    }

    /**
     * custom deserialization code
     *
     * @param in stream
     * @throws IOException in case of stream errors
     * @throws ClassNotFoundException never
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        boolean doublePrecision=in.readChar()=='d';
        x = doublePrecision?in.readDouble():in.readFloat();
        y = doublePrecision?in.readDouble():in.readFloat();
        velX = doublePrecision?in.readDouble():in.readFloat();
        velY = doublePrecision?in.readDouble():in.readFloat();
        mass = doublePrecision?in.readDouble():in.readFloat();
        density = doublePrecision?in.readDouble():in.readFloat();
        recalcRadius();
    }

    private static final long serialVersionUID = 2;

}
