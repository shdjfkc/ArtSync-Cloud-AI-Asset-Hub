package com.acaah.artsync.controller;

import com.acaah.artsync.common.BaseResponse;
import com.acaah.artsync.common.ResultUtils;
import com.acaah.artsync.exception.ErrorCode;
import com.acaah.artsync.exception.ThrowUtils;
import com.acaah.artsync.model.dto.space.analyze.*;
import com.acaah.artsync.model.vo.space.analyze.*;
import com.acaah.artsync.model.entity.Space;
import com.acaah.artsync.model.entity.User;
import com.acaah.artsync.service.SpaceAnalyzeService;
import com.acaah.artsync.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {

    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    @Resource
    private UserService userService;

/**
 * 获取空间使用状态
 * 这是一个处理HTTP POST请求的方法，用于获取空间使用分析数据
 *
 * @param spaceUsageAnalyzeRequest 包含空间使用分析请求参数的对象
 * @param request HTTP请求对象，用于获取用户登录信息
 * @return 返回一个BaseResponse对象，其中包含SpaceUsageAnalyzeResponse类型的空间使用分析结果
 */
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,  // 请求体，包含空间使用分析的参数
            HttpServletRequest request  // HTTP请求对象，用于获取当前用户信息
    ) {
    // 检查请求参数是否为空，如果为空则抛出参数错误异常
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    // 从请求中获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
    // 调用空间分析服务获取空间使用分析数据
        SpaceUsageAnalyzeResponse spaceUsageAnalyze = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
    // 返回成功响应，包含空间使用分析结果
        return ResultUtils.success(spaceUsageAnalyze);
    }


/**
 * 获取空间分类分析数据的接口方法
 *
 * @param spaceCategoryAnalyzeRequest 请求参数，包含空间分类分析所需的查询条件
 * @param request HTTP请求对象，用于获取当前登录用户信息
 * @return BaseResponse 包含空间分类分析结果的响应对象
 */
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
                                                                                    HttpServletRequest request) {
        // 检查请求参数是否为空，如果为空则抛出参数错误异常
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 从请求中获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 调用空间分析服务获取空间分类分析数据
        List<SpaceCategoryAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        // 创建并返回成功响应，包含空间分类分析结果
        return ResultUtils.success(resultList);
    }



/**
 * 获取空间标签分析数据的接口方法
 * @param spaceTagAnalyzeRequest 空间标签分析请求参数，包含需要分析的标签信息
 * @param request HTTP请求对象，用于获取用户登录信息
 * @return BaseResponse 包含空间标签分析结果的响应对象，结果为SpaceTagAnalyzeResponse对象的列表
 */
    @PostMapping("/tag")  // 标识这是一个POST请求映射，访问路径为/tag
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
    // 参数校验：如果请求参数为空，则抛出参数错误异常
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
    // 调用服务层方法获取空间标签分析结果
        List<SpaceTagAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
    // 返回成功响应，包含分析结果列表
        return ResultUtils.success(resultList);
    }


/**
 * 获取空间大小分析数据的接口方法
 *
 * @param spaceSizeAnalyzeRequest 空间大小分析请求参数，包含分析所需的条件信息
 * @param request HTTP请求对象，用于获取用户登录信息
 * @return BaseResponse 包含空间大小分析结果的响应对象
 */
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
                                                                            HttpServletRequest request) {
        // 参数校验：如果请求参数为空，则抛出参数错误异常
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 调用服务层方法获取空间大小分析结果
        List<SpaceSizeAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        // 返回成功响应，包含分析结果列表
        return ResultUtils.success(resultList);
    }


/**
 * 获取空间用户分析数据的接口方法
 *
 * @param spaceUserAnalyzeRequest 请求体参数，包含空间用户分析所需的查询条件
 * @param request HTTP请求对象，用于获取当前登录用户信息
 * @return 返回BaseResponse包装的SpaceUserAnalyzeResponse列表，包含分析结果数据
 */
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
    // 参数校验：如果请求体为空，则抛出参数错误异常
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
    // 调用服务层方法获取空间用户分析数据
        List<SpaceUserAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
    // 返回成功响应，包含分析结果数据
        return ResultUtils.success(resultList);
    }

/**
 * 获取空间排名分析数据
 * @param spaceRankAnalyzeRequest 空间排名分析请求参数
 * @param request HTTP请求对象
 * @return 返回空间排名分析结果列表
 */
    @PostMapping("/rank")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(@RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, HttpServletRequest request) {
    // 检查请求参数是否为空，为空则抛出参数错误异常
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
    // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
    // 调用服务层方法获取空间排名分析结果
        List<Space> resultList = spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
    // 返回成功响应，包含分析结果列表
        return ResultUtils.success(resultList);
    }

}


