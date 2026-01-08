package com.acaah.artsync.manager;

import cn.hutool.core.io.FileUtil;
import com.acaah.artsync.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class CosManager {  
  
    @Resource
    private CosClientConfig cosClientConfig;
  
    @Resource  
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * 该方法用于将本地文件上传到指定的存储桶中
     *
     * @param key  唯一键
     *            用于标识上传对象的唯一键值
     * @param file 文件
     *            需要上传的本地文件对象
     * @return PutObjectResult
     *            返回上传操作的结果，包含ETag等响应信息
     */
    public PutObjectResult putObject(String key, File file) {
        // 创建上传对象的请求，指定存储桶名称、对象键和文件
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 执行上传操作并返回结果
        return cosClient.putObject(putObjectRequest);
    }


    /**
     * 上传对象（附带图片信息）
     * 该方法用于向腾讯云COS上传图片文件，并获取图片的基本信息

     *
     * @param key  唯一键  ，用于在存储桶中标识上传的图片
     * @param file 文件  ，表示要上传的图片文件对象
     * @return PutObjectResult 返回上传结果，包含图片的基本信息
     */

    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 对图片进行处理（获取基本信息也被视作为一种图片的处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        // 图片处理规则列表
        List<PicOperations.Rule> rules = new ArrayList<>();
        // 1. 图片压缩（转成 webp 格式）
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setFileId(webpKey);
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setRule("imageMogr2/format/webp");
        rules.add(compressRule);
        // 2. 缩略图处理，仅对 > 20 KB 的图片生成缩略图
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            // 拼接缩略图的路径
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            // 缩放规则 /thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理）
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
            rules.add(thumbnailRule);
        }
        // 构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     * 该方法用于从腾讯云COS(Cloud Object Storage)下载指定key的对象。
     * 它会创建一个下载请求，并使用配置的COS客户端执行下载操作。
     *
     * @param key 唯一键
     *            用于标识COS中对象的唯一键值，类似于文件路径
     * @return COSObject 返回下载后的COS对象，包含对象的内容和各种元数据
     */
    public COSObject getObject(String key) {
    // 创建获取对象的请求，指定存储桶名称和对象键
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
    // 使用COS客户端执行下载请求并返回结果
        return cosClient.getObject(getObjectRequest);
    }
    /**
     * 删除对象
     *
     * @param key 文件 key
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

}
