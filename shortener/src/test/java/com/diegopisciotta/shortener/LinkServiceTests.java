package com.diegopisciotta.shortener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Mockito creates a dummy instance to test business logic and database
 * interactions
 * without needing to spin up a full Spring application context or running
 * database.
 */
@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    /** Must match MAX_ATTEMPTS in {@link LinkService}. */
    private static final int MAX_ATTEMPTS = 5;

    /** Fake repository. Returns default values unless a test stubs it. */
    @Mock
    private LinkRepository linkRepository;

    /** Real service, created by Mockito with the mock repository passed in. */
    @InjectMocks
    private LinkService linkService;

    @Test
    void createRetriesAfterCollision() {
        Link saved = new Link("https://diegopisciotta.com", "abc1234", null);
        when(linkRepository.saveAndFlush(any(Link.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"))
                .thenReturn(saved);

        Link result = linkService.create("https://diegopisciotta.com", null);

        assertThat(result).isSameAs(saved);
        verify(linkRepository, times(2)).saveAndFlush(any(Link.class));
    }

    @Test
    void createThrowsWhenMaxAttemptsExceeded() {
        // A single thenThrow repeats on every call, so every attempt fails
        when(linkRepository.saveAndFlush(any(Link.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        try {
            linkService.create("https://diegopisciotta.com", null);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("unique short code");
        }

        verify(linkRepository, times(MAX_ATTEMPTS)).saveAndFlush(any(Link.class));
    }

    // resolve

    @Test
    void resolveReturnsLinkWhenActiveWithNoExpiry() {
        Link link = new Link("https://diegopisciotta.com", "abc1234", null);
        when(linkRepository.findByShortCode("abc1234")).thenReturn(Optional.of(link));

        Optional<Link> result = linkService.resolve("abc1234");

        assertThat(result).contains(link);
    }

    @Test
    void resolveReturnsLinkWhenExpiryIsInTheFuture() {
        Instant tomorrow = Instant.now().plusSeconds(86_400);
        Link link = new Link("https://diegopisciotta.com", "abc1234", tomorrow);
        when(linkRepository.findByShortCode("abc1234")).thenReturn(Optional.of(link));

        Optional<Link> result = linkService.resolve("abc1234");

        assertThat(result).contains(link);
    }

    @Test
    void resolveReturnsEmptyWhenCodeNotFound() {
        when(linkRepository.findByShortCode("missing")).thenReturn(Optional.empty());

        Optional<Link> result = linkService.resolve("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void resolveReturnsEmptyWhenLinkIsInactive() {
        Link link = new Link("https://diegopisciotta.com", "abc1234", null);
        link.deactivate();
        when(linkRepository.findByShortCode("abc1234")).thenReturn(Optional.of(link));

        Optional<Link> result = linkService.resolve("abc1234");

        assertThat(result).isEmpty();
    }

    @Test
    void resolveReturnsEmptyWhenLinkIsExpired() {
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        Link link = new Link("https://diegopisciotta.com", "abc1234", oneHourAgo);
        when(linkRepository.findByShortCode("abc1234")).thenReturn(Optional.of(link));

        Optional<Link> result = linkService.resolve("abc1234");

        assertThat(result).isEmpty();
    }
}
