package com.resultmanage.servlet;

import com.resultmanage.dao.NoticeDao;
import com.resultmanage.model.Notice;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PUBLIC (anyone, no login).
 * GET /api/public/notices  → all notices, newest first.
 * Used by notices.html (students) and the teacher dashboard.
 */
@WebServlet("/api/public/notices")
public class PublicNoticeServlet extends BaseServlet {

    private final NoticeDao noticeDao = new NoticeDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<String> items = new ArrayList<>();
            for (Notice n : noticeDao.findAll()) {
                items.add(n.toJson());
            }
            send(resp, 200, "[" + String.join(",", items) + "]");
        } catch (SQLException e) {
            send(resp, 500, error("Database error: " + e.getMessage()));
        }
    }
}
