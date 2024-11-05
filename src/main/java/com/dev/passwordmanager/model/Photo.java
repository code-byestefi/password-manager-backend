package com.dev.passwordmanager.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Blob;

@Data
@Entity
@Table(name = "photos")
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private Blob image;

    private String fileName;
    private String fileType;

    @OneToOne(mappedBy = "photo")
    private User user;
}