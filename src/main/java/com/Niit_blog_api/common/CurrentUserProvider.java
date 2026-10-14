package com.Niit_blog_api.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/** Temporary: reads the user id from the X-User-Id header. Replaced by JWT in the security lesson. */
@Component
public class CurrentUserProvider {

    private static final String HEADER = "X-User-Id";

    private final HttpServletRequest request;

    public CurrentUserProvider(HttpServletRequest request) {
        this.request = request;
    }

    public Long requireUserId() {
        String value = request.getHeader(HEADER);
        if (value == null || value.isBlank()) {
            throw new UnauthorizedException("No user identified. Send the X-User-Id header.");
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("X-User-Id must be a number");
        }
    }
}