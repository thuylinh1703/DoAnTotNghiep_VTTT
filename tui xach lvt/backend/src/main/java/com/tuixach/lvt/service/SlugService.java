package com.tuixach.lvt.service;

import com.tuixach.lvt.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SlugService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private final BlogPostRepository blogPostRepository;

    public String generateUniqueSlug(String input, Long currentId) {
        String base = toSlug(input);
        if (base.isBlank()) {
            base = "bai-viet";
        }

        String candidate = base;
        int suffix = 2;
        while (slugExists(candidate, currentId)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private boolean slugExists(String slug, Long currentId) {
        return blogPostRepository.findBySlug(slug)
                .map(post -> !post.getId().equals(currentId))
                .orElse(false);
    }

    private String toSlug(String input) {
        if (input == null) {
            return "";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');

        String slug = WHITESPACE.matcher(normalized).replaceAll("-");
        slug = NONLATIN.matcher(slug).replaceAll("");
        slug = slug.replaceAll("-+", "-").replaceAll("^-|-$", "");
        return slug.toLowerCase(Locale.ROOT);
    }
}
