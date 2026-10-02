package com.resultmanage.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/** Runs BEFORE every /api/teacher/* servlet. No login → request is stopped here. */
@WebFilter("/api/teacher/*")
public class AuthFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("teacherId") == null) {
            resp.setStatus(401);
            resp.setContentType("application/json");
            resp.getWriter().write("{\"error\":\"Please log in as a teacher.\"}");
            return;                          // the servlet never runs
        }
        chain.doFilter(req, resp);           // logged in → continue to the servlet
    }
}
