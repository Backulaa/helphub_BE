package com.helphub.backend.bootstrap;

import com.helphub.backend.persistence.entity.Category;
import com.helphub.backend.persistence.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CategorySeeder implements CommandLineRunner {

        private final CategoryRepository categoryRepository;

        @Override
        public void run(String... args) {

                List<CategorySeedData> categories = List.of(
                                new CategorySeedData(
                                                "Medical",
                                                "MEDICAL",
                                                "Medical assistance and healthcare support",
                                                "https://cdn.helphub.com/icons/medical.png"),

                                new CategorySeedData(
                                                "Food",
                                                "FOOD",
                                                "Food and essential supplies support",
                                                "https://cdn.helphub.com/icons/food.png"),

                                new CategorySeedData(
                                                "Living",
                                                "LIVING",
                                                "Daily living and household support",
                                                "https://cdn.helphub.com/icons/living.png"),

                                new CategorySeedData(
                                                "Education",
                                                "EDUCATION",
                                                "Education and learning support",
                                                "https://cdn.helphub.com/icons/education.png"),

                                new CategorySeedData(
                                                "Job",
                                                "JOB",
                                                "Employment and career support",
                                                "https://cdn.helphub.com/icons/job.png"),

                                new CategorySeedData(
                                                "Housing",
                                                "HOUSING",
                                                "Housing and accommodation support",
                                                "https://cdn.helphub.com/icons/housing.png"),

                                new CategorySeedData(
                                                "Legal",
                                                "LEGAL",
                                                "Legal consultation and assistance",
                                                "https://cdn.helphub.com/icons/legal.png"),

                                new CategorySeedData(
                                                "Emergency",
                                                "EMERGENCY",
                                                "Emergency and disaster relief support",
                                                "https://cdn.helphub.com/icons/emergency.png"));

                for (CategorySeedData categoryData : categories) {

                        boolean codeExists = categoryRepository
                                        .existsByCodeIgnoreCase(categoryData.code());

                        boolean nameExists = categoryRepository
                                        .existsByNameIgnoreCase(categoryData.name());

                        if (codeExists || nameExists) {
                                continue;
                        }

                        Category category = Category.builder()
                                        .name(categoryData.name())
                                        .code(categoryData.code())
                                        .description(categoryData.description())
                                        .iconUrl(categoryData.iconUrl())
                                        .isActive(true)
                                        .build();

                        categoryRepository.save(Objects.requireNonNull(category));
                }
        }

        private record CategorySeedData(
                        String name,
                        String code,
                        String description,
                        String iconUrl) {
        }
}
