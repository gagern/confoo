package net.von_gagern.martin.cetm.fun;

import java.util.Random;

/**
 * Command line tool to test accuracy of Clausen's integral.<p>
 *
 * This tool is not an automatic unit check which fails when some
 * value exceeds a given error bound, but instead an interactive tool
 * which can be used to measure and compare the performance of the
 * numeric implementation.<p>
 */
abstract class CliClausen {

    public static void main(String[] args) {
        boolean jtem = false;
        for (String arg: args) {
            if ("jtem".equals(arg)) {
                jtem = true;
            } else if ("own".equals(arg)) {
                jtem = false;
            } else if ("cmpTable".equals(arg)) {
                new CliClausen(jtem, arg + (jtem ? " (jtem)" : "")) {
                    public void generate() {
                        for (int i = 0; i < TestClausen.x.length; ++i) {
                            measureError(TestClausen.x[i], TestClausen.cl[i]);
                        }
                    }
                };
            } else if ("cmpMultiTable".equals(arg)) {
                new CliClausen(jtem, arg + (jtem ? " (jtem)" : "")) {
                    public void generate() {
                        for (int j = -TestClausen.PERIODS;
                             j <= TestClausen.PERIODS; j++) {
                            double x0 = j*(2*Math.PI);
                            for (int i = 0; i < TestClausen.x.length; i++) {
                                double xx = x0 + TestClausen.x[i];
                                measureError(xx, TestClausen.cl[i]);
                                xx = x0 - TestClausen.x[i];
                                measureError(xx, -TestClausen.cl[i]);
                            }
                        }
                    }
                };
            } else if ("cmpJtemRnd".equals(arg)) {
                new CliClausen(false, arg) {
                    public void generate() {
                        Random rnd = new Random(499402343);
                        for (int i = 0; i < 1000; ++i) {
                            double x = (rnd.nextDouble() - 0.5)*7;
                            x += rnd.nextDouble()/4;
                            measureError(x, jtemCl2(x));
                        }
                    }
                };
            } else {
                double x = Double.parseDouble(arg);
                System.out.println("cl2(" + x + ")=" + cl2(jtem, x));
            }
        }
    }

    private static boolean jtem = false;

    private double abs_delta = Double.NEGATIVE_INFINITY;
    private double abs_ulps = Double.NEGATIVE_INFINITY;
    private double abs_exp = Double.NaN;
    private double abs_act = Double.NaN;
    private double abs_x = Double.NaN;

    private double rel_delta = Double.NEGATIVE_INFINITY;
    private double rel_ulps = Double.NEGATIVE_INFINITY;
    private double rel_exp = Double.NaN;
    private double rel_act = Double.NaN;
    private double rel_x = Double.NaN;

    private CliClausen(boolean jtem, String msg) {
        this.jtem = jtem;
        if (msg != null) System.out.println(msg);
        generate();
        System.out.println("Maximal absolute error: " + abs_delta +
                           " = " + (abs_delta/TestClausen.EPS) + " EPS = " +
                           abs_ulps + " ulp at x = " + abs_x +
                           ": expected " + abs_exp + " but got " + abs_act);
        System.out.println("Maximal relative error: " + rel_delta +
                           " = " + (rel_delta/TestClausen.EPS) + " EPS = " +
                           rel_ulps + " ulp at x = " + rel_x +
                           ": expected " + rel_exp + " but got " + rel_act);
        System.out.println();
    }

    protected abstract void generate();

    protected void measureError(double x, double expected) {
        double actual = cl2(jtem, x);
        double delta = Math.abs(expected - actual);
        double ulp = Math.max(Math.ulp(expected), Math.ulp(actual));
        double ulps = delta/ulp;
        if (delta > abs_delta) {
            abs_delta = delta;
            abs_ulps = ulps;
            abs_exp = expected;
            abs_act = actual;
            abs_x = x;
        }
        if (ulps > rel_ulps) {
            rel_delta = delta;
            rel_ulps = ulps;
            rel_exp = expected;
            rel_act = actual;
            rel_x = x;
        }
    }

    private static double jtemCl2(double x) {
        return de.jtem.numericalMethods.calculus.specialFunctions.
            Clausen.cl2(x);
    }

    private static double cl2(boolean jtem, double x) {
        if (jtem) return jtemCl2(x);
        else return Clausen.cl2(x);
    }

}
