package cn.ac.nya.ppgcalc;

import java.util.LinkedList;

public class PPGData {

    public class PPGWave {
        public int red = 0;
        public int ir = 0;
    }

    public LinkedList<PPGWave> data;

    public float refSpO2;

    private int size;

    public PPGData(int size) {
        data = new LinkedList<>();
        refSpO2 = 0;
        this.size = size;
    }

    public int len() { return data.size(); }

    public void put(int red, int ir) {
        PPGWave wave = new PPGWave();
        wave.red = red; wave.ir = ir;
        data.addLast(wave);
        if (data.size() > size) data.removeFirst();
    }

    public double[] getRed() {
        double[] red = new double[len()];
        for (int i = 0; i < len(); i++)
            red[i] = (double) data.get(i).red;
        return red;
    }

    public int[] getRedInt() {
        int[] red = new int[len()];
        for (int i = 0; i < len(); i++)
            red[i] =  data.get(i).red;
        return red;
    }

    public double[] getIR() {
        double[] ir = new double[len()];
        for (int i = 0; i < len(); i++)
            ir[i] = (double) data.get(i).ir;
        return ir;
    }

    public int[] getIRInt() {
        int[] ir = new int[len()];
        for (int i = 0; i < len(); i++)
            ir[i] = data.get(i).ir;
        return ir;
    }

}
