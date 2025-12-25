package com.orderflow.api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {
    @Id
    private String eventId;

    private OffsetDateTime processedAt;

    public ProcessedEvent() {
    }

    public ProcessedEvent(String eventId, OffsetDateTime processedAt) {
        this.eventId = eventId;
        this.processedAt = processedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
