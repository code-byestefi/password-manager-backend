package com.dev.passwordmanager.service;

import com.dev.passwordmanager.dto.category.CategoryDTO;
import com.dev.passwordmanager.exception.ResourceNotFoundException;
import com.dev.passwordmanager.model.Category;
import com.dev.passwordmanager.model.User;
import com.dev.passwordmanager.repository.CategoryRepository;
import com.dev.passwordmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setUser(user);

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    public List<CategoryDTO> getUserCategories(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return categoryRepository.findByUserId(user.getId())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO, String userEmail) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        // Verificar que la categoría pertenece al usuario
        if (!category.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("No autorizado para modificar esta categoría");
        }

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());

        category = categoryRepository.save(category);
        return convertToDTO(category);
    }

    @Transactional
    public void deleteCategory(Long id, String userEmail) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        if (!category.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("No autorizado para eliminar esta categoría");
        }

        categoryRepository.delete(category);
    }

    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        return dto;
    }
}
