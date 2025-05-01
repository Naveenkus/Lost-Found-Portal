package com.example.lostoria.repository;

import com.example.lostoria.model.FoundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
}
