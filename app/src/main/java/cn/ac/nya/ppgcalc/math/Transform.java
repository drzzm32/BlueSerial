package cn.ac.nya.ppgcalc.math;

import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.log;

public class Transform {

    public class WaveData {
        public double max;
        public int pos;
        public double f;
    }

    private int N;
    private Complex[] w;

    private double f;

    static final double pi = Math.PI;

    public Transform(int N, double f) {
        this.N = N;
        this.f = f;

        w = new Complex[N];
        for (int i = 0; i < N; i++) {
            w[i] = new Complex();
            w[i].real = cos(2 * pi / N * i);
            w[i].imag = (-1) * sin(2 * pi / N * i);
        }
    }

    public Complex[] fft(double[] data) {
        Complex[] x = new Complex[N];
        for (int i = 0; i < N; i++) {
            x[i] = new Complex();
            x[i].real = data[i];
            x[i].imag = 0;
        }
        int l;
        Complex up, down, product;
        change(x);
        for (int i = 0; i < log(N) / log(2); i++) {
            l = 1 << i;
            for (int j = 0; j < N; j += 2 * l) {
                for (int k = 0; k < l; k++) {
                    product = x[j + k + l].mul(w[N * k / 2 / l]);
                    up = x[j + k].add(product);
                    down = x[j + k].sub(product);
                    x[j + k] = up;
                    x[j + k + l] = down;
                }
            }
        }
        return x;
    }

    private void change(Complex[] x) {
        Complex temp;
        int i, j, k;
        double t;
        for (i = 0; i < N; i++) {
            k = i;
            j = 0;
            t = (log(N) / log(2));
            while ((t--) > 0) {
                j = j << 1;
                j |= (k & 1);
                k = k >> 1;
            }
            if (j > i) {
                temp = x[i];
                x[i] = x[j];
                x[j] = temp;
            }
        }

    }

    public WaveData output(Complex[] c, double left, double right) {
        int k = 0, XL = 0;
        double max = 0;
        double x[] = new double[N];

        for (int i = 0; i < N; i++) {
            x[i] = c[i].len() * 2 / N;
        }
        x[0] = x[0] / 2;

        left = left * N / f;
        right = right * N / f;

        for (int i = 0; i < N / 2; i++) {
            if (i == 0) continue;
            if (i > left && i < right) {
                if (x[i] > max) {
                    max = x[i];
                    k = i;
                }
            }
        }

        WaveData data = new WaveData();
        data.max = max;
        data.pos = k;
        data.f = k * f / N;
        return data;
    }

}
