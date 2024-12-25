package com.blize.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.OffsetDateTime;

@Data
@Document(collation = "story")
public class Story {

    @Id
    private String id;
    private int from;
    private String rootPath;
    private String path;
    private String fileName;
    private OffsetDateTime date;
}
