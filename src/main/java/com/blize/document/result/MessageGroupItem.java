package com.blize.document.result;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class MessageGroupItem {
    private int from;
    private int to;
    private String text;
    private OffsetDateTime date;
    private boolean read;
    private int unReadCount;
}
