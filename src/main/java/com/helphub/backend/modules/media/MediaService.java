package com.helphub.backend.modules.media;

import com.helphub.backend.modules.media.dto.request.CreateMediaRequest;
import com.helphub.backend.modules.media.dto.request.UpdateMediaRequest;
import com.helphub.backend.modules.media.dto.response.MediaDetailResponse;
import com.helphub.backend.modules.media.dto.response.MediaSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MediaService {

    MediaDetailResponse createMedia(UUID currentUserId, CreateMediaRequest request);

    MediaDetailResponse uploadMedia(
            UUID currentUserId,
            MultipartFile file,
            String folderName,
            Boolean isPublic,
            String altText);

    String uploadFile(MultipartFile file, String folderName);

    MediaDetailResponse getMediaById(UUID currentUserId, UUID mediaId);

    List<MediaSummaryResponse> getMyMedia(UUID currentUserId);

    MediaDetailResponse updateMedia(UUID userId, UUID mediaId, UpdateMediaRequest request);

    void deleteMedia(UUID currentUserId, UUID mediaId);
}