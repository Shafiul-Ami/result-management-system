package com.resultmanage.util;

/** Subject names and the rules for total, percentage, grade and pass/fail. */
public final class ResultCalc {

    /** sub1..sub5 in the database, in this order. Keep in sync with SUBJECTS in dashboard.js. */
    public static final String[] SUBJECTS = {
        "Mathematics", "Physics", "Chemistry", "English", "Computer Science"
    };
    public static final int MAX_MARKS = 100;
    public static final int PASS_MARKS = 40;

    private ResultCalc() {
    }

    public static int total(int[] marks) {
        int sum = 0;
        for (int m : marks) {
            sum += m;
        }
        return sum;
    }

    /** 438 out of 500 → 87.6  (rounded to 2 decimals) */
    public static double percentage(int[] marks) {
        double pct = total(marks) * 100.0 / (MAX_MARKS * marks.length);
        return Math.round(pct * 100) / 100.0;
    }

    /** A student passes only if EVERY subject is at least PASS_MARKS. */
    public static boolean passed(int[] marks) {
        for (int m : marks) {
            if (m < PASS_MARKS) {
                return false;
            }
        }
        return true;
    }

    public static String grade(int[] marks) {
        if (!passed(marks)) {
            return "F";                 // failed a subject → F, even with a high percentage
        }
        double pct = percentage(marks);
        if (pct >= 90) {                // check from the HIGHEST grade down
            return "A+";
        } else if (pct >= 80) {
            return "A";
        } else if (pct >= 70) {
            return "B+";
        } else if (pct >= 60) {
            return "B";
        } else if (pct >= 50) {
            return "C";
        } else {
            return "D";
        }
    }

    /** The extra JSON fields added to a student who has marks. */
    public static String summaryJson(int[] marks) {
        return "\"total\":" + total(marks)
                + ",\"maxTotal\":" + (MAX_MARKS * marks.length)
                + ",\"percentage\":" + percentage(marks)
                + ",\"grade\":" + JsonUtil.str(grade(marks))
                + ",\"status\":" + JsonUtil.str(passed(marks) ? "PASS" : "FAIL");
    }
}