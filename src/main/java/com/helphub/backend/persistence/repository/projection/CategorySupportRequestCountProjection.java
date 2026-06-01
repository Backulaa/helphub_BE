package com.helphub.backend.persistence.repository.projection;

import java.util.UUID;

public interface CategorySupportRequestCountProjection {

    UUID getCategoryId();

    String getCategoryName();

    Long getSupportRequestCount();
}