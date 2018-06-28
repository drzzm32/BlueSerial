package cn.ac.nya.ppgcalc;

public class SPO2 {

    private double a, b;

    public SPO2() {
        this(0, 0);
    }

    public SPO2(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public double calcSpO2(PPGData data) {
        double max_total = 0, max_num = 0, min_total = 0, min_num = 0;/*初始化数值*/
        int a = 0, b = 0, a1 = 0, b1 = 0;
        double max_660 = 0, min_660 = 0;
        double max_940 = 0, min_940 = 0;
        double z = 0;
        double k = 0, f = 0, m;
        /*660nm*/
        for (int i = 0; i < data.len(); i++)
        {
            a = a1;
            b = b1;
            a1 = data.data.get(i).red;
            if (a1 > a)
                b1 = 1;
            else if (a1 < a)
                b1 = -1;
            if (b1 == -1 && b == 1)
            {
                max_total = max_total + data.data.get(i).red;
                max_num = max_num + 1;
            }
            if (b1 == 1 && b == -1)
            {
                min_total = min_total + data.data.get(i).red;
                min_num = min_num + 1;
            }
        }
        max_660 = max_total / max_num;
        min_660 = min_total / min_num;
        /*940nm*/
        max_total = max_num = min_total = min_num = 0;/*初始化数值*/
        a = b = a1 = b1 = 0;
        for (int i = 0; i < data.len(); i++)
        {
            a = a1;
            b = b1;
            a1 = data.data.get(i).ir;
            if (a1 > a)
                b1 = 1;
            else if (a1 < a)
                b1 = -1;
            if (b1 == -1 && b == 1)
            {
                max_total = max_total + data.data.get(i).ir;
                max_num = max_num + 1;
            }
            if (b1 == 1 && b == -1)
            {
                min_total = min_total + data.data.get(i).ir;
                min_num = min_num + 1;
            }
        }
        max_940 = max_total / max_num;
        min_940 = min_total / min_num;

        k = (max_660 - min_660) / max_660;
        f = (max_940 - min_940) / max_940;
        z = k / f;
	    m = z;

        return m * this.a + this.b;
    }

    public static void calcCoeff(SPO2 SpO2, PPGData[] data) {
        if (data.length == 0) return;

        double max_total = 0, max_num = 0, min_total = 0, min_num = 0;/*初始化数值*/
        int a1 = 0, b1 = 0, a2 = 0, b2 = 0;
        double[] max_660 = new double[data.length], min_660 = new double[data.length];
        double[] max_940 = new double[data.length], min_940 = new double[data.length];
        double sum = 0, sum1 = 0, sum2 = 0, sum3 = 0;
        double[] k = new double[data.length], f = new double[data.length];
        double[] o = new double[data.length];
        /*660nm每个人的数据求Imax和Imin*/
        for (int x = 0; x < data.length; x++) {
            for (int s = 0; s < data[x].len(); s++) {
                a1 = a2;
                b1 = b2;
                a2 = data[x].data.get(s).red;
                if (a2 > a1)
                    b2 = 1;
                else if (a2 < a1)
                    b2 = -1;
                if (b2 == -1 && b1 == 1) {
                    max_total = max_total + data[x].data.get(s).red;
                    max_num = max_num + 1;
                }
                if (b2 == 1 && b1 == -1) {
                    min_total = min_total + data[x].data.get(s).red;
                    min_num = min_num + 1;
                }
            }
            max_660[x] = max_total / max_num;
            min_660[x] = min_total / min_num;
            k[x] = (max_660[x] - min_660[x]) / max_660[x];
            max_total = max_num = min_total = min_num = 0;
        }
        /*940nm每个人的数据求Imax和Imin*/
        max_total = max_num = min_total = min_num = 0;/*再次初始化数值*/
        a1 = b1 = a2 = b2 = 0;
        for (int x = 0; x < data.length; x++) {
            for (int s = 0; s < data[x].len(); s++) {
                a1 = a2;
                b1 = b2;
                a2 = data[x].data.get(s).ir;
                if (a2 > a1)
                    b2 = 1;
                else if (a2 < a1)
                    b2 = -1;
                if (b2 == -1 && b1 == 1) {
                    max_total = max_total + data[x].data.get(s).ir;
                    max_num = max_num + 1;
                }
                if (b2 == 1 && b1 == -1) {
                    min_total = min_total + data[x].data.get(s).ir;
                    min_num = min_num + 1;
                }
            }
            max_940[x] = max_total / max_num;
            min_940[x] = min_total / min_num;
            f[x] = (max_940[x] - min_940[x]) / max_940[x];
            max_total = max_num = min_total = min_num = 0;
        }

        /*最小二乘法求系数的X值*/
        for (int x = 0; x < data.length; x++) {
            o[x] = k[x] / f[x];
        }
        /*朗伯比尔定律系数*/
        for (int x = 0; x < data.length; x++) {
            sum = sum + o[x] * data[x].refSpO2;/*x*y的和*/
            sum1 = sum1 + o[x];/*X的和*/
            sum2 = sum2 + data[x].refSpO2;/*Y的和*/
            sum3 = sum3 + o[x] * o[x];/*X的方的和*/
        }
        SpO2.a = (data.length * sum - sum1 * sum2) / (data.length * sum3 - sum1 * sum1);
        SpO2.b = (sum3 * sum2 - sum1 * sum) / (data.length * sum3 - sum1 * sum1);
    }

}
