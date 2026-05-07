package com.tuixach.lvt.service;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Service;

@Service
public class HtmlSanitizerService {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "br", "strong", "em", "u", "h2", "h3", "h4",
                    "ul", "ol", "li", "blockquote", "a", "img")
            .allowAttributes("href", "target", "rel").onElements("a")
            .allowUrlProtocols("http", "https")
            .allowAttributes("src", "alt", "width", "height").onElements("img")
            .allowStandardUrlProtocols()
            .toFactory();

    public String sanitize(String html) {
        if (html == null) {
            return null;
        }
        return POLICY.sanitize(html);
    }
}
