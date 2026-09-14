package engine.anim;

import engine.anim.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Rig {
    private final Node root;
    private final Map<String, Node> byName = new LinkedHashMap<String, Node>();

    public Rig(Node root) {
        if (root == null) {
            throw new IllegalArgumentException("null root");
        }
        if (root.parent() != null) {
            throw new IllegalArgumentException("root already parented");
        }
        this.root = root;
        this.index(root);
    }

    private void index(Node n) {
        if (this.byName.put(n.name(), n) != null) {
            throw new IllegalArgumentException("duplicate: " + n.name());
        }
        for (Node c : n.children()) {
            this.index(c);
        }
    }

    public Node root() {
        return this.root;
    }

    public Node find(String name) {
        return this.byName.get(name);
    }

    public List<Node> nodes() {
        return Collections.unmodifiableList(new ArrayList<Node>(this.byName.values()));
    }

    public int nodeCount() {
        return this.byName.size();
    }

    public void resetPose() {
        for (Node n : this.byName.values()) {
            n.resetPose();
        }
    }
}
