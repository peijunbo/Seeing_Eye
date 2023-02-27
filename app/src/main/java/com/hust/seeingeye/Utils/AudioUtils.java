package com.hust.seeingeye.Utils;


import com.hust.seeingeye.R;

public class AudioUtils {
    public static Integer parse2id(Double distance) {
        if (distance == null) return R.raw.find_error;
        distance /= 10;//在这里换成单位为厘米
        if (distance > 1000) return R.raw.further_than_ten;
        if (distance >= 100) {
            distance /= 100;
            switch ((int) distance.floatValue()) {
                case 5:
                case 4:
                    //[400,600)
                    return R.raw.around_five;
                case 3:
                    //[300,400)
                    return R.raw.closer_than_four;
                case 2:
                    //[200,300)
                    return R.raw.closer_than_three;
                case 1:
                    //[100,200)
                    return R.raw.closer_than_two;
                default:
                    //[600,1000)
                    return R.raw.further_than_five;
            }
        } else if (distance >= 10) {
            distance /= 10;
            int res = Integer.parseInt(String.format("%.0f", distance));
            switch (res) {
                case 10:
                    return R.raw.ten;
                case 9:
                    return R.raw.nine;
                case 8:
                    return R.raw.eight;
                case 7:
                    return R.raw.seven;
                case 6:
                    return R.raw.six;
                case 5:
                    return R.raw.five;
                case 4:
                    return R.raw.four;
                case 3:
                    return R.raw.three;
                case 2:
                    return R.raw.two;
                case 1:
                    return R.raw.one;
            }
        }
        else {
            switch ((int) distance.floatValue()) {
                case 9:
                case 8:
                    return R.raw.ten;
                case 7:
                case 6:
                case 5:
                    return R.raw.half;
                default:
                    return R.raw.quarter;
            }
        }
        return null;
    }
}
