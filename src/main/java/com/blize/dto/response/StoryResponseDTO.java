package com.blize.dto.response;

import com.blize.entity.User;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class StoryResponseDTO {

    private String id;
    private User from;
    private String rootPath;
    private String path;
    private String fileName;
    private OffsetDateTime date;
}
