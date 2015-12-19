/*
 * GNU LGPLv3
 */
package com.dosse.gravity2d.demo;

import com.dosse.gravity2d.Point;
import com.dosse.gravity2d.Simulation;

/**
 *
 * @author Federico
 */
public class Demo extends Simulation {

    public Point pointAt(double x, double y) {
        for (Point p : points) {
            final double dx = x - p.getX(), dy = y - p.getY();
            if (Math.sqrt(dx * dx + dy * dy) <= p.getRadius()) {
                return p;
            }
        }
        return null;
    }

    public void createAt(double x, double y, double initSpeedX, double initSpeedY, double mass, double density) {
        add(new Point(x, y, initSpeedX, initSpeedY, mass, density));
    }

    public void removeAt(double x, double y) {
        final Point p = pointAt(x, y);
        if (p != null) {
            remove(p);
        }
    }

    public double[][] getPlotData() {
        boolean tryAgain = false;
        do {
            try {
                final double[][] plotData = new double[points.size()][4];
                int i = 0;
                for (Point p : points) {
                    plotData[i][0] = p.getX();
                    plotData[i][1] = p.getY();
                    plotData[i][2] = p.getRadius();
                    plotData[i][3] = p.getDensity();
                    i++;
                }
                return plotData;
            } catch (Throwable e) {
                //I did not synchronize access to the points list for performance reasons. In the rare event that a sync problem arises, ignore it
                tryAgain = true;
            }
        } while (tryAgain);
        return null; //unreachable, but java complained
    }

}
