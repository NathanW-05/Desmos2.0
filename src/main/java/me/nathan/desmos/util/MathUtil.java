package me.nathan.desmos.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MathUtil {

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public static float log(float n, float base) {
        return (float) (Math.log(n) / Math.log(base));
    }

    public static List<int[]> bresenham(int x0, int y0, int x1, int y1) {
        List<int[]> line = new ArrayList<>();

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx-dy;
        int e2;

        while (true) {
            line.add(new int[]{x0,y0});

            if (x0 == x1 && y0 == y1)
                break;

            e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y0 = y0 + sy;
            }
        }
        return line;
    }
}
