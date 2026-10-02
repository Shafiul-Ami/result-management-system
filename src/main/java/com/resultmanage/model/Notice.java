package com.resultmanage.model;

import com.resultmanage.util.JsonUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Notice {

    private final int id;
    private final String title;
    private final String message;
    private final LocalDate resultDate; // null = general notice
    private final String className; // null = all classes
    private final LocalDateTime createdAt;

    public Notice(int id, String title, String message, LocalDate resultDate,
            String className, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.resultDate = resultDate;
        this.className = className;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public LocalDate getResultDate() {
        return resultDate;
    }

    public String getClassName() {
        return className;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** A result notice is "released" once its date is today or earlier. */
    public boolean isReleased() {
        return resultDate != null && !resultDate.isAfter(LocalDate.now());
    }

    public String toJson() {
        return "{"
                + "\"id\":" + id + ","
                + "\"title\":" + JsonUtil.str(title) + ","
                + "\"message\":" + JsonUtil.str(message) + ","
                + "\"resultDate\":" + JsonUtil.str(resultDate) + ","
                + "\"className\":" + JsonUtil.str(className) + ","
                + "\"released\":" + isReleased() + ","
                + "\"createdAt\":" + JsonUtil.str(createdAt.toLocalDate())
                + "}";
    }
}
