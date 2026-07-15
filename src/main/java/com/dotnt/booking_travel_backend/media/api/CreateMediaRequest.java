package com.dotnt.booking_travel_backend.media.api;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMediaRequest {
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private Integer width;
    private Integer height;
}
