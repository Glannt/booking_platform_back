package com.dotnt.booking_travel_backend.user.api;

import com.dotnt.booking_travel_backend.user.internal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserFacade {
    private final UserRepository userRepository;

    public Long findUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(com.dotnt.booking_travel_backend.user.internal.entity.User::getId)
                .orElse(null);
    }
}
