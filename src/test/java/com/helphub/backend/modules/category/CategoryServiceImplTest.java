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

    @SuppressWarnings("null")
    @Test
    void createCategory_shouldThrowBadRequestException_whenNameExists() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("medical");
        request.setCode("NEW_CODE");

        when(categoryRepository.findAll()).thenReturn(List.of(category));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> categoryService.createCategory(request));

        assertEquals("Category name already exists", exception.getMessage());

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @SuppressWarnings("null")
    @Test
    void createCategory_shouldThrowBadRequestException_whenCodeExists() {
        CreateCategoryRequest request = new CreateCategoryRequest();
        request.setName("New Category");
        request.setCode("medical");

        when(categoryRepository.findAll()).thenReturn(List.of(category));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> categoryService.createCategory(request));

        assertEquals("Category code already exists", exception.getMessage());

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getActiveCategories_success_shouldReturnActiveCategories() {
        CategorySummaryResponse expectedResponse = CategorySummaryResponse.builder()
                .id(categoryId)
                .name("Medical")
                .code("MEDICAL")
                .iconUrl("https://example.com/medical.png")
                .isActive(true)
                .build();

        when(categoryRepository.findAllByIsActiveTrueOrderByCreatedAtDesc())
                .thenReturn(List.of(category));

        when(categoryMapper.toSummaryResponse(category))
                .thenReturn(expectedResponse);

        List<CategorySummaryResponse> response = categoryService.getActiveCategories();

        assertEquals(1, response.size());
        assertEquals(categoryId, response.get(0).getId());
        assertEquals("Medical", response.get(0).getName());

        verify(categoryRepository).findAllByIsActiveTrueOrderByCreatedAtDesc();
        verify(categoryMapper).toSummaryResponse(category);
    }

    @Test
    void getAllCategories_success_shouldReturnAllCategories() {
        Category inactiveCategory = createCategory(
                UUID.randomUUID(),
                "Food",
                "FOOD",
                "Food support",
                "https://example.com/food.png",
                false);

        CategorySummaryResponse activeResponse = CategorySummaryResponse.builder()
                .id(categoryId)
                .name("Medical")
                .code("MEDICAL")
                .isActive(true)
                .build();

        CategorySummaryResponse inactiveResponse = CategorySummaryResponse.builder()
                .id(inactiveCategory.getId())
                .name("Food")
                .code("FOOD")
                .isActive(false)
                .build();

        when(categoryRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(category, inactiveCategory));

        when(categoryMapper.toSummaryResponse(category))
                .thenReturn(activeResponse);

        when(categoryMapper.toSummaryResponse(inactiveCategory))
                .thenReturn(inactiveResponse);

        List<CategorySummaryResponse> response = categoryService.getAllCategories();

        assertEquals(2, response.size());
        assertEquals("Medical", response.get(0).getName());
        assertEquals("Food", response.get(1).getName());

        verify(categoryRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void getCategoryById_success_shouldReturnCategoryDetail() {
        CategoryDetailResponse expectedResponse = CategoryDetailResponse.builder()
                .id(categoryId)
                .name("Medical")
                .code("MEDICAL")
                .description("Medical support")
                .iconUrl("https://example.com/medical.png")
                .isActive(true)
                .build();

        when(categoryRepository.findById(Objects.requireNonNull(categoryId)))
                .thenReturn(Optional.of(category));

        when(categoryMapper.toDetailResponse(category))
                .thenReturn(expectedResponse);

        CategoryDetailResponse response = categoryService.getCategoryById(Objects.requireNonNull(categoryId));

        assertNotNull(response);
        assertEquals(categoryId, response.getId());
        assertEquals("MEDICAL", response.getCode());

        verify(categoryRepository).findById(Objects.requireNonNull(categoryId));
        verify(categoryMapper).toDetailResponse(category);
    }

    @Test
    void getCategoryById_shouldThrowResourceNotFoundException_whenNotFound() {
        when(categoryRepository.findById(Objects.requireNonNull(categoryId)))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(Objects.requireNonNull(categoryId)));

        assertEquals("Category not found with id: " + categoryId, exception.getMessage());

        verify(categoryMapper, never()).toDetailResponse(any(Category.class));
    }

    @Test
    void updateCategory_success_shouldUpdateCategory() {
        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("  Healthcare  ");
        request.setCode(" health care ");
        request.setDescription("  Healthcare support  ");
        request.setIconUrl("  https://example.com/healthcare.png  ");

        CategoryDetailResponse expectedResponse = CategoryDetailResponse.builder()
                .id(categoryId)
                .name("Healthcare")
                .code("HEALTH_CARE")
                .description("Healthcare support")
                .iconUrl("https://example.com/healthcare.png")
                .isActive(true)
                .build();

        when(categoryRepository.findById(Objects.requireNonNull(categoryId)))
                .thenReturn(Optional.of(category));

        when(categoryRepository.findAll())
                .thenReturn(List.of(category));

        when(categoryRepository.save(Objects.requireNonNull(category)))
                .thenReturn(category);

        when(categoryMapper.toDetailResponse(Objects.requireNonNull(category)))
                .thenReturn(expectedResponse);

        CategoryDetailResponse response = categoryService.updateCategory(categoryId, request);

        assertEquals("Healthcare", response.getName());
        assertEquals("HEALTH_CARE", response.getCode());

        assertEquals("Healthcare", category.getName());
        assertEquals("HEALTH_CARE", category.getCode());
        assertEquals("Healthcare support", category.getDescription());
        assertEquals("https://example.com/healthcare.png", category.getIconUrl());

        verify(categoryRepository).save(Objects.requireNonNull(category));
    }

    @SuppressWarnings("null")
    @Test
    void updateCategory_shouldThrowBadRequestException_whenNameExists() {
        Category existingCategory = createCategory(
                UUID.randomUUID(),
                "Food",
                "FOOD",
                "Food support",
                "https://example.com/food.png",
                true);

        UpdateCategoryRequest request = new UpdateCategoryRequest();
        request.setName("food");
        request.setCode("NEW_CODE");

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(categoryRepository.findAll())
                .thenReturn(List.of(category, existingCategory));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> categoryService.updateCategory(categoryId, request));

        assertEquals("Category name already exists", exception.getMessage());

        verify(categoryRepository, never()).save(any(Category.class));
    }