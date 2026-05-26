package com.example.sprintsight.exceptions;

import org.hibernate.exception.ConstraintViolationException;

import java.util.Map;

public final class ConstraintViolationTranslator {
    private static final Map<String, String> MESSAGES = Map.ofEntries(
            // Users
            Map.entry("users_username_key",                     "Username already exists"),
            Map.entry("users_email_key",                        "Email already exists"),

            // Projects
            Map.entry("sprints_project_name_key",               "A sprint with this name already exists in this project"),
            Map.entry("components_project_name_key",            "A component with this name already exists in this project"),

            // Issue configuration
            Map.entry("issue_type_project_name_key",            "An issue type with this name already exists in this project"),
            Map.entry("issue_priority_project_name_key",        "A priority with this name already exists in this project"),
            Map.entry("issue_status_project_name_key",          "A status with this name already exists in this project"),

            // Defaults — single-row-per-project enforced by partial unique index
            Map.entry("issue_type_single_default_per_project",     "Only one issue type can be marked as default"),
            Map.entry("issue_priority_single_default_per_project", "Only one priority can be marked as default"),
            Map.entry("issue_status_single_default_per_project",   "Only one status can be marked as default"),

            // Sprint membership
            Map.entry("sprint_issues_active_unique",            "Issue is already in this sprint"),

            // Invitations
            Map.entry("invitations_pending_unique",             "A pending invitation already exists for this user")
    );

    private ConstraintViolationTranslator() {}

    public static String translate(Throwable ex) {
        String constraintName = extractConstraintName(ex);

        if (constraintName == null) return null;

        String message = MESSAGES.get(constraintName);

        if (message != null) return message;

        return MESSAGES.entrySet()
                .stream()
                .filter(e -> constraintName.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static String extractConstraintName(Throwable ex) {
        Throwable current = ex;

        while (current != null) {
            if (current instanceof ConstraintViolationException cve) {
                String name = cve.getConstraintName();

                if (name != null && !name.isBlank()) {
                    int dot = name.lastIndexOf('.');

                    return dot >= 0 ? name.substring(dot + 1) : name;
                }
            }

            current = current.getCause();
        }

        assert ex != null;
        String msg = ex.getMessage();

        if (msg != null) {
            for (String key : MESSAGES.keySet()) {
                if (msg.contains(key)) return key;
            }
        }

        return null;
    }
}
