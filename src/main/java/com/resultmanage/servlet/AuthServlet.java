package com.resultmanage.servlet;

import com.resultmanage.dao.TeacherDao;
import com.resultmanage.model.Teacher;
import com.resultmanage.util.AppConfig;
import com.resultmanage.util.JsonUtil;
import com.resultmanage.util.PasswordUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * POST /api/auth/login      email, password
 * POST /api/auth/register   name, email, password, accessCode
 * POST /api/auth/logout
 * GET  /api/auth/me         → who is logged in (or 401)
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends BaseServlet {

    private final TeacherDao teacherDao = new TeacherDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!"/me".equals(req.getPathInfo())) {
            send(resp, 404, error("Not found"));
            return;
        }
        HttpSession session = req.getSession(false);        // false = don't create a new one
        if (session == null || session.getAttribute("teacherId") == null) {
            send(resp, 401, error("Not logged in"));
            return;
        }
        send(resp, 200, "{\"id\":" + session.getAttribute("teacherId")
                + ",\"name\":" + JsonUtil.str(session.getAttribute("teacherName")) + "}");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();
        try {
            switch (path) {
                case "/login" -> login(req, resp);
                case "/register" -> register(req, resp);
                case "/logout" -> {
                    HttpSession session = req.getSession(false);
                    if (session != null) {
                        session.invalidate();                // server forgets this login
                    }
                    send(resp, 200, "{\"message\":\"Logged out\"}");
                }
                default -> send(resp, 404, error("Not found"));
            }
        } catch (SQLException e) {
            send(resp, 500, error("Database error: " + e.getMessage()));
        }
    }

    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String email = trim(req.getParameter("email"));      // reads the form field named "email"
        String password = req.getParameter("password");
        if (email.isEmpty() || password == null || password.isEmpty()) {
            send(resp, 400, error("Email and password are required."));
            return;
        }
        Teacher teacher = teacherDao.findByEmail(email);
        // Same message for "no such email" and "wrong password", so attackers can't discover which emails exist
        if (teacher == null || !PasswordUtil.verify(password, teacher.getPasswordHash())) {
            send(resp, 401, error("Invalid email or password."));
            return;
        }
        startSession(req, teacher);
        send(resp, 200, teacher.toJson());
    }

    private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String name = trim(req.getParameter("name"));
        String email = trim(req.getParameter("email")).toLowerCase();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String accessCode = trim(req.getParameter("accessCode"));

        // Validate everything BEFORE touching the database
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            send(resp, 400, error("Name, email and password are required."));
        } else if (!AppConfig.TEACHER_ACCESS_CODE.equals(accessCode)) {
            send(resp, 403, error("Invalid teacher access code."));
        } else if (password.length() < 6) {
            send(resp, 400, error("Password must be at least 6 characters."));
        } else if (teacherDao.findByEmail(email) != null) {
            send(resp, 409, error("An account with this email already exists."));
        } else {
            int id = teacherDao.insert(name, email, PasswordUtil.hash(password));  // store the HASH, never the password
            Teacher teacher = new Teacher(id, name, email, null);
            startSession(req, teacher);                                            // log them in straight away
            send(resp, 201, teacher.toJson());
        }
    }

    /** Remember the teacher on the server. The browser only gets a random JSESSIONID cookie. */
    private void startSession(HttpServletRequest req, Teacher teacher) {
        HttpSession session = req.getSession(true);   // true = create if missing
        req.changeSessionId();                         // new id after login, which blocks "session fixation" attacks
        session.setAttribute("teacherId", teacher.getId());
        session.setAttribute("teacherName", teacher.getName());
    }
}
