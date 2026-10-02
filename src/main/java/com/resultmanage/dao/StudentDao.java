package com.resultmanage.dao;

import com.resultmanage.model.Student;
import com.resultmanage.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {

    /** Shared start of every "student + marks" query. */
    private static final String SELECT_WITH_MARKS =
            "SELECT s.id, s.roll_no, s.name, s.class_name, s.dob, "
          + "       m.sub1, m.sub2, m.sub3, m.sub4, m.sub5 "
          + "FROM students s "
          + "LEFT JOIN marks m ON m.student_id = s.id ";

    /** All students with their marks (marks = null if not entered yet). */
    public List<Student> findAll() throws SQLException {
        List<Student> list = new ArrayList<>();
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_WITH_MARKS + "ORDER BY s.class_name, s.roll_no");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    /** Student login for the result page: roll number (any capitals) + exact date of birth. */
    public Student findByRollAndDob(String rollNo, LocalDate dob) throws SQLException {
        String sql = SELECT_WITH_MARKS + "WHERE LOWER(s.roll_no) = LOWER(?) AND s.dob = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, rollNo);
            ps.setObject(2, dob);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;      // one student, or null if no match
            }
        }
    }

    /** Turns the current row into a Student. Used by both methods above. */
    private Student map(ResultSet rs) throws SQLException {
        int[] marks = null;
        if (rs.getObject("sub1") != null) {
            marks = new int[5];
            for (int i = 0; i < 5; i++) {
                marks[i] = rs.getInt("sub" + (i + 1));
            }
        }
        return new Student(
                rs.getInt("id"),
                rs.getString("roll_no"),
                rs.getString("name"),
                rs.getString("class_name"),
                rs.getObject("dob", LocalDate.class),
                marks);
    }

    public void insert(Student s, int teacherId) throws SQLException {
        String sql = "INSERT INTO students(roll_no, name, class_name, dob, created_by) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getRollNo());
            ps.setString(2, s.getName());
            ps.setString(3, s.getClassName());
            ps.setObject(4, s.getDob());          // setObject works with LocalDate
            ps.setInt(5, teacherId);              // which teacher added this student
            ps.executeUpdate();                   // INSERT/UPDATE/DELETE use executeUpdate, not executeQuery
        }
    }

    /** Returns true if a row was changed, false if no student had that id. */
    public boolean update(Student s) throws SQLException {
        String sql = "UPDATE students SET roll_no = ?, name = ?, class_name = ?, dob = ? WHERE id = ?";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getRollNo());
            ps.setString(2, s.getName());
            ps.setString(3, s.getClassName());
            ps.setObject(4, s.getDob());
            ps.setInt(5, s.getId());
            return ps.executeUpdate() == 1;       // executeUpdate returns how many rows were changed
        }
    }

    /** Deleting a student also deletes their marks (ON DELETE CASCADE in schema.sql). */
    public boolean delete(int id) throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM students WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}