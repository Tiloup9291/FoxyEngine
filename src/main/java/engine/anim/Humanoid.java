package engine.anim;

import engine.anim.Node;
import engine.anim.Rig;
import engine.math.Quaternion;
import engine.math.Vec3;
import engine.render.Material;
import engine.render.Mesh;
import engine.render.Renderer;
import engine.render.Transform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Humanoid {
    public static final int TORSO_COLOR = 5088255;
    public static final int HEAD_COLOR = 16765286;
    public static final int LIMB_COLOR = 7268279;
    public static final int JOINT_COLOR = 15681391;
    private static final Map<Integer, Mesh> CUBE_CACHE = new HashMap<Integer, Mesh>();

    private Humanoid() {
    }

    public static synchronized Mesh cubeOf(int rgb) {
        return CUBE_CACHE.computeIfAbsent(rgb & 0xFFFFFF, c -> Mesh.createCube(1.0f, new Material("part", (int)c, 1.0f)));
    }

    private static Vec3 v(float x, float y, float z) {
        return new Vec3(x, y, z);
    }

    private static Vec3 e() {
        return Vec3.ZERO;
    }

    public static Rig create() {
        Node pelvis = new Node("pelvis", Humanoid.v(0.0f, 0.05f, 0.0f), Humanoid.e(), Humanoid.v(0.3f, 0.18f, 0.2f));
        Node torso = new Node("torso", Humanoid.v(0.0f, 0.22f, 0.0f), Humanoid.e(), Humanoid.v(0.36f, 0.3f, 0.22f));
        Node head = new Node("head", Humanoid.v(0.0f, 0.28f, 0.0f), Humanoid.e(), Humanoid.v(0.2f, 0.2f, 0.2f));
        pelvis.addChild(torso);
        torso.addChild(head);
        Humanoid.limb(torso, "armL", Humanoid.v(-0.24f, 0.1f, 0.0f), Humanoid.v(0.1f, 0.24f, 0.1f), Humanoid.v(0.09f, 0.22f, 0.09f));
        Humanoid.limb(torso, "armR", Humanoid.v(0.24f, 0.1f, 0.0f), Humanoid.v(0.1f, 0.24f, 0.1f), Humanoid.v(0.09f, 0.22f, 0.09f));
        Humanoid.limb(pelvis, "legL", Humanoid.v(-0.1f, -0.12f, 0.0f), Humanoid.v(0.12f, 0.26f, 0.12f), Humanoid.v(0.11f, 0.26f, 0.11f));
        Humanoid.limb(pelvis, "legR", Humanoid.v(0.1f, -0.12f, 0.0f), Humanoid.v(0.12f, 0.26f, 0.12f), Humanoid.v(0.11f, 0.26f, 0.11f));
        return new Rig(pelvis);
    }

    private static void limb(Node attach, String prefix, Vec3 pivot, Vec3 upperSize, Vec3 lowerSize) {
        Node upper = new Node(prefix + "_upper", pivot, Humanoid.e(), upperSize);
        Node lower = new Node(prefix + "_lower", Humanoid.v(0.0f, -upperSize.y() / 2.0f - lowerSize.y() / 2.0f - 0.02f, 0.0f), Humanoid.e(), lowerSize);
        attach.addChild(upper);
        upper.addChild(lower);
    }

    public static int colorOf(String nodeName) {
        if (nodeName.equals("pelvis") || nodeName.equals("torso")) {
            return 5088255;
        }
        if (nodeName.equals("head")) {
            return 16765286;
        }
        if (nodeName.endsWith("_lower")) {
            return 15681391;
        }
        return 7268279;
    }

    public static List<Renderer.Instance> toInstances(Rig rig, Transform rootWorld) {
        ArrayList<Renderer.Instance> out = new ArrayList<Renderer.Instance>(rig.nodeCount());
        for (Node n : rig.nodes()) {
            Transform w = n.worldTransform();
            Vec3 pos = rootWorld.position().add(rootWorld.rotation().rotate(w.position()));
            Quaternion rot = rootWorld.rotation().mul(w.rotation());
            out.add(new Renderer.Instance(Humanoid.cubeOf(Humanoid.colorOf(n.name())), new Transform(pos, rot, w.scale())));
        }
        return out;
    }
}
