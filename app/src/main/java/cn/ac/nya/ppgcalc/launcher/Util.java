package cn.ac.nya.ppgcalc.launcher;

import cn.ac.nya.ppgcalc.PPGData;

import java.io.BufferedReader;
import java.io.FileReader;

public class Util {

    private static int tryParse(String str) {
        int result;
        try {
            result = Integer.parseInt(str);
        } catch (Exception e) {
            result = 0;
        }
        return result;
    }

    public static void fromString(PPGData data, String str) {
        if (data == null) return;

        if (str.charAt(0) == 'P' && str.charAt(str.length() - 1) == ';') {
            str = str.substring(1, str.length() - 1);
            if (str.split(",").length == 2) {
                if (str.contains("P") || str.contains(";")) return;

                int red = tryParse(str.split(",")[0]);
                int ir = tryParse(str.split(",")[1]);

                data.put(red, ir);
            }
        }
    }

    public static PPGData fromFile(int N, String path) {
        PPGData data = new PPGData(N);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String buf;
            while (reader.ready()) {
                buf = reader.readLine();

                if (buf.split("\t").length != 2)
                    continue;

                int red = tryParse(buf.split("\t")[0]);
                int ir = tryParse(buf.split("\t")[1]);

                data.put(red, ir);
            }
        } catch (Exception e) {
            return null;
        }

        return data;
    }

}
