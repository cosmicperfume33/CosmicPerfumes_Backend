package com.example.ForgeX.model;

import java.util.Locale;

import com.example.ForgeX.exceptions.APIException;

public enum BannerTheme {

    LIGHT,
    DARK;

    public static BannerTheme from(String value, BannerTheme fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new APIException("Theme must be LIGHT or DARK.");
        }
    }
}
