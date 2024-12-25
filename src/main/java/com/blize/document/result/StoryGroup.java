package com.blize.document.result;

import lombok.Data;
import java.util.List;

@Data
public class StoryGroup {

    private int from;

    private List<StoryGroupItem> items;
}
