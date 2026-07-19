package com.dotnt.booking_travel_backend.user.internal.repository;

import com.dotnt.booking_travel_backend.user.internal.entity.BlacklistedToken;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {
    Optional<BlacklistedToken> findByToken(String token);

    @Cacheable(value = "blacklistedTokens", key = "#token")
    boolean existsByToken(String token);

    @Override
    @CacheEvict(value = "blacklistedTokens", key = "#entity.token")
    <S extends BlacklistedToken> S save(S entity);
}
