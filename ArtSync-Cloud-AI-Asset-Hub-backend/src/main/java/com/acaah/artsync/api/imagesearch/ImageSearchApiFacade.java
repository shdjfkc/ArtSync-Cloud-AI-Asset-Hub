package com.acaah.artsync.api.imagesearch;

import com.acaah.artsync.api.imagesearch.model.ImageSearchResult;
import com.acaah.artsync.api.imagesearch.sub.GetImageFirstUrlApi;
import com.acaah.artsync.api.imagesearch.sub.GetImageListApi;
import com.acaah.artsync.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 图片搜索API门面类
 * 提供以图搜图功能的统一入口
 */
@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     * 根据提供的图片URL，执行以图搜图操作

     *
     * @param imageUrl 要搜索的图片URL
     * @return 返回图片搜索结果列表
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        // 获取图片页面URL
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        // 获取图片第一页URL
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        // 获取图片列表
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    /**
     * 主方法，用于测试以图搜图功能
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://img.shetu66.com/2025/10/14/176042873328709361.jpg";
        // 执行搜索并获取结果
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        // 输出结果列表
        System.out.println("结果列表" + resultList);
    }
}
