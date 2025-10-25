package com.order.dto;

import java.time.LocalDateTime;

public class TrackingEventDto {
    private String status;
    private String description;
    private LocalDateTime eventTime;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
}
