package com.carrental.domain;

import java.time.LocalDateTime;

public record DateRange(LocalDateTime start, LocalDateTime end) {

    public DateRange {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End date must be strictly after start date");
        }
    }

    public static DateRange of(LocalDateTime start, int durationDays) {
        if (durationDays <= 0) {
            throw new IllegalArgumentException("Duration in days must be positive");
        }
        return new DateRange(start, start.plusDays(durationDays));
    }

    public boolean overlapsWith(DateRange other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
