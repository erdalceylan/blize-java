package com.blize.document.result;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class StoryMeItem {
    private String _id;
    private int from;
    private OffsetDateTime date;
    private String fileName;
    private String path;
    private String rootPath;
    private List<StoryViewItem> views;
    private int viewsLength;
}
