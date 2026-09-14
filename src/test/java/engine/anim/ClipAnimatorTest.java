package engine.anim;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Track/Clip (sampling, loop), Animator (fade), FSM. */
class ClipAnimatorTest {

    private static Rig rig1() {
        return new Rig(new Node("a", engine.math.Vec3.ZERO,
                engine.math.Vec3.ZERO, new engine.math.Vec3(1, 1, 1)));
    }

    @Test
    void sampleLinearAndClamp() {
        Track t = new Track("a", Channel.ROT_X, List.of(
                new Keyframe(0f, 0f), new Keyframe(1f, 2f)));
        assertEquals(0f, t.sample(-1f), 1e-6f);
        assertEquals(1f, t.sample(0.5f), 1e-6f);
        assertEquals(2f, t.sample(5f), 1e-6f);
    }

    @Test
    void clipLoops() {
        Clip c = new Clip("c", List.of(new Track("a", Channel.ROT_X, List.of(
                new Keyframe(0f, 0f), new Keyframe(1f, 1f)))), true);
        Rig rig = rig1();
        c.apply(rig, 1.5f);
        assertEquals(0.5f, rig.find("a").getChannel(Channel.ROT_X), 1e-5f);
    }

    @Test
    void clipNoLoopClamps() {
        Clip c = new Clip("c", List.of(new Track("a", Channel.ROT_X, List.of(
                new Keyframe(0f, 0f), new Keyframe(1f, 1f)))), false);
        Rig rig = rig1();
        c.apply(rig, 5f);
        assertEquals(1f, rig.find("a").getChannel(Channel.ROT_X), 1e-6f);
    }

    @Test
    void animatorFade() {
        Rig rig = rig1();
        Clip idle = new Clip("idle", List.of(new Track("a", Channel.ROT_X, List.of(
                new Keyframe(0f, 0f), new Keyframe(1f, 0f)))), true);
        Clip walk = new Clip("walk", List.of(new Track("a", Channel.ROT_X, List.of(
                new Keyframe(0f, 2f), new Keyframe(1f, 2f)))), true);
        Animator an = new Animator(rig, Map.of(
                Animator.State.IDLE, idle, Animator.State.WALK, walk));
        an.setFadeDuration(0.2f);
        an.update(0.016f);
        assertEquals(0f, rig.find("a").getChannel(Channel.ROT_X), 1e-5f);
        an.play(Animator.State.WALK);
        assertTrue(an.fading());
        an.update(0.1f);
        float mid = rig.find("a").getChannel(Channel.ROT_X);
        assertTrue(mid > 0.5f && mid < 1.5f, "mid-fade=" + mid);
        an.update(0.2f);
        assertFalse(an.fading());
        assertEquals(2f, rig.find("a").getChannel(Channel.ROT_X), 1e-5f);
    }

    @Test
    void fsmAndSpeedScale() {
        Rig rig = rig1();
        Clip idle = new Clip("idle", List.of(new Track("a", Channel.ROT_X, List.of(
                new Keyframe(0f, 0f), new Keyframe(1f, 0f)))), true);
        Clip walk = new Clip("walk", List.of(new Track("a", Channel.ROT_X, List.of(
                new Keyframe(0f, 0f), new Keyframe(1f, 1f)))), true);
        Clip run = new Clip("run", List.of(new Track("a", Channel.ROT_X, List.of(
                new Keyframe(0f, 0f), new Keyframe(1f, 2f)))), true);
        Clip jump = new Clip("jump", List.of(new Track("a", Channel.ROT_X, List.of(
                new Keyframe(0f, 3f), new Keyframe(0.5f, 3f)))), false);
        Animator an = new Animator(rig, Map.of(
                Animator.State.IDLE, idle, Animator.State.WALK, walk,
                Animator.State.RUN, run, Animator.State.JUMP, jump));
        AnimStateMachine fsm = new AnimStateMachine(an, 2f, 5f);
        assertEquals(Animator.State.IDLE, fsm.update(0f, true, false));
        assertEquals(Animator.State.WALK, fsm.update(2f, true, false));
        assertEquals(1f, an.speedScale(), 1e-5f);
        assertEquals(Animator.State.RUN, fsm.update(6f, true, false));
        assertEquals(3f, an.speedScale(), 1e-5f);
        assertEquals(Animator.State.JUMP, fsm.update(0f, false, true));
        assertEquals(Animator.State.IDLE, fsm.update(0f, true, false));
    }

    @Test
    void humanoidComplete() {
        Rig rig = Humanoid.create();
        assertEquals(11, rig.nodeCount());
        List<engine.render.Renderer.Instance> inst =
                Humanoid.toInstances(rig, engine.render.Transform.IDENTITY);
        assertEquals(11, inst.size());
        engine.math.Vec3 head = null, torso = null;
        for (var n : rig.nodes()) {
            var w = n.worldTransform();
            if (n.name().equals("head")) head = w.position();
            if (n.name().equals("torso")) torso = w.position();
        }
        assertTrue(head.y() > torso.y());
    }
}
