package com.helphub.backend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

    private final CloudinaryProperties cloudinaryProperties;

    @Bean
    public Cloudinary cloudinary() {
        if (hasExplicitCredentials()) {
            Map<String, String> config = ObjectUtils.asMap(
                    "cloud_name", cloudinaryProperties.getCloudName().trim(),
                    "api_key", cloudinaryProperties.getApiKey().trim(),
                    "api_secret", cloudinaryProperties.getApiSecret().trim(),
                    "secure", true);

            return new Cloudinary(config);
        }

        if (StringUtils.hasText(cloudinaryProperties.getUrl())) {
            return new Cloudinary(cloudinaryProperties.getUrl().trim());
        }

        return new Cloudinary(ObjectUtils.emptyMap());
    }

    private boolean hasExplicitCredentials() {
        return StringUtils.hasText(cloudinaryProperties.getCloudName())
                && StringUtils.hasText(cloudinaryProperties.getApiKey())
                && StringUtils.hasText(cloudinaryProperties.getApiSecret());
    }
}
