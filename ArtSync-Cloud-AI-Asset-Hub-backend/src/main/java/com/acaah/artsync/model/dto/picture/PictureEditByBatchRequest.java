package com.acaah.artsync.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * PictureEditByBatchRequest 类用于批量编辑图片的请求参数
 * 该类实现了 Serializable 接口，支持序列化操作
 * 使用 @Data 注解自动生成 getter、setter 等方法
 */
@Data
public class PictureEditByBatchRequest implements Serializable {

    /**
     * 图片 id 列表
     * 用于指定需要批量编辑的图片 ID 集合
     */
    private List<Long> pictureIdList;

    /**
     * 空间 id
     * 用于指定图片所属的空间标识
     */
    private Long spaceId;

    /**
     * 分类
     * 用于对图片进行分类标识
     */
    private String category;
    /**
     * 命名规则
     */
    private String nameRule;

    /**
     * 标签
     * 用于对图片进行标签标记，可以是多个标签
     */
    private List<String> tags;

    /**
     * 序列化版本号
     * 用于在序列化和反序列化过程中进行版本控制
     */
    private static final long serialVersionUID = 1L;
}
