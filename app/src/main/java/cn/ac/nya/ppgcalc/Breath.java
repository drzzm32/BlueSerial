package cn.ac.nya.ppgcalc;

import java.util.Arrays;

import static java.lang.Math.pow;

public class Breath {

    public class Coefficient {
        public double lambda;
        public double u1;
    }

    int len;

    int[] c;
    double[][] a;
    double[] h;
    double[] m, n, x, p, q, f, l;
    int[] x1, x2_max, x2_min;
    double[] y, v, g;
    double[] e_max, e_min;
    int u_max = 0, u_min = 0;

    public Breath(int[] data) {
        this.len = data.length + 1;
        c = Arrays.copyOf(data, data.length);

        a = new double[len][len];
        h = new double[len];
        m = new double[len]; n = new double[len]; x = new double[len];
        p = new double[len]; q = new double[len]; f = new double[len];
        l = new double[len];
        x1 = new int[len]; x2_max = new int[len]; x2_min = new int[len];
        y = new double[len]; v = new double[len]; g = new double[len];
        e_max = new double[len - 1]; e_min = new double[len - 1];
    }

    private void extreme() {
        int[] s = new int[len - 1];
        int i, k = 1, u = 1;
        e_max[0] = e_min[0] = x2_min[0] = x2_max[0] = 0;
        for (k = 0; k < len - 1; k++) {
            x1[k] = k;
        }
        for (i = 0; i < len - 1; i++) {
            s[i] = c[i];
        }
        /*极大值和极小值*/
        k = 1;
        for (i = 0; i < len - 3; i++) {
            if (s[i + 1] >= s[i] && s[i + 1] > s[i + 2]) {
                e_max[k] = s[i + 1];
                x2_max[k] = x1[i + 1];
                k++;
            }
            u_min = k;
            if (s[i + 1] <= s[i] && s[i + 1] <= s[i + 2]) {
                e_min[u] = s[i + 1];
                x2_min[u] = x1[i + 1];
                u++;
            }
            u_max = u;
        }
        x2_max[u_min] = x1[len - 2];
        e_max[u_min] = s[len - 2];
        x2_min[u_max] = x1[len - 2];
        e_min[u_max] = s[len - 2];
    }

    private void interp() {
        int k, i, j, j1, j2, j3;
        float M;
        Coefficient[] coe = new Coefficient[len - 2];
        /*构建三对角矩阵*/
        for (i = 0; i < u_min + 2; i++) {
            for (j = 0; j < u_min + 2; j++) {
                if (Math.abs(i - j) > 1)
                    a[i][j] = 0;
            }
        }
        for (k = 0; k < u_min; k++) {
            h[k] = x2_max[k + 1] - x2_max[k];
        }
        for (i = 0; i < u_min - 1; i++) {
            coe[i] = new Coefficient();
            coe[i].lambda = h[i + 1] / (h[i] + h[i + 1]);
            coe[i].u1 = 1 - coe[i].lambda;
        }
        for (i = 1; i < u_min; i++) {
            a[i][i - 1] = coe[i - 1].lambda;
            a[i][i] = 2;
            a[i][i + 1] = coe[i - 1].u1;
        }
        a[0][0] = 2;
        a[0][1] = 1;
        a[u_min + 1][u_min - 1] = 1;
        a[u_min][u_min] = 2;
        /*三对角矩阵的Y值*/
        for (k = 0; k < u_min; k++) {
            y[k] = (e_max[k + 1] - e_max[k]) / h[k];
        }
        for (k = 0; k < u_min - 1; k++) {
            v[k] = 3 * (h[k + 1] / (h[k] + h[k + 1]) * y[k] + h[k] / (h[k] + h[k + 1]) * y[k + 1]);
        }
        for (k = 1; k < u_min; k++) {
            a[k][u_min + 1] = v[k - 1];
        }
        a[0][u_min + 1] = 3 * y[0];
        a[u_min][u_min + 1] = 3 * y[u_min - 1];
        /*三对角矩阵计算*/
        p[0] = a[0][1] / a[0][0];
        q[0] = a[0][u_min + 1] / a[0][0];
        for (i = 1; i < u_min + 1; i++) {
            p[i] = a[i][i + 1] / (a[i][i] - a[i][i - 1] * p[i - 1]);
            q[i] = (a[i][u_min + 1] - a[i][i - 1] * q[i - 1]) / (a[i][i] - a[i][i - 1] * p[i - 1]);
        }
        m[u_min] = q[u_min];
        for (i = u_min - 1; i >= 0; i--) {
            m[i] = q[i] - p[i] * m[i + 1];
        }
    }

    public double[] makeHs() {
        int i, t = 0;
        double r = 0;
        double[] hs = new double[(len - 1) * 10];

        extreme();
        interp();

        for (i = 0; i < u_min + 1; i++) {
            for (; r < x2_max[i + 1]; r += 1.0) {
                hs[t++] = (h[i] + 2 * (r - x2_max[i])) * pow((r - x2_max[i + 1]), 2) * e_max[i] / pow(h[i], 3)
                        + (h[i] + 2 * (x2_max[i + 1] - r)) * pow((r - x2_max[i]), 2) * e_max[i + 1] / pow(h[i], 3)
                        + (r - x2_max[i]) * pow((r - x2_max[i + 1]), 2) * m[i] / pow(h[i], 2)
                        + (r - x2_max[i + 1]) * pow((r - x2_max[i]), 2) * m[i + 1] / pow(h[i], 2);
            }
        }

        return hs;
    }

}
