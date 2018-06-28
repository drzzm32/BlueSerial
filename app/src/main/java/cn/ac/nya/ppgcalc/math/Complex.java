package cn.ac.nya.ppgcalc.math;

public class Complex {
    public double real;
    public double imag;

    public static Complex make(double real, double imag) {
        Complex c = new Complex();
        c.real = real; c.imag = imag;
        return c;
    }

    public static Complex[] fromArray(double[] a) {
        Complex[] cs = new Complex[a.length / 2];
        for (int i = 0; i < cs.length; i++) {
            cs[i] = new Complex();
            cs[i].real = a[2 * i];
            cs[i].imag = a[2 * i + 1];
        }
        return cs;
    }

    public static double[] toArray(double[] a) {
        double[] arrays = new double[a.length * 2];
        for (int i = 0; i < a.length; i++) {
            arrays[2 * i] = a[i];
            arrays[2 * i + 1] = 0;
        }
        return arrays;
    }

    public Complex cpy() {
        return make(real, imag);
    }

    public Complex add(Complex c) {
        Complex co = cpy();
        co.real += c.real;
        co.imag += c.imag;
        return co;
    }

    public Complex inv() {
        Complex co = cpy();
        co.real = -real;
        co.imag = -imag;
        return co;
    }

    public Complex sub(Complex c) {
        return add(c.inv());
    }

    public Complex mul(Complex c) {
        double r = real * c.real - imag * c.imag;
        double i = imag * c.real + c.imag * real;
        return make(r, i);
    }

    public double len() {
        return Math.sqrt(real * real + imag * imag);
    }

}
