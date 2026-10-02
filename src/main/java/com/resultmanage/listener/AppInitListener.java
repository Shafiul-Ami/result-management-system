package com.resultmanage.listener;

import com.resultmanage.util.DBUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

@WebListener
public class AppInitListener implements ServletContextListener {

    /** App starts → run schema.sql. CREATE TABLE IF NOT EXISTS makes this safe to run every time. */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        try (InputStream in = ctx.getResourceAsStream("/WEB-INF/schema.sql")) {
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);

            // Remove comment lines ("-- ...") so a ';' inside a comment can't break the split below
            StringBuilder clean = new StringBuilder();
            for (String line : sql.split("\n")) {
                if (!line.trim().startsWith("--")) {
                    clean.append(line).append("\n");
                }
            }

            try (Connection con = DBUtil.getConnection();
                 Statement st = con.createStatement()) {
                for (String statement : clean.toString().split(";")) {
                    if (!statement.isBlank()) {
                        st.execute(statement);
                    }
                }
            }
            ctx.log("ResultManagementSystem: database tables are ready");
        } catch (Exception e) {
            // Don't crash Tomcat — the pages still load and API calls will show the DB error
            ctx.log("ResultManagementSystem: could not create tables - check app.properties", e);
        }
    }

    /** App stops → unregister the PostgreSQL driver (fixes the memory-leak WARNING in Tomcat's log). */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == getClass().getClassLoader()) {   // only OUR app's driver
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ignored) {
                    // shutting down anyway
                }
            }
        }
    }
}