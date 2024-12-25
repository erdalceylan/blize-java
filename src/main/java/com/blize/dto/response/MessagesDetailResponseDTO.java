package com.blize.dto.response;

import com.blize.entity.User;
import lombok.Data;

import java.util.List;

@Data
public class MessagesDetailResponseDTO {
    private User to;
    private List<MessageResponseDTO> messages;
}
