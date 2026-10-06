package com.diegopisciotta.shortener;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ShortCodeGeneratorTest {
    // The returned length matches the argument
    @Test
    void randomCodeReturnsRequestedLength() {
        String code = ShortCodeGenerator.randomShortCode(7);

        assertThat(code).hasSize(7);
    }

    // Every character is in the alphabet
    @Test
    void randomCodeUsesOnlyAlnums() {
        String code = ShortCodeGenerator.randomShortCode(1_000);

        assertThat(code).matches("[0-9A-Za-z]+");
    }

    // 10,000 generated codes are all distinct
    @Test
    void allGeneratedCodesAreDistinct() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            codes.add(ShortCodeGenerator.randomShortCode(7));
        }
        assertThat(codes).hasSize(10_000);
    }

    // Quiet fixes
    // zero
    @Test
    void randomCodeFixesQuietlyInvalidSizeZero() {
        String code = ShortCodeGenerator.randomShortCode(0);
        assertThat(code).hasSize(7);
    }

    // Less than zero
    @Test
    void randomCodeFixesQuietlyInvalidSizeNegativeNumber() {
        String code = ShortCodeGenerator.randomShortCode(-1);
        assertThat(code).hasSize(7);
    }

    // Greater than 16
    @Test
    void randomCodeFixesQuietlyClamp() {
        String code = ShortCodeGenerator.randomShortCode(17);
        assertThat(code).hasSize(16);
    }
}
