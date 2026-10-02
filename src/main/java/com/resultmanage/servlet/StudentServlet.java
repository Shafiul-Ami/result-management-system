package com.resultmanage.servlet;

import com.resultmanage.dao.StudentDao;
import com.resultmanage.model.Student;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Teacher only (protected by AuthFilter).
 * GET    /api/teacher/students          → list all students
 * POST   /api/teacher/students          rollNo, name, className, dob     → add
 * POST   /api/teacher/students          id + the same fields             → edit
 * DELETE /api/teacher/students?id=5     → delete
 */
@WebServlet("/api/teacher/students")
public class StudentServlet extends BaseServlet {

    private final StudentDao studentDao = new StudentDao();

    // ---------- READ ----------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<String> items = new ArrayList<>();
            for (Student s : studentDao.findAll()) {
                items.add(s.toJson());
            }
            send(resp, 200, "[" + String.join(",", items) + "]");   // join adds commas BETWEEN items
        } catch (SQLException e) {
            send(resp, 500, error("Database error: " + e.getMessage()));
        }
    }

    // ---------- CREATE and UPDATE ----------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idText = trim(req.getParameter("id"));
        String rollNo = trim(req.getParameter("rollNo"));
        String name = trim(req.getParameter("name"));
        String className = trim(req.getParameter("className"));
        String dobText = trim(req.getParameter("dob"));

        // 1. Validate — never trust data from the browser
        if (rollNo.isEmpty() || name.isEmpty() || className.isEmpty() || dobText.isEmpty()) {
            send(resp, 400, error("Roll number, name, class and date of birth are all required."));
            return;
        }
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobText);           // <input type="date"> sends "2006-03-14"
        } catch (DateTimeParseException e) {
            send(resp, 400, error("Date of birth is not a valid date."));
            return;
        }
        if (dob.isAfter(LocalDate.now())) {
            send(resp, 400, error("Date of birth cannot be in the future."));
            return;
        }

        // 2. No id → new student.  Has id → edit existing student.
        try {
            if (idText.isEmpty()) {
                studentDao.insert(new Student(0, rollNo, name, className, dob), teacherId(req));
                send(resp, 201, message("Student added successfully."));
            } else {
                Student student = new Student(Integer.parseInt(idText), rollNo, name, className, dob);
                if (studentDao.update(student)) {
                    send(resp, 200, message("Student updated successfully."));
                } else {
                    send(resp, 404, error("Student not found."));
                }
            }
        } catch (NumberFormatException e) {
            send(resp, 400, error("Invalid student id."));
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {   // PostgreSQL code for "UNIQUE rule broken"
                send(resp, 409, error("Roll number " + rollNo + " already exists."));
            } else {
                send(resp, 500, error("Database error: " + e.getMessage()));
            }
        }
    }

    // ---------- DELETE ----------
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            int id = Integer.parseInt(trim(req.getParameter("id")));   // from the URL: ?id=5
            if (studentDao.delete(id)) {
                send(resp, 200, message("Student deleted."));
            } else {
                send(resp, 404, error("Student not found."));
            }
        } catch (NumberFormatException e) {
            send(resp, 400, error("Invalid student id."));
        } catch (SQLException e) {
            send(resp, 500, error("Database error: " + e.getMessage()));
        }
    }
}
