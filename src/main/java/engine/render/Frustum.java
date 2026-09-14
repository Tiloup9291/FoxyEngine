package engine.render;

import engine.math.AABB;
import engine.math.Mat4;
import engine.math.Vec3;

public final class Frustum {
    private final float[][] planes;

    private Frustum(float[][] planes) {
        this.planes = planes;
    }

    public static Frustum fromViewProjection(Mat4 vp) {
        float[] row0 = new float[]{vp.get(0, 0), vp.get(0, 1), vp.get(0, 2), vp.get(0, 3)};
        float[] row1 = new float[]{vp.get(1, 0), vp.get(1, 1), vp.get(1, 2), vp.get(1, 3)};
        float[] row2 = new float[]{vp.get(2, 0), vp.get(2, 1), vp.get(2, 2), vp.get(2, 3)};
        float[] row3 = new float[]{vp.get(3, 0), vp.get(3, 1), vp.get(3, 2), vp.get(3, 3)};
        float[][] p = new float[6][4];
        p[0] = Frustum.add(row3, row0);
        p[1] = Frustum.sub(row3, row0);
        p[2] = Frustum.add(row3, row1);
        p[3] = Frustum.sub(row3, row1);
        p[4] = Frustum.add(row3, row2);
        p[5] = Frustum.sub(row3, row2);
        for (int i = 0; i < 6; ++i) {
            Frustum.normalize(p[i]);
        }
        return new Frustum(p);
    }

    private static float[] add(float[] a, float[] b) {
        return new float[]{a[0] + b[0], a[1] + b[1], a[2] + b[2], a[3] + b[3]};
    }

    private static float[] sub(float[] a, float[] b) {
        return new float[]{a[0] - b[0], a[1] - b[1], a[2] - b[2], a[3] - b[3]};
    }

    private static void normalize(float[] p) {
        float len = (float)Math.sqrt(p[0] * p[0] + p[1] * p[1] + p[2] * p[2]);
        if (len < 1.0E-9f) {
            return;
        }
        p[0] = p[0] / len;
        p[1] = p[1] / len;
        p[2] = p[2] / len;
        p[3] = p[3] / len;
    }

    private float distance(int plane, float x, float y, float z) {
        float[] p = this.planes[plane];
        return p[0] * x + p[1] * y + p[2] * z + p[3];
    }

    public boolean isVisible(Vec3 point) {
        for (int i = 0; i < 6; ++i) {
            if (!(this.distance(i, point.x(), point.y(), point.z()) < 0.0f)) continue;
            return false;
        }
        return true;
    }

    public boolean isVisible(AABB box) {
        float[] xs = new float[]{box.min().x(), box.max().x()};
        float[] ys = new float[]{box.min().y(), box.max().y()};
        float[] zs = new float[]{box.min().z(), box.max().z()};
        for (int i = 0; i < 6; ++i) {
            boolean anyInside = false;
            for (float x : xs) {
                block2: for (float y : ys) {
                    for (float z : zs) {
                        if (!(this.distance(i, x, y, z) >= 0.0f)) continue;
                        anyInside = true;
                        continue block2;
                    }
                }
            }
            if (anyInside) continue;
            return false;
        }
        return true;
    }

    public static AABB transformAABB(AABB box, Mat4 m) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        float[] xs = new float[]{box.min().x(), box.max().x()};
        float[] ys = new float[]{box.min().y(), box.max().y()};
        float[] zs = new float[]{box.min().z(), box.max().z()};
        for (float x : xs) {
            for (float y : ys) {
                for (float z : zs) {
                    Vec3 t = m.transformPoint(new Vec3(x, y, z));
                    if (t.x() < minX) {
                        minX = t.x();
                    }
                    if (t.y() < minY) {
                        minY = t.y();
                    }
                    if (t.z() < minZ) {
                        minZ = t.z();
                    }
                    if (t.x() > maxX) {
                        maxX = t.x();
                    }
                    if (t.y() > maxY) {
                        maxY = t.y();
                    }
                    if (!(t.z() > maxZ)) continue;
                    maxZ = t.z();
                }
            }
        }
        return new AABB(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ));
    }
}
