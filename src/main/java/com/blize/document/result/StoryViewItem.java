package com.blize.document.result;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class StoryViewItem {
    private int from;
    private OffsetDateTime date;
}
