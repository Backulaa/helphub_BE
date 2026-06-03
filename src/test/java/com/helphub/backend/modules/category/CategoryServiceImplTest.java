package com.helphub.backend.modules.category;

import com.helphub.backend.common.exception.BadRequestException;
import com.helphub.backend.common.exception.ResourceNotFoundException;
import com.helphub.backend.modules.category.dto.request.CreateCategoryRequest;
import com.helphub.backend.modules.category.dto.request.UpdateCategoryRequest;
import com.helphub.backend.modules.category.dto.request.UpdateCategoryStatusRequest;
import com.helphub.backend.modules.category.dto.response.CategoryDetailResponse;
import com.helphub.backend.modules.category.dto.response.CategorySummaryResponse;
import com.helphub.backend.persistence.entity.Category;
import com.helphub.backend.persistence.repository.CategoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UUID categoryId;
    private Category category;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        category = createCategory(
                categoryId,
                "Medical",
                "MEDICAL",
                "Medical support",
                "https://example.com/medical.png",
                true);
    }

    @SuppressWarnings("null")
    @Test
    void createCategory_success_shouldCreateCategory() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("  Education  ");
        request.setCode(" education ");
        request.setDescription("  Education support  ");
        request.setIconUrl("  https://example.com/education.png  ");

        CategoryDetailResponse expectedResponse = CategoryDetailResponse.builder()
                .id(UUID.randomUUID())
                .name("Education")
                .code("EDUCATION")
                .description("Education support")
                .iconUrl("https://example.com/education.png")
                .isActive(true)
                .build();

        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(categoryMapper.toDetailResponse(any(Category.class))).thenReturn(expectedResponse);

        CategoryDetailResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("Education", response.getName());
        assertEquals("EDUCATION", response.getCode());
        assertTrue(response.getIsActive());

        verify(categoryRepository).findAll();
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toDetailResponse(any(Category.class));
    }