package com.dev.passwordmanager.service;

import com.dev.passwordmanager.exception.ResourceNotFoundException;
import com.dev.passwordmanager.model.Photo;
import com.dev.passwordmanager.model.User;
import com.dev.passwordmanager.repository.PhotoRepository;
import com.dev.passwordmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    @Transactional
    public Photo savePhoto(MultipartFile file, String userEmail) throws IOException, SQLException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Photo photo = new Photo();
        if (file != null && !file.isEmpty()) {
            byte[] photoBytes = file.getBytes();
            Blob photoBlob = new SerialBlob(photoBytes);
            photo.setImage(photoBlob);
            photo.setFileType(file.getContentType());
            photo.setFileName(file.getOriginalFilename());
        }

        Photo savedPhoto = photoRepository.save(photo);
        user.setPhoto(savedPhoto);
        userRepository.save(user);

        return savedPhoto;
    }

    public byte[] getImageData(Long id) throws SQLException {
        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada"));

        Blob photoBlob = photo.getImage();
        int blobLength = (int) photoBlob.length();
        return photoBlob.getBytes(1, blobLength);
    }

    @Transactional
    public void updateUserPhoto(MultipartFile file, String userEmail) throws IOException, SQLException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Photo photo = user.getPhoto();
        if (photo == null) {
            photo = new Photo();
        }

        byte[] photoBytes = file.getBytes();
        Blob photoBlob = new SerialBlob(photoBytes);
        photo.setImage(photoBlob);
        photo.setFileType(file.getContentType());
        photo.setFileName(file.getOriginalFilename());

        photoRepository.save(photo);
        user.setPhoto(photo);
        userRepository.save(user);
    }

    public byte[] getUserPhoto(String userEmail) throws SQLException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getPhoto() == null) {
            return null;
        }

        Photo photo = user.getPhoto();
        Blob photoBlob = photo.getImage();
        int blobLength = (int) photoBlob.length();
        return photoBlob.getBytes(1, blobLength);
    }

    @Transactional
    public void deletePhoto(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (user.getPhoto() != null) {
            Long photoId = user.getPhoto().getId();
            user.removeUserPhoto();
            userRepository.save(user);
            photoRepository.deleteById(photoId);
        }
    }
}
