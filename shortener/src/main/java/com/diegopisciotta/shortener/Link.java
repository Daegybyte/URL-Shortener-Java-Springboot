// class
package com.diegopisciotta.shortener;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a shortened URL entry mapped to the database links table.
 * This JPA entity manages the mapping between a generated unique short code
 * the original URL. it tracks a when it was created,
 * an expiration date, and a whenther it is active or not
 */

// Entity marks this as a persistent database entity (ORM)
@Entity
// Table says what table this maps to
@Table(name = "links")
public class Link {
    // declares primary key and makes UUID
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Database columns
    @Column(name = "original_url", nullable = false, columnDefinition = "text")
    private String originalUrl;

    @Column(name = "short_code", nullable = false, length = 16)
    private String shortCode;

    @Column(name = "expiresAt")
    private Instant expiresAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Required by JPA
    protected Link() {
    }

    public Link(String originalUrl, String shortCode, Instant expiresAt) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.expiresAt = expiresAt;
    }

    // /**
    // * Constructor not "necessary", but can be useful for testing.
    // * one above is actually used in production
    // */
    // public Link(UUID id, String originalUrl, String shortCode, Instant expiresAt,
    // boolean active, Instant createdAt) {
    // this.id = id;
    // this.originalUrl = originalUrl;
    // this.shortCode = shortCode;
    // this.expiresAt = expiresAt;
    // this.active = active;
    // this.createdAt = createdAt;
    // }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
