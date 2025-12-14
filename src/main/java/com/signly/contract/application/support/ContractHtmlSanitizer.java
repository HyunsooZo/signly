package com.signly.contract.application.support;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

@Slf4j
public final class ContractHtmlSanitizer {

    private static final Safelist CONTRACT_HTML_WHITELIST = Safelist.relaxed()
            .removeTags("script", "iframe", "object", "embed", "form", "input", "button")
            .removeAttributes("a", "href", "onclick", "onload", "onerror")
            .removeAttributes("img", "src", "onerror", "onload", "onclick")
            .removeAttributes("*", "onclick", "onload", "onerror", "onmouseover", "onmouseout", 
                           "onfocus", "onblur", "onchange", "onsubmit", "onreset", "onkeydown", 
                           "onkeyup", "onkeypress", "onmousedown", "onmouseup", "onmousemove", 
                           "onmouseenter", "onmouseleave", "ondblclick", "oncontextmenu")
            .addAttributes("a", "href")
            .addAttributes("img", "src", "alt", "width", "height")
            .addProtocols("a", "href", "http", "https", "mailto")
            .addProtocols("img", "src", "http", "https", "data");

    private ContractHtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null) {
            return "";
        }

        try {
            String cleaned = html.replace("\uFEFF", "").trim();
            
            String sanitized = Jsoup.clean(cleaned, CONTRACT_HTML_WHITELIST);
            
            if (!sanitized.equals(cleaned)) {
                log.debug("HTML content sanitized for security reasons");
            }
            
            return sanitized.trim();
            
        } catch (Exception e) {
            log.error("Failed to sanitize HTML content", e);
            return "";
        }
    }
    
    public static boolean isSafe(String html) {
        if (html == null) {
            return true;
        }
        
        try {
            String sanitized = sanitize(html);
            return sanitized.equals(html.replace("\uFEFF", "").trim());
        } catch (Exception e) {
            log.error("Failed to check HTML safety", e);
            return false;
        }
    }
}
