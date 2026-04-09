package com.example.producer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "status", nullable = false, length = 32)
  private String status;

  @Column(name = "reason", length = 255)
  private String reason;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected OrderEntity() {
  }

  public OrderEntity(UUID id, String status, String reason) {
    this.id = id;
    this.status = status;
    this.reason = reason;
  }

  public UUID getId() { return id; }
  public String getStatus() { return status; }
  public String getReason() { return reason; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  public void setStatus(String status) { this.status = status; }
  public void setReason(String reason) { this.reason = reason; }
}
