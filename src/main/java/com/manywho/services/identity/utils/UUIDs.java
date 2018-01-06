package com.manywho.services.identity.utils;

/**
 * TODO: Remove this class once sdk-beta-03 is released
 */
public class UUIDs {
    public static boolean isValid(String value) {
        return value != null && value.trim().matches("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
    }
}
