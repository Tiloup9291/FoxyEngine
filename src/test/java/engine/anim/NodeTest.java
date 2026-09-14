package engine.anim;

import static org.junit.jupiter.api.Assertions.*;
import engine.math.Quaternion;
import engine.math.Vec3;
import org.junit.jupiter.api.Test;

/** Node hierarchy: pos/rot composition, reset, channels. */
class NodeTest {

    private static Rig chain() {
        Node root = new Node("root", new Vec3(1, 0, 0), Vec3.ZERO, new Vec3(1, 1, 1));
        Node child = new Node("child", new Vec3(0, 2, 0), Vec3.ZERO, new Vec3(0.5f, 0.5f, 0.5f));
        root.addChild(child);
        return new Rig(root);
    }

    @Test
    void translationAccumulates() {
        Rig rig = chain();
        var w = rig.find("child").worldTransform();
        assertEquals(1f, w.position().x(), 1e-5f);
        assertEquals(2f, w.position().y(), 1e-5f);
        assertEquals(0.5f, w.scale().x(), 1e-5f);
    }

    @Test
    void parentRotationMovesChild() {
        Node root = new Node("root", Vec3.ZERO, Vec3.ZERO, new Vec3(1, 1, 1));
        Node child = new Node("child", new Vec3(1, 0, 0), Vec3.ZERO, new Vec3(1, 1, 1));
        root.addChild(child);
        Rig rig = new Rig(root);
        root.setChannel(Channel.ROT_Y, (float) Math.PI / 2f);
        var w = rig.find("child").worldTransform();
        assertEquals(0f, w.position().x(), 1e-5f);
        assertEquals(-1f, w.position().z(), 1e-5f);
    }

    @Test
    void resetPose() {
        Rig rig = chain();
        Node c = rig.find("child");
        c.setChannel(Channel.ROT_X, 1f);
        c.setChannel(Channel.POS_Y, 9f);
        rig.resetPose();
        assertEquals(0f, c.getChannel(Channel.ROT_X), 1e-6f);
        assertEquals(2f, c.getChannel(Channel.POS_Y), 1e-6f);
    }

    @Test
    void duplicateRejected() {
        Node root = new Node("root", Vec3.ZERO, Vec3.ZERO, new Vec3(1, 1, 1));
        root.addChild(new Node("a", Vec3.ZERO, Vec3.ZERO, new Vec3(1, 1, 1)));
        Node other = new Node("a", Vec3.ZERO, Vec3.ZERO, new Vec3(1, 1, 1));
        root.addChild(other);
        assertThrows(IllegalArgumentException.class, () -> new Rig(root));
    }

    @Test
    void worldMatrixConsistent() {
        Rig rig = chain();
        var m = rig.find("child").worldMatrix();
        assertEquals(1f, m.get(0, 3), 1e-5f);
        assertEquals(2f, m.get(1, 3), 1e-5f);
    }

    @Test
    void quaternionIdentity() {
        Quaternion q = Quaternion.IDENTITY;
        Vec3 v = q.rotate(new Vec3(1, 2, 3));
        assertEquals(1f, v.x(), 1e-5f);
        assertEquals(2f, v.y(), 1e-5f);
        assertEquals(3f, v.z(), 1e-5f);
    }
}
