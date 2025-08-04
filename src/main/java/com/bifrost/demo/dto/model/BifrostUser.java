package com.bifrost.demo.dto.model;

import java.util.Optional;
import java.util.Set;

public record BifrostUser(String username, Role role, String token) {

    public enum Role {
        SUPERADMIN,
        ADMIN,
        TEMPORARY_ADMIN,
        LV0_USER;

        private static final Set<Role> REGISTER_TOKEN_ROLES = Set.of(
                TEMPORARY_ADMIN,
                LV0_USER
        );

        public static Optional<Role> fromString(String value) {
            try {
                return Optional.of(Role.valueOf(value));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }

        public static Optional<Role> validateRegisterInput(String value) {
            try {
                Role parsed = Role.valueOf(value);
                return REGISTER_TOKEN_ROLES.contains(parsed) ? Optional.of(parsed) : Optional.empty();
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        }
    }
}
