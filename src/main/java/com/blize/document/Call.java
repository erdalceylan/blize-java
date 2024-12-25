package com.blize.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.OffsetDateTime;

@Data
@Document(collection = "call")
public class Call {

    @Id
    private String id;
    private int from;
    private int to;
    private OffsetDateTime date;
    private boolean video;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
