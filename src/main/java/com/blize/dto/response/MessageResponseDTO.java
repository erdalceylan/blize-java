package com.blize.dto.response;

import com.blize.entity.User;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.OffsetDateTime;

@Data
public class MessageResponseDTO {

    @Id
    private String id;
    private User from;
    private User to;
    private String text;
    private OffsetDateTime date;
    private boolean read;
}