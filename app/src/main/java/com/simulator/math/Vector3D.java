package com.simulator.math;

import java.util.Objects;

public class Vector3D {
    public double x;
    public double y;
    public double z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // ---------------- Basic Operations ----------------

    /** Vector addition */
    public Vector3D add(Vector3D other) {
        return new Vector3D(x + other.x, y + other.y, z + other.z);
    }

    /** Vector subtraction */
    public Vector3D subtract(Vector3D other) {
        return new Vector3D(x - other.x, y - other.y, z - other.z);
    }

    /** Scalar multiplication */
    public Vector3D scale(double factor) {
        return new Vector3D(x * factor, y * factor, z * factor);
    }

    /** Euclidean norm (magnitude) */
    public double norm() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /** Alias for norm() — used for compatibility with visualizer */
    public double magnitude() {
        return norm();
    }

    /** Distance to another vector */
    public double distance(Vector3D other) {
        return this.subtract(other).norm();
    }

    /** Returns a normalized (unit) vector */
    public Vector3D normalize() {
        double n = norm();
        if (n == 0) throw new ArithmeticException("Cannot normalize zero vector");
        return scale(1.0 / n);
    }

    // ---------------- Advanced Operations ----------------

    /** Dot product */
    public double dot(Vector3D other) {
        return x * other.x + y * other.y + z * other.z;
    }

    /** Cross product */
    public Vector3D cross(Vector3D other) {
        return new Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }

    /**
     * Rotate this vector around a given axis by an angle in radians.
     * Uses Rodrigues' rotation formula.
     */
    public Vector3D rotate(Vector3D axis, double angleRad) {
        Vector3D u = axis.normalize();
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        return this.scale(cos)
            .add(u.cross(this).scale(sin))
            .add(u.scale(u.dot(this) * (1 - cos)));
    }

    // ---------------- Object Overrides ----------------

    @Override
    public String toString() {
        return String.format("[%.6f, %.6f, %.6f]", x, y, z);
    }

    /** Shorter string representation (rounded to 2 decimals) */
    public String toShortString() {
        return String.format("[%.2f, %.2f, %.2f]", x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vector3D)) return false;
        Vector3D other = (Vector3D) obj;
        return Double.compare(x, other.x) == 0 &&
               Double.compare(y, other.y) == 0 &&
               Double.compare(z, other.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    // ---------------- Static Helpers ----------------

    /** Zero vector */
    public static Vector3D zero() {
        return new Vector3D(0, 0, 0);
    }

    /** Unit vectors along principal axes */
    public static final Vector3D X_UNIT = new Vector3D(1, 0, 0);
    public static final Vector3D Y_UNIT = new Vector3D(0, 1, 0);
    public static final Vector3D Z_UNIT = new Vector3D(0, 0, 1);
}
