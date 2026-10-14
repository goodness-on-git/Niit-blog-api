package com.Niit_blog_api.common;

import com.Niit_blog_api.user.User;

public record UserSummary(Long id, String username, String displayName) {

    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getUsername(), user.getDisplayName());
    }
}