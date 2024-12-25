package com.blize.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.OffsetDateTime;

@Data
@Document(collection = "message")
public class Message {

    @Id
    private String id;
    private int from;
    private int to;
    private String text;
    private OffsetDateTime date;
    private boolean read;
}