package com.resultmanage.model;

import java.time.LocalDate;
import java.util.Arrays;

import com.resultmanage.util.JsonUtil;
import com.resultmanage.util.ResultCalc;

public class Student {
    private final int id;
    private final String rollNo;
    private final String name;
    private final String className;
    private final LocalDate dob;
    private final int[] marks;      // null = marks not entered yet
       /** Student without marks — used when adding or editing a student. */
    public Student(int id, String rollNo, String name, String className, LocalDate dob) {
        this(id, rollNo, name, className, dob, null);        // calls the constructor below
    }

    /** Student with marks — used when reading from the database. */
    public Student(int id, String rollNo, String name, String className, LocalDate dob, int[] marks) {
        this.id = id;
        this.rollNo = rollNo;
        this.name = name;
        this.className = className;
        this.dob = dob;
        this.marks = marks;
    }

    public int getId() {
        return id;
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public LocalDate getDob() {
        return dob;
    }
        public int[] getMarks() {
        return marks;
    }
       public String toJson() {
        String json = "{"
                + "\"id\":" + id + ","
                + "\"rollNo\":" + JsonUtil.str(rollNo) + ","
                + "\"name\":" + JsonUtil.str(name) + ","
                + "\"className\":" + JsonUtil.str(className) + ","
                + "\"dob\":" + JsonUtil.str(dob) + ","
                + "\"marks\":" + (marks == null ? "null" : Arrays.toString(marks));   // [92, 85, 78, 88, 95]
        if (marks != null) {
            json += "," + ResultCalc.summaryJson(marks);     // total, percentage, grade, status
        }
        return json + "}";
    }
}
