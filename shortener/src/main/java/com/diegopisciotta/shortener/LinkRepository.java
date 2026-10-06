package com.diegopisciotta.shortener;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * This interface interacts with the database and is a wrapper allowing for
 * CRUD queries to be performed with Java
 * Looks up a link by its short code
 * It basically runs this SQL *
 * {@code SELECT * FROM links WHERE short_code = ?;}
 */
public interface LinkRepository extends JpaRepository<Link, UUID> {

    Optional<Link> findByShortCode(String shortCode);
}
