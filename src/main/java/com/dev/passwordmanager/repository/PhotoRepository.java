package com.dev.passwordmanager.repository;

import com.dev.passwordmanager.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
}