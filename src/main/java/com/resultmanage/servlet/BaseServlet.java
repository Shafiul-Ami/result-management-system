package com.resultmanage.servlet;

import com.resultmanage.util.JsonUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Parent class for all API servlets: shared helper methods live here once. */
public abstract class BaseServlet extends HttpServlet {

    /** Reads a form field; never returns null. */
    protected static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /** Builds {"error":"..."} */
    protected static String error(String message) {
        return "{\"error\":" + JsonUtil.str(message) + "}";
    }

    /** Builds {"message":"..."} */
    protected static String message(String message) {
        return "{\"message\":" + JsonUtil.str(message) + "}";
    }

    /** Sends a JSON response with a status code. */
    protected static void send(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(json);
    }

    /** Id of the logged-in teacher. Safe for /api/teacher/* because AuthFilter guarantees a session. */
    protected static int teacherId(HttpServletRequest req) {
        return (Integer) req.getSession(false).getAttribute("teacherId");
    }
}
