package com.novamarket.repository;

import com.novamarket.model.Order;
import com.novamarket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    Optional<Order> findByOrderNumber(String orderNumber);
    Optional<Order> findByIdAndUser(Long id, User user);
}