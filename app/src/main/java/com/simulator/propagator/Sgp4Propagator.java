package com.simulator.propagator;

import com.simulator.math.Vector3D;
import com.simulator.tle.Tle;

/**
 * SGP4 propagator for near-Earth (LEO) satellites.
 * Includes J2 perturbation and atmospheric drag.
 */
public class Sgp4Propagator extends AdvancedOrbitPropagator {

    public Sgp4Propagator(Tle tle) { super(tle); }

    @Override
    protected double[] computePosition(double minutes) {
        double a = tle.semiMajorAxis;
        double e = tle.eccentricity;
        double i = Math.toRadians(tle.inclination);
        double raan = Math.toRadians(tle.raan);
        double w = Math.toRadians(tle.argumentOfPerigee);

        double n = Math.sqrt(MU / (a*a*a)); // rad/s
        double tSec = minutes * 60.0;

        // J2 secular perturbations
        double p = a * (1 - e*e);
        double raanRate = -1.5 * J2 * Math.pow(RE/p,2) * n * Math.cos(i);
        double wRate = 0.75 * J2 * Math.pow(RE/p,2) * n * (5*Math.pow(Math.cos(i),2) - 1);

        double raanPerturbed = raan + raanRate * tSec;
        double wPerturbed = w + wRate * tSec;

        // Mean anomaly with drag decay
        a -= atmosphericDecay(a, i, e, mass, crossSection, tSec);
        double M = Math.toRadians(tle.meanAnomaly) + n * tSec;

        double E = solveKepler(M, e);
        double theta = 2*Math.atan2(Math.sqrt(1+e)*Math.sin(E/2), Math.sqrt(1-e)*Math.cos(E/2));
        double r = a * (1 - e * Math.cos(E));

        double xPeri = r * Math.cos(theta);
        double yPeri = r * Math.sin(theta);
        double zPeri = 0;

        return perifocalToECI(xPeri, yPeri, zPeri, raanPerturbed, i, wPerturbed);
    }

    @Override
    protected double[] computeVelocity(double minutes) {
        double[] pos = computePosition(minutes);
        double r = Math.sqrt(pos[0]*pos[0] + pos[1]*pos[1] + pos[2]*pos[2]);
        double a = tle.semiMajorAxis;
        double vMag = Math.sqrt(MU*(2/r - 1/a));

        double vx = -vMag*pos[1]/r;
        double vy = vMag*pos[0]/r;
        double vz = 0;

        return new double[]{vx, vy, vz};
    }

    // Drag, Kepler solver, perifocal to ECI (reuse from base)
    private double atmosphericDecay(double semiMajor, double inclination, double ecc, double mass, double area, double tSec) {
        double rho = RHO0 * Math.exp(-(semiMajor - RE)/H_SCALE);
        double Cd = 2.2;
        return 0.5 * Cd * area/mass * rho * Math.sqrt(MU/semiMajor) * tSec;
    }

    private double solveKepler(double M, double e) {
        double E = M;
        for (int j=0; j<15; j++) {
            double delta = E - e*Math.sin(E) - M;
            E -= delta/(1 - e*Math.cos(E));
            if (Math.abs(delta)<1e-8) break;
        }
        return E;
    }

    private double[] perifocalToECI(double x, double y, double z, double raan, double i, double w) {
        double cosOmega = Math.cos(raan), sinOmega = Math.sin(raan);
        double cosI = Math.cos(i), sinI = Math.sin(i);
        double cosw = Math.cos(w), sinw = Math.sin(w);

        double r11 = cosOmega*cosw - sinOmega*sinw*cosI;
        double r12 = -cosOmega*sinw - sinOmega*cosw*cosI;
        double r13 = sinOmega*sinI;
        double r21 = sinOmega*cosw + cosOmega*sinw*cosI;
        double r22 = -sinOmega*sinw + cosOmega*cosw*cosI;
        double r23 = -cosOmega*sinI;
        double r31 = sinw*sinI;
        double r32 = cosw*sinI;
        double r33 = cosI;

        double xe = r11*x + r12*y + r13*z;
        double ye = r21*x + r22*y + r23*z;
        double ze = r31*x + r32*y + r33*z;
        return new double[]{xe, ye, ze};
    }
}
