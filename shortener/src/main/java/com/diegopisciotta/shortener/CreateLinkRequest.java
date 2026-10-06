package com.diegopisciotta.shortener;

import java.time.Instant;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Target web address to shorten.
 * Must be a non-blank HTTP or HTTPS URL under 2,048 characters.
 */
public record CreateLinkRequest(
                @NotBlank @Size(max = 2048) @Pattern(regexp = "^https?://\\S+$", message = "must be an http or https URL") String url,

                @Future Instant expiresAt) {
}
