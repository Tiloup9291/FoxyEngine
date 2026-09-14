package engine.anim;

import engine.anim.Channel;
import engine.anim.Clip;
import engine.anim.Keyframe;
import engine.anim.Track;
import java.util.ArrayList;
import java.util.List;

public final class Clips {
    private Clips() {
    }

    public static Clip idle() {
        float a = 0.06f;
        return new Clip("idle", List.of(Clips.track("torso", Channel.POS_Y, Clips.k(0.0f, 0.0f, 0.5f, a * 0.3f, 1.0f, 0.0f)), Clips.track("armL_upper", Channel.ROT_Z, Clips.k(0.0f, a, 0.5f, -a, 1.0f, a)), Clips.track("armR_upper", Channel.ROT_Z, Clips.k(0.0f, -a, 0.5f, a, 1.0f, -a)), Clips.track("head", Channel.ROT_Y, Clips.k(0.0f, 0.0f, 0.5f, 0.08f, 1.0f, 0.0f))), true);
    }

    public static Clip walk() {
        float leg = 0.55f;
        float arm = 0.45f;
        float knee = 0.5f;
        return new Clip("walk", List.of(Clips.track("legL_upper", Channel.ROT_X, Clips.k(0.0f, 0.0f, 0.25f, leg, 0.5f, 0.0f, 0.75f, -leg, 1.0f, 0.0f)), Clips.track("legR_upper", Channel.ROT_X, Clips.k(0.0f, 0.0f, 0.25f, -leg, 0.5f, 0.0f, 0.75f, leg, 1.0f, 0.0f)), Clips.track("legL_lower", Channel.ROT_X, Clips.k(0.0f, 0.0f, 0.25f, 0.0f, 0.5f, knee, 0.75f, 0.0f, 1.0f, 0.0f)), Clips.track("legR_lower", Channel.ROT_X, Clips.k(0.0f, knee, 0.25f, 0.0f, 0.5f, 0.0f, 0.75f, 0.0f, 1.0f, knee)), Clips.track("armL_upper", Channel.ROT_X, Clips.k(0.0f, 0.0f, 0.25f, -arm, 0.5f, 0.0f, 0.75f, arm, 1.0f, 0.0f)), Clips.track("armR_upper", Channel.ROT_X, Clips.k(0.0f, 0.0f, 0.25f, arm, 0.5f, 0.0f, 0.75f, -arm, 1.0f, 0.0f)), Clips.track("torso", Channel.ROT_Y, Clips.k(0.0f, 0.0f, 0.25f, 0.08f, 0.5f, 0.0f, 0.75f, -0.08f, 1.0f, 0.0f)), Clips.track("pelvis", Channel.POS_Y, Clips.k(0.0f, 0.0f, 0.25f, 0.02f, 0.5f, 0.0f, 0.75f, 0.02f, 1.0f, 0.0f))), true);
    }

    public static Clip run() {
        float leg = 0.85f;
        float arm = 0.7f;
        float knee = 0.9f;
        return new Clip("run", List.of(Clips.track("legL_upper", Channel.ROT_X, Clips.k(0.0f, 0.0f, 0.2f, leg, 0.4f, 0.0f, 0.6f, -leg, 0.8f, 0.0f)), Clips.track("legR_upper", Channel.ROT_X, Clips.k(0.0f, 0.0f, 0.2f, -leg, 0.4f, 0.0f, 0.6f, leg, 0.8f, 0.0f)), Clips.track("legL_lower", Channel.ROT_X, Clips.k(0.0f, 0.2f, 0.2f, 0.0f, 0.4f, knee, 0.6f, 0.0f, 0.8f, 0.2f)), Clips.track("legR_lower", Channel.ROT_X, Clips.k(0.0f, knee, 0.2f, 0.0f, 0.4f, 0.2f, 0.6f, 0.0f, 0.8f, knee)), Clips.track("armL_upper", Channel.ROT_X, Clips.k(0.0f, 0.0f, 0.2f, -arm, 0.4f, 0.0f, 0.6f, arm, 0.8f, 0.0f)), Clips.track("armR_upper", Channel.ROT_X, Clips.k(0.0f, 0.0f, 0.2f, arm, 0.4f, 0.0f, 0.6f, -arm, 0.8f, 0.0f)), Clips.track("armL_lower", Channel.ROT_X, Clips.k(0.0f, -0.5f, 0.4f, -0.5f, 0.8f, -0.5f)), Clips.track("armR_lower", Channel.ROT_X, Clips.k(0.0f, -0.5f, 0.4f, -0.5f, 0.8f, -0.5f)), Clips.track("torso", Channel.ROT_X, Clips.k(0.0f, 0.12f, 0.4f, 0.12f, 0.8f, 0.12f)), Clips.track("pelvis", Channel.POS_Y, Clips.k(0.0f, 0.0f, 0.2f, 0.04f, 0.4f, 0.0f, 0.6f, 0.04f, 0.8f, 0.0f))), true);
    }

    public static Clip jump() {
        return new Clip("jump", List.of(Clips.track("pelvis", Channel.POS_Y, Clips.k(0.0f, -0.1f, 0.25f, 0.14f, 0.5f, 0.0f)), Clips.track("legL_upper", Channel.ROT_X, Clips.k(0.0f, -0.5f, 0.25f, 0.35f, 0.5f, 0.0f)), Clips.track("legR_upper", Channel.ROT_X, Clips.k(0.0f, -0.5f, 0.25f, 0.35f, 0.5f, 0.0f)), Clips.track("armL_upper", Channel.ROT_X, Clips.k(0.0f, 0.6f, 0.25f, -0.9f, 0.5f, 0.0f)), Clips.track("armR_upper", Channel.ROT_X, Clips.k(0.0f, 0.6f, 0.25f, -0.9f, 0.5f, 0.0f))), false);
    }

    private static Track track(String node, Channel c, List<Keyframe> keys) {
        return new Track(node, c, keys);
    }

    private static List<Keyframe> k(float ... tv) {
        if (tv.length % 2 != 0) {
            throw new IllegalArgumentException("time/value pairs");
        }
        ArrayList<Keyframe> out = new ArrayList<Keyframe>(tv.length / 2);
        for (int i = 0; i < tv.length; i += 2) {
            out.add(new Keyframe(tv[i], tv[i + 1]));
        }
        return out;
    }
}
