package kz.ask.identity.authorization.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import kz.ask.identity.authorization.domain.enums.Role;

@Converter
public class AppUserRoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }
        return switch (role) {
            case SUPER_ADMIN -> "PLATFORM_SUPER_ADMIN";
            case ADMIN -> "PLATFORM_ADMIN";
            case MODERATOR -> "PLATFORM_MODERATOR";
            case OWNER -> "BUSINESS_OWNER";
            case MANAGER -> "BUSINESS_MANAGER";
            case WORKER -> "BUSINESS_WORKER";
            case CUSTOMER -> "CUSTOMER";
        };
    }

    @Override
    public Role convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        return switch (value) {
            case "PLATFORM_SUPER_ADMIN" -> Role.SUPER_ADMIN;
            case "PLATFORM_ADMIN" -> Role.ADMIN;
            case "PLATFORM_MODERATOR" -> Role.MODERATOR;
            case "BUSINESS_OWNER" -> Role.OWNER;
            case "BUSINESS_MANAGER" -> Role.MANAGER;
            case "BUSINESS_WORKER" -> Role.WORKER;
            case "CUSTOMER" -> Role.CUSTOMER;
            default -> throw new IllegalArgumentException("Unknown app user role: " + value);
        };
    }
}
