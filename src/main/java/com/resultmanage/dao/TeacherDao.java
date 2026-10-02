package com.resultmanage.dao;

import com.resultmanage.model.Teacher;
import com.resultmanage.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TeacherDao {
        /** Finds a teacher by email (case-insensitive). Returns null if no teacher has this email. */
    public Teacher findByEmail(String email) throws SQLException {
        String sql = "SELECT id, name, email, password_hash FROM teachers WHERE LOWER(email) = LOWER(?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);                      // fill the ? BEFORE running the query
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {                         // found a row
                    return new Teacher(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password_hash"));
                }
                return null;                             // no teacher with this email
            }
        }
    }
   /** Saves a new teacher and returns the id PostgreSQL generated. */
    public int insert(String name, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO teachers(name, email, password_hash) VALUES (?, ?, ?) RETURNING id";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {   // RETURNING makes INSERT give back a row
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}