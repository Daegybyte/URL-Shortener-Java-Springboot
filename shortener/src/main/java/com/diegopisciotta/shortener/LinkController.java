package com.diegopisciotta.shortener;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
public class LinkController {

    private final LinkService linkService;
    private final String baseUrl;

    public LinkController(
            LinkService linkService,
            @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.linkService = linkService;
        this.baseUrl = baseUrl;
    }

    @PostMapping("/api/links")
    public ResponseEntity<CreateLinkResponse> create(
            @Valid @RequestBody CreateLinkRequest request) {
        Link link = linkService.create(request.url(), request.expiresAt());
        String shortUrl = baseUrl + "/" + link.getShortCode();

        // 201 Created
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateLinkResponse(link.getShortCode(), shortUrl));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        Optional<Link> found = linkService.resolve(shortCode);

        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // Returns FOUND -> 302 server side redirect for analytics
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(found.get().getOriginalUrl()))
                .build();
    }
}
