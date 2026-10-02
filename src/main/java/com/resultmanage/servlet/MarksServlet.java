package com.resultmanage.servlet;

import com.resultmanage.dao.MarksDao;
import com.resultmanage.util.ResultCalc;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Teacher only (under /api/teacher/, so AuthFilter protects it).
 * POST /api/teacher/marks   studentId, sub1, sub2, sub3, sub4, sub5
 */
@WebServlet("/api/teacher/marks")
public class MarksServlet extends BaseServlet {

    private final MarksDao marksDao = new MarksDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int studentId;
        int[] marks = new int[ResultCalc.SUBJECTS.length];

        // 1. Read and validate every number
        try {
            studentId = Integer.parseInt(trim(req.getParameter("studentId")));
        } catch (NumberFormatException e) {
            send(resp, 400, error("Please select a student."));
            return;
        }
        for (int i = 0; i < marks.length; i++) {
            String subject = ResultCalc.SUBJECTS[i];
            try {
                marks[i] = Integer.parseInt(trim(req.getParameter("sub" + (i + 1))));
            } catch (NumberFormatException e) {
                send(resp, 400, error(subject + " marks must be a whole number."));
                return;
            }
            if (marks[i] < 0 || marks[i] > ResultCalc.MAX_MARKS) {
                send(resp, 400, error(subject + " marks must be between 0 and " + ResultCalc.MAX_MARKS + "."));
                return;
            }
        }

        // 2. Save
        try {
            marksDao.save(studentId, marks, teacherId(req));
            send(resp, 200, message("Marks saved successfully."));
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {      // foreign key broken → no student with this id
                send(resp, 404, error("Student not found."));
            } else {
                send(resp, 500, error("Database error: " + e.getMessage()));
            }
        }
    }
}
