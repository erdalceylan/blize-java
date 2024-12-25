package com.blize.dto.response;

import com.blize.entity.User;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CallResponseDTO {
    private String id;
    private User from;
    private User to;
    private boolean video;
    private OffsetDateTime date;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
