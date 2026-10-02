package com.resultmanage.dao;

import com.resultmanage.util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MarksDao {

    /** First time: INSERT. Next times: UPDATE the same row. One SQL statement does both. */
    public void save(int studentId, int[] marks, int teacherId) throws SQLException {
        String sql = "INSERT INTO marks(student_id, sub1, sub2, sub3, sub4, sub5, updated_by) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                   + "ON CONFLICT (student_id) DO UPDATE SET "
                   + "  sub1 = EXCLUDED.sub1, sub2 = EXCLUDED.sub2, sub3 = EXCLUDED.sub3, "
                   + "  sub4 = EXCLUDED.sub4, sub5 = EXCLUDED.sub5, "
                   + "  updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP";
        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            for (int i = 0; i < 5; i++) {
                ps.setInt(i + 2, marks[i]);        // placeholders 2..6 are sub1..sub5
            }
            ps.setInt(7, teacherId);
            ps.executeUpdate();
        }
    }
}
