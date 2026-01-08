package com.acaah.artsync.api.alinyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.acaah.artsync.api.alinyunai.model.CreateOutPaintingTaskRequest;
import com.acaah.artsync.api.alinyunai.model.CreateOutPaintingTaskResponse;
import com.acaah.artsync.api.alinyunai.model.GetOutPaintingTaskResponse;
import com.acaah.artsync.exception.BusinessException;
import com.acaah.artsync.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliYunAiApi {
    // 读取配置文件
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    // 创建任务地址
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/out-painting";

    // 查询任务状态
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建任务
     * 该方法用于创建一个扩图任务，发送请求到AI服务并处理响应
     * @param createOutPaintingTaskRequest 创建扩图任务的请求参数，包含扩图所需的各项配置信息
     * @return CreateOutPaintingTaskResponse 返回创建任务的响应结果，包含任务ID等信息
     * @throws BusinessException 当请求参数为空或AI服务返回错误时抛出业务异常
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        // 参数校验：检查请求参数是否为空
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }

        // 构建HTTP请求，设置请求头和请求体
        // 发送请求到AI扩图任务创建接口
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                // 设置认证头，使用API密钥进行Bearer认证
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                // 必须开启异步处理，设置为enable。
                .header("X-DashScope-Async", "enable")
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .body(JSONUtil.toJsonStr(createOutPaintingTaskRequest));
        // 使用try-with-resources语句执行HTTP请求，确保资源自动关闭
        try (HttpResponse httpResponse = httpRequest.execute()) {
            // 检查HTTP响应是否正常
            if (!httpResponse.isOk()) {
                // 记录错误日志，输出响应体内容
                log.error("请求异常：{}", httpResponse.body());
                // 抛出业务异常，提示AI扩图失败
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图失败");
            }
            // 将HTTP响应体转换为CreateOutPaintingTaskResponse对象
            CreateOutPaintingTaskResponse response = JSONUtil.toBean(httpResponse.body(), CreateOutPaintingTaskResponse.class);
            // 获取响应中的错误码
            String errorCode = response.getCode();
            // 检查错误码是否为空
            if (StrUtil.isNotBlank(errorCode)) {
                // 获取响应中的错误信息
                String errorMessage = response.getMessage();
                // 记录错误日志，输出错误码和错误信息
                log.error("AI 扩图失败，errorCode:{}, errorMessage:{}", errorCode, errorMessage);
                // 抛出业务异常，提示AI扩图接口响应异常
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI 扩图接口响应异常");
            }
            log.info("AI 扩图成功，taskId:{}", response.getOutput().getTaskId());
            return response;
        }
    }

    /**
     * 查询创建的任务
     * 该方法用于根据任务ID查询图片生成任务的详细信息
     * @param taskId 任务ID，用于标识需要查询的具体任务
     * @return GetOutPaintingTaskResponse 包含任务状态和结果的响应对象
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
    // 检查任务ID是否为空，如果为空则抛出业务异常
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "任务 id 不能为空");
        }
        try (HttpResponse httpResponse = HttpRequest.get(String.format(GET_OUT_PAINTING_TASK_URL, taskId))
            // 设置请求头，添加API密钥进行认证
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
            // 执行HTTP GET请求
                .execute()) {
        // 检查响应状态码，如果不是200则抛出异常
            if (!httpResponse.isOk()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取任务失败");
            }
        // 将响应体JSON字符串转换为响应对象并返回
            return JSONUtil.toBean(httpResponse.body(), GetOutPaintingTaskResponse.class);
        }
    }
}
