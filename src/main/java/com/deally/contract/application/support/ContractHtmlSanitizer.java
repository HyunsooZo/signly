package com.deally.contract.application.support;

import java.util.regex.Pattern;

public final class ContractHtmlSanitizer {

    private static final Pattern DOCTYPE_PATTERN = Pattern.compile("(?is)<!DOCTYPE[^>]*>");
    private static final Pattern META_PATTERN = Pattern.compile("(?is)<meta[^>]*>");
    private static final Pattern TITLE_PATTERN = Pattern.compile("(?is)<title[^>]*>.*?</title>");
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(?is)</?html[^>]*>");
    private static final Pattern HEAD_TAG_PATTERN = Pattern.compile("(?is)</?head[^>]*>");
    private static final Pattern BODY_TAG_PATTERN = Pattern.compile("(?is)</?body[^>]*>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("(?is)<script[^>]*>.*?</script>");

    private ContractHtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null) {
            return "";
        }

        String sanitized = html
                .replace("\uFEFF", "")
                .trim();

        sanitized = DOCTYPE_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = META_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = TITLE_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = HEAD_TAG_PATTERN.matcher(sanitized).replaceAll("");
        sanitized = BODY_TAG_PATTERN.matcher(sanitized).replaceAll("");

        return sanitized.trim();
    }
}
