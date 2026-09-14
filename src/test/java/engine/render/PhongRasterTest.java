/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  engine.math.Vec3
 *  engine.render.Clipper
 *  engine.render.Clipper$Vertex
 *  engine.render.Framebuffer
 *  engine.render.Light
 *  engine.render.Material
 *  engine.render.PhongRaster
 *  engine.render.Texture
 *  engine.render.Texture$Filter
 *  org.junit.jupiter.api.Assertions
 *  org.junit.jupiter.api.Test
 */
package engine.render;

import engine.math.Vec3;
import engine.render.Clipper;
import engine.render.Framebuffer;
import engine.render.Light;
import engine.render.Material;
import engine.render.PhongRaster;
import engine.render.Texture;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PhongRasterTest {
    PhongRasterTest() {
    }

    private static Material mat() {
        return new Material("m", 0xFFFFFF, 1.0f, new Vec3(0.2f, 0.2f, 0.2f), new Vec3(0.0f, 0.0f, 0.0f), 0.0f, null);
    }

    private static float[] attr(float nx, float ny, float nz, float u, float v) {
        return new float[]{nx, ny, nz, 0.0f, 0.0f, -5.0f, u, v};
    }

    @Test
    void trianglePleinEclaire() {
        Framebuffer fb = new Framebuffer(40, 40);
        fb.clear(0);
        Light sun = new Light(Vec3.UNIT_Z, new Vec3(1.0f, 1.0f, 1.0f), 1.0f);
        Material m = PhongRasterTest.mat();
        PhongRaster.drawTriangle((Framebuffer)fb, (float)5.0f, (float)30.0f, (float)5.0f, (float)0.5f, (float[])PhongRasterTest.attr(0.0f, 0.0f, 1.0f, 0.0f, 0.0f), (float)35.0f, (float)30.0f, (float)5.0f, (float)0.5f, (float[])PhongRasterTest.attr(0.0f, 0.0f, 1.0f, 1.0f, 0.0f), (float)20.0f, (float)5.0f, (float)5.0f, (float)0.5f, (float[])PhongRasterTest.attr(0.0f, 0.0f, 1.0f, 0.5f, 1.0f), (Material)m, null, (Texture.Filter)Texture.Filter.NEAREST, (Light)sun, (float)0.25f, (float)0.0f, (float)0.0f, (float)10.0f);
        long painted = 0L;
        for (int px : fb.pixels()) {
            if ((px & 0xFFFFFF) == 0) continue;
            ++painted;
        }
        Assertions.assertTrue((painted > 100L ? 1 : 0) != 0, (String)("triangle Phong dessine, mesures=" + painted));
    }

    @Test
    void textureModulKd() {
        Framebuffer fb = new Framebuffer(40, 40);
        fb.clear(0);
        Texture tex = new Texture(1, 1, new int[]{-65536});
        Material m = new Material("m", 0xFFFFFF, 1.0f, new Vec3(1.0f, 1.0f, 1.0f), new Vec3(0.0f, 0.0f, 0.0f), 0.0f, "r.png");
        Light sun = new Light(Vec3.UNIT_Z, new Vec3(1.0f, 1.0f, 1.0f), 0.0f);
        PhongRaster.drawTriangle((Framebuffer)fb, (float)5.0f, (float)30.0f, (float)5.0f, (float)0.5f, (float[])PhongRasterTest.attr(0.0f, 0.0f, 1.0f, 0.0f, 0.0f), (float)35.0f, (float)30.0f, (float)5.0f, (float)0.5f, (float[])PhongRasterTest.attr(0.0f, 0.0f, 1.0f, 1.0f, 0.0f), (float)20.0f, (float)5.0f, (float)5.0f, (float)0.5f, (float[])PhongRasterTest.attr(0.0f, 0.0f, 1.0f, 0.5f, 1.0f), (Material)m, (Texture)tex, (Texture.Filter)Texture.Filter.NEAREST, (Light)sun, (float)1.0f, (float)0.0f, (float)0.0f, (float)10.0f);
        int px = fb.getPixel(20, 20);
        Assertions.assertEquals((int)0xFF0000, (int)px, () -> String.format("attendu rouge, recu 0x%06X", px));
    }

    @Test
    void clipAttrConserveUv() {
        Clipper.Vertex a = new Clipper.Vertex(0.0f, 0.0f, -5.0f, new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, -5.0f, 0.0f, 0.0f});
        Clipper.Vertex b = new Clipper.Vertex(2.0f, 0.0f, -5.0f, new float[]{0.0f, 0.0f, 1.0f, 2.0f, 0.0f, -5.0f, 1.0f, 0.0f});
        Clipper.Vertex c = new Clipper.Vertex(0.0f, 0.0f, 5.0f, new float[]{0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 5.0f, 0.0f, 1.0f});
        List tris = Clipper.clipTriangleAttr((Clipper.Vertex)a, (Clipper.Vertex)b, (Clipper.Vertex)c, (float)0.1f);
        Assertions.assertEquals((int)2, (int)tris.size(), (String)"1 sommet derriere -> quad -> 2 triangles");
        Iterator iterator = tris.iterator();
        while (iterator.hasNext()) {
            Clipper.Vertex[] tri;
            for (Clipper.Vertex vtx : tri = (Clipper.Vertex[])iterator.next()) {
                Assertions.assertTrue((vtx.z <= -0.09999f ? 1 : 0) != 0);
                Assertions.assertEquals((int)8, (int)vtx.attr.length, (String)"attributs interpoles conserves");
            }
        }
    }
}
