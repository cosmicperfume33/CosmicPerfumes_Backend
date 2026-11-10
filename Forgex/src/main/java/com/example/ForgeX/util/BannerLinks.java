package com.example.ForgeX.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import com.example.ForgeX.exceptions.APIException;

public final class BannerLinks {

    private static final String MESSAGE = "Enter a full https:// address or a path starting with /.";

    private BannerLinks() {
    }

    public static String normalise(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.chars().anyMatch(c -> c < 0x20 || c == 0x7f)) {
            throw new APIException(MESSAGE);
        }
        if (value.length() > 500) {
            throw new APIException("Keep the link to 500 characters or fewer.");
        }

        String lower = value.toLowerCase(Locale.ROOT);

        if (lower.startsWith("//")) {
            throw new APIException(MESSAGE);
        }
        if (value.startsWith("/")) {
            return value;
        }
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            try {
                URI uri = new URI(value);
                if (uri.getHost() == null || uri.getHost().isBlank()) {
                    throw new APIException(MESSAGE);
                }
            } catch (URISyntaxException e) {
                throw new APIException(MESSAGE);
            }
            return value;
        }
        throw new APIException(MESSAGE);
    }
}
