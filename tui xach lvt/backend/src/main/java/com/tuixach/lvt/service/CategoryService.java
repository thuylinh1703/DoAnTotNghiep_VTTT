package com.tuixach.lvt.service;

import com.tuixach.lvt.dto.CategoryDTO;
import com.tuixach.lvt.dto.CategoryRequest;
import com.tuixach.lvt.entity.Category;
import com.tuixach.lvt.exception.BadRequestException;
import com.tuixach.lvt.exception.ResourceNotFoundException;
import com.tuixach.lvt.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getActiveCategories() {
        return categoryRepository.findByActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        return mapToDTO(category);
    }

    public CategoryDTO createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Tên danh mục đã tồn tại");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .image(request.getImage())
                .active(true)
                .build();

        categoryRepository.save(category);
        return mapToDTO(category);
    }

    public CategoryDTO updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getImage() != null) {
            category.setImage(request.getImage());
        }

        categoryRepository.save(category);
        return mapToDTO(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        category.setActive(false);
        categoryRepository.save(category);
    }

    private CategoryDTO mapToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .image(category.getImage())
                .active(category.isActive())
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .build();
    }
}
