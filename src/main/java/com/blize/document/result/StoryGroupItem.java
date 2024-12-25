package com.blize.document.result;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class StoryGroupItem {
    private String _id;
    private int from;
    private OffsetDateTime date;
    private String fileName;
    private String path;
    private String rootPath;
    private Boolean seen;
}
