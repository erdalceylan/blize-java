package com.blize.dto.response;

import com.blize.entity.User;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class StoryMeItemResponseDTO {
    private String id;
    private User user;
    private OffsetDateTime date;
    private String fileName;
    private String path;
    private String rootPath;
    private List<StoryViewItemResponseDTO> views;
    private int viewsLength;
}
