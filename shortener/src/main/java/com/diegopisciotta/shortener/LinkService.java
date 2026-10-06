package com.diegopisciotta.shortener;

import java.time.Instant;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class LinkService {

    private static final int CODE_LENGTH = 7;
    private static final int MAX_ATTEMPTS = 5;

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    /**
     * Creates the new Link entity with the randomly generated short code
     * it attemps to write to the database and if it fails it tries again
     * a failure indicates there is already an entry because the DB schema ensures
     * uniqness
     * on the off chance there is a collision it trys again
     * attemps are capped at {@value #MAX_ATTEMPTS} tries so the user isn't waiting
     * ages
     * if many collisions happen to occur throws IllegalStateException
     */
    public Link create(String originalUrl, Instant expiresAt) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String code = ShortCodeGenerator.randomShortCode(CODE_LENGTH);
            try {
                return linkRepository.saveAndFlush(new Link(originalUrl, code, expiresAt));
            } catch (DataIntegrityViolationException e) {
                // Most likely a short code collision, so loop and try a new code
            }
        }
        throw new IllegalStateException(
                "Could not generate a unique short code after " + MAX_ATTEMPTS + " attempts");
    }

    // Optional safely handles database lookup results, avoiding
    // NullPointerException if the short code does not exist.
    public Optional<Link> resolve(String code) {
        Optional<Link> found = linkRepository.findByShortCode(code);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        Link link = found.get();
        if (!link.isActive()) {
            return Optional.empty();
        }
        if (link.getExpiresAt() != null && !link.getExpiresAt().isAfter(Instant.now())) {
            return Optional.empty();
        }
        return found;
    }
}
