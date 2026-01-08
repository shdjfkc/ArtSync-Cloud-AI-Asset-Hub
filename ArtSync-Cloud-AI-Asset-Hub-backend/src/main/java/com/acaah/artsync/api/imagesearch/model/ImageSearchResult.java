package com.acaah.artsync.api.imagesearch.model;

import lombok.Data;

/**
 * 图片搜索结果类，用于存储图片搜索后的相关信息
 * 使用@Data注解来自动生成getter、setter、toString等方法
 */
@Data
public class ImageSearchResult {

    /**
     * 缩略图地址
     * 用于显示图片的缩略版本
     */
    private String thumbUrl;

    /**
     * 来源地址
     * 指向图片原始来源的URL链接
     */
    private String fromUrl;
}
