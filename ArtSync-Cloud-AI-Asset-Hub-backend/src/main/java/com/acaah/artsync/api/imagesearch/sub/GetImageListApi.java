package com.acaah.artsync.api.imagesearch.sub;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.acaah.artsync.api.imagesearch.model.ImageSearchResult;
import com.acaah.artsync.exception.BusinessException;
import com.acaah.artsync.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GetImageListApi {

    /**
     * 获取图片列表
     * 该方法通过发送HTTP GET请求获取指定URL的图片列表数据

     *
     * @param url 请求的目标URL地址
     * @return 返回图片搜索结果列表
     */
    public static List<ImageSearchResult> getImageList(String url) {
        try {
            // 发起GET请求
            // 使用HttpUtil工具类创建GET请求并执行
            HttpResponse response = HttpUtil.createGet(url).execute();

            // 获取响应内容
            // 获取HTTP响应状态码和响应体内容
            int statusCode = response.getStatus();
            String body = response.body();

            // 处理响应
            // 判断响应状态码是否为200，表示请求成功
            if (statusCode == 200) {
                // 解析 JSON 数据并处理
                // 调用processResponse方法处理响应数据
                return processResponse(body);
            } else {
                // 如果请求失败，抛出业务异常
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
        } catch (Exception e) {
            // 记录错误日志
            log.error("获取图片列表失败", e);
            // 抛出业务异常
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取图片列表失败");
        }
    }

    /**
     * 处理接口响应内容
     * 该方法解析接口返回的JSON数据，提取图片列表信息
     *
     * @param responseBody 接口返回的JSON字符串
     * @return 返回解析后的图片搜索结果列表
     */
    private static List<ImageSearchResult> processResponse(String responseBody) {
        // 解析响应对象
        // 将JSON字符串转换为JSONObject对象
        JSONObject jsonObject = new JSONObject(responseBody);
        // 检查响应中是否包含"data"字段
        if (!jsonObject.containsKey("data")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        // 获取"data"字段对应的JSONObject
        JSONObject data = jsonObject.getJSONObject("data");
        // 检查"data"中是否包含"list"字段
        if (!data.containsKey("list")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未获取到图片列表");
        }
        // 获取图片列表数据
        JSONArray list = data.getJSONArray("list");
        // 将JSONArray转换为ImageSearchResult对象列表
        return JSONUtil.toList(list, ImageSearchResult.class);
    }

    /**
     * 主方法
     * 用于测试获取图片列表功能
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 设置请求URL
        String url = "https://graph.baidu.com/ajax/pcsimi?carousel=503&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&inspire=general_pc&limit=30&next=2&render_type=card&session_id=9976667499045488714&sign=1211a1616da44f43d326001767081052&tk=7e02c&tpl_from=pc";
        // 调用getImageList方法获取图片列表
        List<ImageSearchResult> imageList = getImageList(url);
        // 输出搜索结果
        System.out.println("搜索成功" + imageList);
    }
}
