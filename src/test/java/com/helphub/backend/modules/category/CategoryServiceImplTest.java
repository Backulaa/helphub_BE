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