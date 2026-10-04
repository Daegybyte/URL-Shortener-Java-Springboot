package com.diegopisciotta.shortener;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ShortCodeGeneratorTest {
    // The returned length matches the argument
    @Test
    void randomCodeReturnsRequestedLength() {
        String code = ShortCodeGenerator.randomCode(7);

        assertThat(code).hasSize(7);
    }

    // Every character is in the alphabet
    @Test
    void randomCodeUsesOnlyAlnums() {
        String code = ShortCodeGenerator.randomCode(1_000);

        assertThat(code).matches("[0-9A-Za-z]+");
    }

    // 10,000 generated codes are all distinct
    @Test
    void allGeneratedCodesAreDistinct() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            codes.add(ShortCodeGenerator.randomCode(7));
        }
        assertThat(codes).hasSize(10_000);
    }

    // length <= 0 quietly fixes to default
    @Test
    void randomCodeFixesQuietlyInvalidSizeZero() {
        String code = ShortCodeGenerator.randomCode(0);
        assertThat(code).hasSize(7);
    }

    @Test
    void randomCodeFixesQuietlyInvalidSizeNegativeNumber() {
        String code = ShortCodeGenerator.randomCode(-1);
        assertThat(code).hasSize(7);
    }

    @Test
    void randomCodeFixesQuietlyClamp() {
        String code = ShortCodeGenerator.randomCode(17);
        assertThat(code).hasSize(16);
    }
}
