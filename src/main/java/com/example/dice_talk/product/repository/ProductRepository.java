package com.example.dice_talk.product.repository;

import com.example.dice_talk.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
