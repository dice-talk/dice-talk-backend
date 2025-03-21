package com.example.dice_talk.item.repository;

import com.example.dice_talk.item.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
