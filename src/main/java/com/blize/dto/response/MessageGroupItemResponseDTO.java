package com.blize.dto.response;

import com.blize.entity.User;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class MessageGroupItemResponseDTO {
    private User from;
    private User to;
    private String text;
    private OffsetDateTime date;
    private boolean read;
    private int unReadCount;
}
