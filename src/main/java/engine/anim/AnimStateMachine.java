package engine.anim;

import engine.anim.Animator;

public final class AnimStateMachine {
    private final Animator animator;
    private final float walkRefSpeed;
    private final float runThreshold;

    public Animator animator() {
        return this.animator;
    }

    public AnimStateMachine(Animator animator, float walkRefSpeed, float runThreshold) {
        if (animator == null) {
            throw new IllegalArgumentException("null animator");
        }
        if (walkRefSpeed <= 0.0f) {
            throw new IllegalArgumentException("walkRef <= 0");
        }
        this.animator = animator;
        this.walkRefSpeed = walkRefSpeed;
        this.runThreshold = runThreshold;
    }

    public Animator.State update(float speed, boolean onGround, boolean jumping) {
        if (!onGround || jumping) {
            this.animator.setSpeedScale(1.0f);
            this.animator.play(Animator.State.JUMP);
        } else if (speed < 0.3f) {
            this.animator.setSpeedScale(1.0f);
            this.animator.play(Animator.State.IDLE);
        } else if (speed < this.runThreshold) {
            this.animator.setSpeedScale(speed / this.walkRefSpeed);
            this.animator.play(Animator.State.WALK);
        } else {
            this.animator.setSpeedScale(speed / this.walkRefSpeed);
            this.animator.play(Animator.State.RUN);
        }
        return this.animator.state();
    }
}
