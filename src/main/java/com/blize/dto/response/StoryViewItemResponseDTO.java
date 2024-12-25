package com.blize.dto.response;

import com.blize.entity.User;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class StoryViewItemResponseDTO {
    private User user;
    private OffsetDateTime date;
}
