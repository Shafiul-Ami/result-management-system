package com.resultmanage.dao;

import com.resultmanage.model.Notice;
import com.resultmanage.util.DBUtil;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoticeDao {
    /**
     * released = latest result date that has already arrived (null if none)
     * upcoming = next result date still in the future     (null if none)
     */
    public record ReleaseStatus(LocalDate released, LocalDate upcoming) {
    }
        /** Is there a result notice for this class (or for ALL classes)? Released yet, or coming? */
    public ReleaseStatus releaseStatus(String className) throws SQLException {
        String sql = "SELECT "
                   + "  MAX(result_date) FILTER (WHERE result_date <= CURRENT_DATE) AS released, "
                   + "  MIN(result_date) FILTER (WHERE result_date >  CURRENT_DATE) AS upcoming "
                   + "FROM notices "
                   + "WHERE result_date IS NOT NULL "
                   + "  AND (class_name IS NULL OR LOWER(class_name) = LOWER(?))";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, className);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();     // aggregate queries ALWAYS return exactly one row
                return new ReleaseStatus(
                        rs.getObject("released", LocalDate.class),
                        rs.getObject("upcoming", LocalDate.class));
            }
        }
    }
    public List<Notice> findAll() throws SQLException {
        String sql = "SELECT id, title, message, result_date, class_name, created_at "
                + "FROM notices ORDER BY created_at DESC";
        List<Notice> list = new ArrayList<>();

        try (Connection con = DBUtil.getConnection();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            // For each row, create a Notice and add it to the list.

            while (rs.next()) { // move to the next row; false when no rows are left
                Notice notice = new Notice(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("message"),
                        rs.getObject("result_date", LocalDate.class), // null if empty
                        rs.getString("class_name"), // null if empty
                        rs.getObject("created_at", LocalDateTime.class));
                list.add(notice); // one row → one Notice object → into the list
            }
        }
        return list;
    }
        /** resultDate and className may be null (general notice / all classes). */
    public void insert(String title, String message, LocalDate resultDate,
                       String className, int teacherId) throws SQLException {
        String sql = "INSERT INTO notices(title, message, result_date, class_name, created_by) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, message);
            if (resultDate == null) {
                ps.setNull(3, Types.DATE);     // store a real NULL → general notice
            } else {
                ps.setObject(3, resultDate);
            }
            ps.setString(4, className);        // setString(null) also stores NULL
            ps.setInt(5, teacherId);
            ps.executeUpdate();
        }
    }

    /** Returns true if a notice was deleted, false if no notice had that id. */
    public boolean delete(int id) throws SQLException {
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM notices WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}