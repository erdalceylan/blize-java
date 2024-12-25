package com.blize.dto.response;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class StoryGroupItemResponseDTO {
    private String id;
    private OffsetDateTime date;
    private String fileName;
    private String path;
    private String rootPath;
    private Boolean seen;
}
