package com.k423.planewar.vfx;

import com.k423.planewar.core.Config;
import com.k423.planewar.core.MathUtils;
import com.k423.planewar.core.Rng;
import com.k423.planewar.gfx.Palette;
import com.k423.planewar.gfx.Renderer;

import java.awt.Color;

/**
 * 粒子系统。固定容量 + 环形游标复用，运行期零分配。
 * 粒子种类：火花、烟、碎片、冲击环、尾焰。
 */
public final class ParticleSystem {

    private static final byte SPARK = 0;
    private static final byte SMOKE = 1;
    private static final byte DEBRIS = 2;
    private static final byte RING = 3;
    private static final byte TRAIL = 4;

    private final int capacity;
    private final double[] px;
    private final double[] py;
    private final double[] vx;
    private final double[] vy;
    private final double[] life;
    private final double[] maxLife;
    private final double[] size;
    private final double[] rotation;
    private final double[] spin;
    private final double[] drag;
    private final int[] color;
    private final byte[] kind;
    private final Rng rng;
    private int cursor;

    public ParticleSystem(Rng rng) {
        this(rng, Config.MAX_PARTICLES);
    }

    public ParticleSystem(Rng rng, int capacity) {
        this.rng = rng;
        this.capacity = capacity;
        px = new double[capacity];
        py = new double[capacity];
        vx = new double[capacity];
        vy = new double[capacity];
        life = new double[capacity];
        maxLife = new double[capacity];
        size = new double[capacity];
        rotation = new double[capacity];
        spin = new double[capacity];
        drag = new double[capacity];
        color = new int[capacity];
        kind = new byte[capacity];
    }

    public int activeCount() {
        int n = 0;
        for (int i = 0; i < capacity; i++) {
            if (life[i] > 0) {
                n++;
            }
        }
        return n;
    }

    public void clear() {
        java.util.Arrays.fill(life, 0);
        cursor = 0;
    }

    private int allocate() {
        // 环形游标：碰到还活着的粒子就覆盖它（最老的先走）
        int idx = cursor;
        cursor = (cursor + 1) % capacity;
        return idx;
    }

    public void spark(double x, double y, int count, Color c, double speed, double lifetime, double pixelSize) {
        for (int i = 0; i < count; i++) {
            int idx = allocate();
            double a = rng.range(0, MathUtils.TAU);
            double v = rng.range(speed * 0.25, speed);
            px[idx] = x;
            py[idx] = y;
            vx[idx] = Math.cos(a) * v;
            vy[idx] = Math.sin(a) * v;
            life[idx] = maxLife[idx] = rng.range(lifetime * 0.5, lifetime);
            size[idx] = rng.range(pixelSize * 0.6, pixelSize * 1.6);
            drag[idx] = 0.90;
            color[idx] = c.getRGB();
            kind[idx] = SPARK;
            rotation[idx] = 0;
            spin[idx] = 0;
        }
    }

    public void smoke(double x, double y, int count, Color c, double spread, double lifetime, double pixelSize) {
        for (int i = 0; i < count; i++) {
            int idx = allocate();
            px[idx] = x + rng.range(-spread, spread);
            py[idx] = y + rng.range(-spread, spread);
            vx[idx] = rng.range(-25, 25);
            vy[idx] = rng.range(-55, -10);
            life[idx] = maxLife[idx] = rng.range(lifetime * 0.6, lifetime);
            size[idx] = rng.range(pixelSize * 0.7, pixelSize * 1.5);
            drag[idx] = 0.96;
            color[idx] = c.getRGB();
            kind[idx] = SMOKE;
            rotation[idx] = 0;
            spin[idx] = 0;
        }
    }

    public void debris(double x, double y, int count, Color c, double speed, double lifetime, double pixelSize) {
        for (int i = 0; i < count; i++) {
            int idx = allocate();
            double a = rng.range(0, MathUtils.TAU);
            double v = rng.range(speed * 0.3, speed);
            px[idx] = x;
            py[idx] = y;
            vx[idx] = Math.cos(a) * v;
            vy[idx] = Math.sin(a) * v;
            life[idx] = maxLife[idx] = rng.range(lifetime * 0.6, lifetime);
            size[idx] = rng.range(pixelSize * 0.8, pixelSize * 2.0);
            drag[idx] = 0.97;
            color[idx] = c.getRGB();
            kind[idx] = DEBRIS;
            rotation[idx] = rng.range(0, MathUtils.TAU);
            spin[idx] = rng.range(-9, 9);
        }
    }

    public void ring(double x, double y, Color c, double startSize, double endSize, double lifetime) {
        int idx = allocate();
        px[idx] = x;
        py[idx] = y;
        vx[idx] = 0;
        vy[idx] = 0;
        life[idx] = maxLife[idx] = lifetime;
        size[idx] = startSize;
        drag[idx] = 0;
        color[idx] = c.getRGB();
        kind[idx] = RING;
        rotation[idx] = endSize; // 借用 rotation 存目标半径
        spin[idx] = 0;
    }

    public void trail(double x, double y, Color c, double pixelSize, double lifetime) {
        int idx = allocate();
        px[idx] = x + rng.range(-2, 2);
        py[idx] = y + rng.range(-2, 2);
        vx[idx] = rng.range(-12, 12);
        vy[idx] = rng.range(20, 70);
        life[idx] = maxLife[idx] = lifetime;
        size[idx] = pixelSize;
        drag[idx] = 0.92;
        color[idx] = c.getRGB();
        kind[idx] = TRAIL;
        rotation[idx] = 0;
        spin[idx] = 0;
    }

    public void update(double dt) {
        for (int i = 0; i < capacity; i++) {
            if (life[i] <= 0) {
                continue;
            }
            life[i] -= dt;
            if (life[i] <= 0) {
                continue;
            }
            double d = drag[i];
            if (d > 0 && d < 1) {
                double f = Math.pow(d, dt * 60);
                vx[i] *= f;
                vy[i] *= f;
            }
            px[i] += vx[i] * dt;
            py[i] += vy[i] * dt;
            rotation[i] += spin[i] * dt;
            if (kind[i] == SMOKE) {
                vy[i] -= 18 * dt;
                size[i] += 26 * dt;
            } else if (kind[i] == DEBRIS) {
                vy[i] += 420 * dt;
            } else if (kind[i] == RING) {
                size[i] += (rotation[i] - size[i]) * Math.min(1, dt * 6);
            }
        }
    }

    public void render(Renderer r) {
        for (int i = 0; i < capacity; i++) {
            double l = life[i];
            if (l <= 0) {
                continue;
            }
            float t = (float) (l / maxLife[i]);
            Color c = new Color(color[i]);
            float a = Math.min(1f, t * 1.6f);
            switch (kind[i]) {
                case SPARK -> r.fillRect(px[i] - size[i] / 2, py[i] - size[i] / 2,
                        Math.max(2, size[i]), Math.max(2, size[i]), c, a);
                case SMOKE -> r.fillRect(px[i] - size[i] / 2, py[i] - size[i] / 2,
                        size[i], size[i], c, a * 0.4f);
                case DEBRIS -> r.fillRect(px[i] - size[i] / 2, py[i] - size[i] / 2,
                        size[i], size[i] * 0.7, c, a);
                case RING -> {
                    double s = size[i];
                    double th = Math.max(1.5, 2.5 * t);
                    r.fillRect(px[i] - s, py[i] - th / 2, s * 2, th, c, a);
                    r.fillRect(px[i] - s, py[i] - s, th, s * 2, c, a);
                    r.fillRect(px[i] + s, py[i] - s, th, s * 2, c, a);
                }
                default -> r.fillRect(px[i] - size[i] / 2, py[i] - size[i] / 2,
                        size[i], size[i], c, a * 0.7f);
            }
        }
    }

    /** 一次小型爆炸：火花 + 烟 + 一圈冲击环。 */
    public void explosion(double x, double y, double scale, Color core) {
        spark(x, y, (int) (12 * scale), core, 320 * scale, 0.42, 2.2 * scale);
        spark(x, y, (int) (8 * scale), Palette.AMBER, 210 * scale, 0.55, 2.6 * scale);
        smoke(x, y, (int) (6 * scale), new Color(50, 55, 70), 10 * scale, 0.9, 9 * scale);
        ring(x, y, Palette.alpha(core, 0.85f), 6 * scale, 62 * scale, 0.42);
    }
}
