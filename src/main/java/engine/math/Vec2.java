package engine.math;

public record Vec2(float x, float y) {
    public static final Vec2 ZERO = new Vec2(0.0f, 0.0f);
    public static final Vec2 ONE = new Vec2(1.0f, 1.0f);
    public static final Vec2 UNIT_X = new Vec2(1.0f, 0.0f);
    public static final Vec2 UNIT_Y = new Vec2(0.0f, 1.0f);

    public Vec2 add(Vec2 o) {
        return new Vec2(this.x + o.x, this.y + o.y);
    }

    public Vec2 sub(Vec2 o) {
        return new Vec2(this.x - o.x, this.y - o.y);
    }

    public Vec2 mul(float s) {
        return new Vec2(this.x * s, this.y * s);
    }

    public Vec2 div(float s) {
        return new Vec2(this.x / s, this.y / s);
    }

    public Vec2 neg() {
        return new Vec2(-this.x, -this.y);
    }

    public float dot(Vec2 o) {
        return this.x * o.x + this.y * o.y;
    }

    public float lengthSq() {
        return this.x * this.x + this.y * this.y;
    }

    public float length() {
        return (float)Math.sqrt(this.lengthSq());
    }

    public Vec2 normalize() {
        float len = this.length();
        if (len < 1.0E-9f) {
            return ZERO;
        }
        return this.div(len);
    }

    public float distance(Vec2 o) {
        return this.sub(o).length();
    }

    public float[] toArray() {
        return new float[]{this.x, this.y};
    }
}
