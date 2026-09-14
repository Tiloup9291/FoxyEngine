package engine.anim;

import engine.anim.Channel;
import engine.math.Mat4;
import engine.math.Quaternion;
import engine.math.Vec3;
import engine.render.Transform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Node {
    private final String name;
    private final Vec3 basePos;
    private final Vec3 baseEuler;
    private final Vec3 scale;
    private Vec3 curPos;
    private Vec3 curEuler;
    private Node parent;
    private final List<Node> children = new ArrayList<Node>();

    public Node(String name, Vec3 basePos, Vec3 baseEuler, Vec3 scale) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("empty name");
        }
        if (basePos == null || baseEuler == null || scale == null) {
            throw new IllegalArgumentException("null components");
        }
        this.name = name;
        this.basePos = basePos;
        this.baseEuler = baseEuler;
        this.scale = scale;
        this.curPos = basePos;
        this.curEuler = baseEuler;
    }

    public String name() {
        return this.name;
    }

    public Node parent() {
        return this.parent;
    }

    public List<Node> children() {
        return Collections.unmodifiableList(this.children);
    }

    public Vec3 basePos() {
        return this.basePos;
    }

    public Vec3 baseEuler() {
        return this.baseEuler;
    }

    public Vec3 scale() {
        return this.scale;
    }

    public void addChild(Node child) {
        if (child.parent != null) {
            throw new IllegalArgumentException("deja parente : " + child.name);
        }
        child.parent = this;
        this.children.add(child);
    }

    public void resetPose() {
        this.curPos = this.basePos;
        this.curEuler = this.baseEuler;
    }

    public void setChannel(Channel c, float value) {
        switch (c) {
            case POS_X: {
                this.curPos = new Vec3(value, this.curPos.y(), this.curPos.z());
                break;
            }
            case POS_Y: {
                this.curPos = new Vec3(this.curPos.x(), value, this.curPos.z());
                break;
            }
            case POS_Z: {
                this.curPos = new Vec3(this.curPos.x(), this.curPos.y(), value);
                break;
            }
            case ROT_X: {
                this.curEuler = new Vec3(value, this.curEuler.y(), this.curEuler.z());
                break;
            }
            case ROT_Y: {
                this.curEuler = new Vec3(this.curEuler.x(), value, this.curEuler.z());
                break;
            }
            case ROT_Z: {
                this.curEuler = new Vec3(this.curEuler.x(), this.curEuler.y(), value);
            }
        }
    }

    public float getChannel(Channel c) {
        return switch (c) {
            case POS_X -> this.curPos.x();
            case POS_Y -> this.curPos.y();
            case POS_Z -> this.curPos.z();
            case ROT_X -> this.curEuler.x();
            case ROT_Y -> this.curEuler.y();
            case ROT_Z -> this.curEuler.z();
        };
    }

    public Quaternion localRotation() {
        return Quaternion.fromEuler(this.curEuler.x(), this.curEuler.y(), this.curEuler.z());
    }

    public Transform localTransform() {
        return new Transform(this.curPos, this.localRotation(), this.scale);
    }

    public Transform worldTransform() {
        if (this.parent == null) {
            return this.localTransform();
        }
        Transform pw = this.parent.worldTransform();
        Vec3 wp = pw.position().add(pw.rotation().rotate(this.curPos));
        Quaternion wr = pw.rotation().mul(this.localRotation());
        return new Transform(wp, wr, this.scale);
    }

    public Mat4 worldMatrix() {
        Transform w = this.worldTransform();
        return Mat4.translation(w.position()).mul(w.rotation().toMatrix()).mul(Mat4.scaling(w.scale()));
    }
}
