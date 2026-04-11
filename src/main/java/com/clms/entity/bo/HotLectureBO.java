package com.clms.entity.bo;

import lombok.Data;

@Data
public class HotLectureBO {
    private String id;

    private String title;

    private String tag;

    private String posterUrl;

    private Long hotValue;
}
