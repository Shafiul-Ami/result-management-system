package com.resultmanage.servlet;

import com.resultmanage.dao.NoticeDao;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Teacher only (AuthFilter protects /api/teacher/*).
 * POST   /api/teacher/notices        title, message [, resultDate, className]
 * DELETE /api/teacher/notices?id=3
 *
 * Listing notices is NOT here — the dashboard reuses GET /api/public/notices.
 */
@WebServlet("/api/teacher/notices")
public class NoticeServlet extends BaseServlet {

    private final NoticeDao noticeDao = new NoticeDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String title = trim(req.getParameter("title"));
        String message = trim(req.getParameter("message"));
        String dateText = trim(req.getParameter("resultDate"));
        String className = trim(req.getParameter("className"));

        // 1. Required fields
        if (title.isEmpty() || message.isEmpty()) {
            send(resp, 400, error("Title and message are required."));
            return;
        }
        if (title.length() > 200) {                       // matches VARCHAR(200) in schema.sql
            send(resp, 400, error("Title must be 200 characters or fewer."));
            return;
        }

        // 2. Optional result date: empty → general notice
        LocalDate resultDate = null;
        if (!dateText.isEmpty()) {
            try {
                resultDate = LocalDate.parse(dateText);
            } catch (DateTimeParseException e) {
                send(resp, 400, error("Result date is not a valid date."));
                return;
            }
        }

        // 3. Class only matters for result notices; empty means "all classes" → NULL
        if (resultDate == null || className.isEmpty()) {
            className = null;
        }

        // 4. Save
        try {
            noticeDao.insert(title, message, resultDate, className, teacherId(req));
            send(resp, 201, message("Notice published."));
        } catch (SQLException e) {
            send(resp, 500, error("Database error: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(trim(req.getParameter("id")));
            if (noticeDao.delete(id)) {
                send(resp, 200, message("Notice deleted."));
            } else {
                send(resp, 404, error("Notice not found."));
            }
        } catch (NumberFormatException e) {
            send(resp, 400, error("Invalid notice id."));
        } catch (SQLException e) {
            send(resp, 500, error("Database error: " + e.getMessage()));
        }
    }
}
