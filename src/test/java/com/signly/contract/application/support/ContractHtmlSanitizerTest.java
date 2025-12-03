package com.signly.contract.application.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContractHtmlSanitizerTest {

    @Test
    void sanitize_removesDocumentHeadersWhilePreservingBody() {
        String raw = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>표준근로계약서</title></head><body><div class=\"title\">표준근로계약서</div></body></html>";

        String sanitized = ContractHtmlSanitizer.sanitize(raw);

        assertThat(sanitized).doesNotContain("<!DOCTYPE")
                .doesNotContain("<meta")
                .doesNotContain("<title")
                .doesNotContain("<html")
                .doesNotContain("<body");
        assertThat(sanitized).contains("<div class=\"title\">표준근로계약서</div>");
    }
}
