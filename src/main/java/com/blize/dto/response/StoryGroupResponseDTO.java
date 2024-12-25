package com.blize.dto.response;

import com.blize.entity.User;
import lombok.Data;
import java.util.List;

@Data
public class StoryGroupResponseDTO {

    private User user;

    private List<StoryGroupItemResponseDTO> items;
}
