package com.dotnt.booking_travel_backend.menu.repository;

import com.dotnt.booking_travel_backend.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
}
