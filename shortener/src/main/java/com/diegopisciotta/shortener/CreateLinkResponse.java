package com.diegopisciotta.shortener;

/**
 * Data Transfer Object (DTO) representing the API response after creating a
 * link.
 *
 * @param shortCode the unique generated identifier (e.g., "8iNZgPv")
 * @param shortUrl  the fully qualified destination URL (e.g.,
 *                  "http://localhost:8080/8iNZgPv")
 *
 *                  record tells the Java compiler to auto-generate toString(),
 *                  equals(), and other java methods
 */
public record CreateLinkResponse(String shortCode, String shortUrl) {
}
