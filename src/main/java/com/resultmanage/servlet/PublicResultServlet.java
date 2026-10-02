package com.resultmanage.servlet;

import com.resultmanage.dao.NoticeDao;
import com.resultmanage.dao.StudentDao;
import com.resultmanage.model.Student;
import com.resultmanage.util.JsonUtil;
import com.resultmanage.util.ResultCalc;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * PUBLIC (students, no login).
 * POST /api/public/result   rollNo, dob
 * Shows the marksheet only after the teacher's result date for the student's class.
 */
@WebServlet("/api/public/result")
public class PublicResultServlet extends BaseServlet {

    private final StudentDao studentDao = new StudentDao();
    private final NoticeDao noticeDao = new NoticeDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rollNo = trim(req.getParameter("rollNo"));
        String dobText = trim(req.getParameter("dob"));

        // ---- 1. Validate input ----
        if (rollNo.isEmpty() || dobText.isEmpty()) {
            send(resp, 400, error("Please enter your roll number and date of birth."));
            return;
        }
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobText);
        } catch (DateTimeParseException e) {
            send(resp, 400, error("Date of birth is not a valid date."));
            return;
        }

        try {
            // ---- 2. Find the student ----
            Student student = studentDao.findByRollAndDob(rollNo, dob);
            if (student == null) {
                // Same message whether the roll number or the DOB is wrong — don't help guessers
                send(resp, 404, error("No student found with this roll number and date of birth."));
                return;
            }

            // ---- 3. Has the result been released for this student's class? ----
            NoticeDao.ReleaseStatus status = noticeDao.releaseStatus(student.getClassName());
            if (status.released() == null) {
                String msg = status.upcoming() == null
                        ? "The result date has not been announced yet. Please check the Notice Board."
                        : "Your result will be published on the date below. Please check back then.";
                send(resp, 200, "{\"published\":false"
                        + ",\"name\":" + JsonUtil.str(student.getName())
                        + ",\"resultDate\":" + JsonUtil.str(status.upcoming())
                        + ",\"message\":" + JsonUtil.str(msg) + "}");
                return;
            }

            // ---- 4. Released — but has the teacher entered marks? ----
            if (student.getMarks() == null) {
                send(resp, 200, "{\"published\":false"
                        + ",\"name\":" + JsonUtil.str(student.getName())
                        + ",\"message\":" + JsonUtil.str("Your result has not been prepared yet. Please contact your teacher.")
                        + "}");
                return;
            }

            // ---- 5. Everything OK → full marksheet ----
            send(resp, 200, "{\"published\":true"
                    + ",\"resultDate\":" + JsonUtil.str(status.released())
                    + ",\"subjects\":" + subjectsJson()
                    + ",\"maxMarks\":" + ResultCalc.MAX_MARKS
                    + ",\"passMarks\":" + ResultCalc.PASS_MARKS
                    + ",\"student\":" + student.toJson() + "}");

        } catch (SQLException e) {
            send(resp, 500, error("Database error: " + e.getMessage()));
        }
    }

    /** ["Mathematics","Physics",...] — so the page shows the same subject names as Java uses. */
    private static String subjectsJson() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < ResultCalc.SUBJECTS.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(JsonUtil.str(ResultCalc.SUBJECTS[i]));
        }
        return sb.append("]").toString();
    }
}