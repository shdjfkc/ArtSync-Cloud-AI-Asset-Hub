package com.acaah.artsync.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data // 使用Lombok注解，自动生成getter、setter、toString等方法
public class SearchPictureByColorRequest implements Serializable { // 类声明，实现Serializable接口以支持序列化

    /**
     * 图片主色调
     * 多行注释：用于描述picColor字段的作用，表示图片的主要颜色特征
     */
    private String picColor; // 单行注释：定义String类型的picColor属性，用于存储图片的主色调信息

    /**
     * 空间 id
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
