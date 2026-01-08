package com.acaah.artsync.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片审核请求
 */
@Data    // 使用Lombok库自动生成getter、setter等方法
public class PictureReviewRequest implements Serializable {    // 实现Serializable接口以支持序列化
  
    /**  
     * id  
     */  
    private Long id;  
  
    /**  
     * 状态：0-待审核, 1-通过, 2-拒绝  
     */  
    private Integer reviewStatus;  
  
    /**  
     * 审核信息  
     */  
    private String reviewMessage;  
  
  
    private static final long serialVersionUID = 1L;  
}
