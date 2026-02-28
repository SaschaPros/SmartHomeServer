package com.spro93.smarthome.util;

import java.time.ZonedDateTime;

public class GeoMag {

    private static final double[][] gnmWmm2020 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {-29404.5, -1450.7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {-2500, 2982, 1676.8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {1363.9, -2381, 1236.2, 525.7, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {903.1, 809.4, 86.2, -309.4, 47.9, 0, 0, 0, 0, 0, 0, 0, 0},
            {-234.4, 363.1, 187.8, -140.7, -151.2, 13.7, 0, 0, 0, 0, 0, 0, 0},
            {65.9, 65.6, 73, -121.5, -36.2, 13.5, -64.7, 0, 0, 0, 0, 0, 0},
            {80.6, -76.8, -8.3, 56.5, 15.8, 6.4, -7.2, 9.8, 0, 0, 0, 0, 0},
            {23.6, 9.8, -17.5, -0.4, -21.1, 15.3, 13.7, -16.5, -0.3, 0, 0, 0, 0},
            {5, 8.2, 2.9, -1.4, -1.1, -13.3, 1.1, 8.9, -9.3, -11.9, 0, 0, 0},
            {-1.9, -6.2, -0.1, 1.7, -0.9, 0.6, -0.9, 1.9, 1.4, -2.4, -3.9, 0, 0},
            {3, -1.4, -2.5, 2.4, -0.9, 0.3, -0.7, -0.1, 1.4, -0.6, 0.2, 3.1, 0},
            {-2, -0.1, 0.5, 1.3, -1.2, 0.7, 0.3, 0.5, -0.2, -0.5, 0.1, -1.1, -0.3},
    };

    private static final double[][] hnmWmm2020 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 4652.9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, -2991.6, -734.8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, -82.2, 241.8, -542.9, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 282, -158.4, 199.8, -350.1, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 47.7, 208.4, -121.3, 32.2, 99.1, 0, 0, 0, 0, 0, 0, 0},
            {0, -19.1, 25, 52.7, -64.4, 9, 68.1, 0, 0, 0, 0, 0, 0},
            {0, -51.4, -16.8, 2.3, 23.5, -2.2, -27.2, -1.9, 0, 0, 0, 0, 0},
            {0, 8.4, -15.3, 12.8, -11.8, 14.9, 3.6, -6.9, 2.8, 0, 0, 0, 0},
            {0, -23.3, 11.1, 9.8, -5.1, -6.2, 7.8, 0.4, -1.5, 9.7, 0, 0, 0},
            {0, 3.4, -0.2, 3.5, 4.8, -8.6, -0.1, -4.2, -3.4, -0.1, -8.8, 0, 0},
            {0, 0, 2.6, -0.5, -0.4, 0.6, -0.2, -1.7, -1.6, -3, -2, -2.6, 0},
            {0, -1.2, 0.5, 1.3, -1.8, 0.1, 0.7, -0.1, 0.6, 0.2, -0.9, 0, 0.5},
    };

    private static final double[][] gtnmWmm2020 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {6.7, 7.7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {-11.5, -7.1, -2.2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {2.8, -6.2, 3.4, -12.2, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {-1.1, -1.6, -6, 5.4, -5.5, 0, 0, 0, 0, 0, 0, 0, 0},
            {-0.3, 0.6, -0.7, 0.1, 1.2, 1, 0, 0, 0, 0, 0, 0, 0},
            {-0.6, -0.4, 0.5, 1.4, -1.4, 0, 0.8, 0, 0, 0, 0, 0, 0},
            {-0.1, -0.3, -0.1, 0.7, 0.2, -0.5, -0.8, 1, 0, 0, 0, 0, 0},
            {-0.1, 0.1, -0.1, 0.5, -0.1, 0.4, 0.5, 0, 0.4, 0, 0, 0, 0},
            {-0.1, -0.2, 0, 0.4, -0.3, 0, 0.3, 0, 0, -0.4, 0, 0, 0},
            {0, 0, 0, 0.2, -0.1, -0.2, 0, -0.1, -0.2, -0.1, 0, 0, 0},
            {0, -0.1, 0, 0, 0, -0.1, 0, 0, -0.1, -0.1, -0.1, -0.1, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -0.1},
    };

    private static final double[][] htnmWmm2020 = {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, -25.1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, -30.2, -23.9, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 5.7, -1, 1.1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0.2, 6.9, 3.7, -5.6, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0.1, 2.5, -0.9, 3, 0.5, 0, 0, 0, 0, 0, 0, 0},
            {0, 0.1, -1.8, -1.4, 0.9, 0.1, 1, 0, 0, 0, 0, 0, 0},
            {0, 0.5, 0.6, -0.7, -0.2, -1.2, 0.2, 0.3, 0, 0, 0, 0, 0},
            {0, -0.3, 0.7, -0.2, 0.5, -0.3, -0.5, 0.4, 0.1, 0, 0, 0, 0},
            {0, -0.3, 0.2, -0.4, 0.4, 0.1, 0, -0.2, 0.5, 0.2, 0, 0, 0},
            {0, 0, 0.1, -0.3, 0.1, -0.2, 0.1, 0, -0.1, 0.2, 0, 0, 0},
            {0, 0, 0.1, 0, 0.2, 0, 0, 0.1, 0, -0.1, 0, 0, 0},
            {0, 0, 0, -0.1, 0.1, 0, 0, 0, 0.1, 0, 0, 0, -0.1},
    };

    private static final double GLOBE_A = 6378.137;
    private static final double GLOBE_B = 6356.7523142;
    private static final double GLOBE_R0 = 6371.2;

    private static final double[] root = new double[13];
    private static final double[][][] roots = new double[13][13][2];

    static {
        for (int n = 2; n <= 12; n++) {
            root[n] = Math.sqrt((2.0 * n - 1) / (2.0 * n));
        }

        for (int m = 0; m <= 12; m++) {
            int mm = m * m;
            for (int n = Math.max(m + 1, 2); n <= 12; n++) {
                roots[m][n][0] = Math.sqrt((n - 1) * (n - 1) - mm);
                roots[m][n][1] = 1.0 / Math.sqrt(n * n - mm);
            }
        }
    }

    public static double getDeclination(double latitude, double longitude) {
        return getDeclination(latitude, longitude, 0);
    }

    public static double getDeclination(double latitude, double longitude, double altitude) {
        double cosLat = Math.cos(deg2rad(latitude));
        double sinLat = Math.sin(deg2rad(latitude));
        double sr = Math.sqrt(Math.pow(GLOBE_A, 2) * Math.pow(cosLat, 2) + Math.pow(GLOBE_B, 2) * Math.pow(sinLat, 2));
        double theta = Math.atan2(cosLat * (altitude * sr + Math.pow(GLOBE_A, 2)), sinLat * (altitude * sr + Math.pow(GLOBE_B, 2)));
        double r = Math.sqrt(
                Math.pow(altitude, 2) +
                        2 * altitude * sr +
                        (Math.pow(GLOBE_A, 4) - (Math.pow(GLOBE_A, 4) - Math.pow(GLOBE_B, 4)) * Math.pow(sinLat, 2)) /
                                (Math.pow(GLOBE_A, 2) - (Math.pow(GLOBE_A, 2) - Math.pow(GLOBE_B, 2)) * Math.pow(sinLat, 2)));
        double c = Math.cos(theta);
        double s = Math.sin(theta);
        double invS = 1 / (s + (s == 0 ? 1e-8 : 0));

        double[][] P = new double[13][13];
        double[][] DP = new double[13][13];
        P[0][0] = 1;
        P[1][1] = s;
        P[1][0] = c;
        DP[1][0] = -s;
        DP[1][1] = c;

        for (int n = 2; n <= 12; n++) {
            P[n][n] = P[n - 1][n - 1] * s * root[n];
            DP[n][n] = (DP[n - 1][n - 1] * s + P[n - 1][n - 1] * c) * root[n];
        }

        for (int m = 0; m <= 12; m++) {
            for (int n = Math.max(m + 1, 2); n <= 12; n++) {
                P[n][m] = (P[n - 1][m] * c * (2 * n - 1) - P[n - 2][m] * roots[m][n][0]) * roots[m][n][1];
                DP[n][m] = ((DP[n - 1][m] * c - P[n - 1][m] * s) * (2 * n - 1) - DP[n - 2][m] * roots[m][n][0]) *
                        roots[m][n][1];
            }
        }

        double julianYears = julianYearsSince2020();
        double[][] gnm = new double[13][13];
        double[][] hnm = new double[13][13];
        for (int n = 1; n <= 12; n++) {
            for (int m = 0; m <= 12; m++) {
                gnm[n][m] = gnmWmm2020[n][m] + julianYears * gtnmWmm2020[n][m];
                hnm[n][m] = hnmWmm2020[n][m] + julianYears * htnmWmm2020[n][m];
            }
        }

        double[] sm = new double[13];
        double[] cm = new double[13];
        for (int m = 0; m <= 12; m++) {
            sm[m] = Math.sin(m * deg2rad(longitude));
            cm[m] = Math.cos(m * deg2rad(longitude));
        }

        double BR = 0.0;
        double BTheta = 0.0;
        double BPhi = 0.0;
        double fn0 = GLOBE_R0 / r;
        double fn = Math.pow(fn0, 2);

        for (int n = 1; n <= 12; n++) {
            double c1n = 0;
            double c2n = 0;
            double c3n = 0;
            for (int m = 0; m <= n; m++) {
                double tmp = gnm[n][m] * cm[m] + hnm[n][m] * sm[m];
                c1n += tmp * P[n][m];
                c2n += tmp * DP[n][m];
                c3n += m * (gnm[n][m] * sm[m] - hnm[n][m] * cm[m]) * P[n][m];
            }
            fn *= fn0;
            BR += (n + 1) * c1n * fn;
            BTheta -= c2n * fn;
            BPhi += c3n * fn * invS;
        }

        double psi = theta - (Math.PI / 2 - deg2rad(latitude));
        double sinPsi = Math.sin(psi);
        double cosPsi = Math.cos(psi);
        double X = -BTheta * cosPsi - BR * sinPsi;
        double Y = BPhi;

        return X != 0 || Y != 0 ? round(rad2deg(Math.atan2(Y, X)), 2) : 0;
    }

    private static double julianYearsSince2020() {
        return (System.currentTimeMillis() / 86400000.0 + 2440587.5 - 2458850.0) / 365.25;
    }

    private static double round(double num, int decimalPlaces) {
        double factor = Math.pow(10, decimalPlaces);
        return Math.round(num * factor) / factor;
    }

    private static double deg2rad(double deg) {
        return deg * 0.017453292519943295;
    }

    private static double rad2deg(double rad) {
        return rad * 57.29577951308232;
    }
}
