package com.example.ForgeX.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import com.example.ForgeX.exceptions.APIException;

public enum BannerPlacement {

    HERO(false),
    SPLIT_ATTAR(true),
    SPLIT_PERFUME(true),
    CATEGORY(false),
    GIFTING(true),
    BUILD_YOUR_BOX(true),
    FRAGRANCE_DISCOVERY(true),
    JOURNAL(false),
    PROMO_STRIP(false);

    private final boolean singleSlot;

    BannerPlacement(boolean singleSlot) {
        this.singleSlot = singleSlot;
    }

    public boolean isSingleSlot() {
        return singleSlot;
    }

    public static BannerPlacement from(String value) {
        if (value == null || value.isBlank()) {
            throw new APIException("Choose where this banner belongs. Use one of " + names() + ".");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new APIException("There is no placement called " + value.trim() + ". Use one of " + names() + ".");
        }
    }

    public static String names() {
        return Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", "));
    }
}
