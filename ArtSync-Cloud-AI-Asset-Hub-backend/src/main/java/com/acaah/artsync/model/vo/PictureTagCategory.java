package com.acaah.artsync.model.vo;


import lombok.Data;

import java.util.List;

/**
 * 图片标签类别列表视图
 *
 */
@Data
public class PictureTagCategory {

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 类别列表
     */
    private List<String> categoryList;
}
