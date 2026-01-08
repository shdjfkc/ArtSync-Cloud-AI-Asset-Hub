package com.acaah.artsync.controller;

import com.acaah.artsync.model.dto.space.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.acaah.artsync.annontation.AuthCheck;
import com.acaah.artsync.common.BaseResponse;
import com.acaah.artsync.common.DeleteRequest;
import com.acaah.artsync.common.ResultUtils;
import com.acaah.artsync.constant.UserConstant;
import com.acaah.artsync.exception.BusinessException;
import com.acaah.artsync.exception.ErrorCode;
import com.acaah.artsync.exception.ThrowUtils;
import com.acaah.artsync.manager.auth.SpaceUserAuthManager;
import com.acaah.artsync.model.entity.Space;
import com.acaah.artsync.model.entity.User;
import com.acaah.artsync.model.enums.SpaceLevelEnum;
import com.acaah.artsync.model.vo.SpaceVO;
import com.acaah.artsync.service.SpaceService;
import com.acaah.artsync.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;
/**
 * 添加空间接口
 * @param spaceAddRequest 添加空间的请求参数
 * @param request HTTP请求对象，用于获取用户信息
 * @return 返回新添加空间的ID
 */
    @PostMapping("/add")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
    // 检查请求参数是否为空，为空则抛出参数错误异常
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
    // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
    // 调用服务层添加空间，并返回新添加空间的ID
        long newId = spaceService.addSpace(spaceAddRequest, loginUser);
    // 返回成功响应，包含新添加空间的ID
        return ResultUtils.success(newId);
    }

/**
 * 删除空间接口
 * @param deleteRequest 删除请求参数，包含要删除的空间ID
 * @param request HTTP请求对象，用于获取当前登录用户信息
 * @return 返回操作结果，成功返回true，失败抛出异常
 */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest
            , HttpServletRequest request) {
    // 参数校验：检查请求参数是否合法
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        // 判断是否存在
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或者管理员可删除
        spaceService.checkSpaceAuth(loginUser, oldSpace);
        // 操作数据库
        boolean result = spaceService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新空间（仅管理员可用）
     * 该接口允许管理员更新空间信息，包括空间的基本属性和配置
     * @param spaceUpdateRequest 包含更新后空间信息的请求体，包含空间ID及其他需要更新的字段
     * @param request HTTP请求对象，用于获取请求相关信息
     * @return 返回一个BaseResponse对象，包含操作是否成功的布尔值
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 权限检查，只有管理员角色可以访问此接口
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest,
                                             HttpServletRequest request) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() <= 0) {
    // 参数校验：检查请求体是否为空或空间ID是否有效
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);
        // 数据校验
        spaceService.validSpace(space, false);
        // 判断是否存在
        long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取空间（仅管理员可用）
     * 该接口需要管理员权限才能访问
     */
    @GetMapping("/get") // HTTP GET 请求映射到 /get 路径
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 权限检查，仅允许管理员角色访问
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) { // 定义方法，接收空间ID和HTTP请求对象
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR); // 参数校验，如果ID小于等于0则抛出参数错误异常
        // 查询数据库
        Space space = spaceService.getById(id); // 调用服务层方法，根据ID查询空间信息
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR); // 结果校验，如果查询结果为空则抛出未找到异常
        // 获取封装类
        return ResultUtils.success(space); // 返回成功响应，封装查询结果
    }

/**
 * 根据 id 获取空间（封装类）
 * 这是一个处理 HTTP GET 请求的控制器方法，用于获取空间信息并返回封装后的视图对象
 *
 * @param id 空间ID，必须大于0
 * @param request HTTP请求对象，用于获取请求相关信息
 * @return 返回包含空间视图对象的BaseResponse封装结果
*/
    @GetMapping("/get/vo")  // 处理HTTP GET请求，映射到"/get/vo"路径
    public BaseResponse<SpaceVO> getSpaceVOById(long id, HttpServletRequest request) {
    // 参数校验：如果id小于等于0，则抛出参数错误异常
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
    // 查询数据库：根据id获取空间对象
        Space space = spaceService.getById(id);
    // 校验空间是否存在：如果空间不存在，则抛出未找到异常
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
    // 获取空间视图对象：将空间对象转换为视图对象
        SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
    // 获取当前登录用户：从请求中获取登录用户信息
        User loginUser = userService.getLoginUser(request);
    // 获取用户权限列表：获取当前用户在该空间中的权限列表
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
    // 设置权限列表：将权限列表设置到空间视图对象中
        spaceVO.setPermissionList(permissionList);
    // 获取封装类
        return ResultUtils.success(spaceVO);
}


    /**
     * 分页获取空间列表（仅管理员可用）
     * 该接口需要管理员权限才能访问，用于分页查询空间信息
     */
    @PostMapping("/list/page") // HTTP POST请求映射到/list/page路径
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) // 权限检查，要求必须是管理员角色
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        long current = spaceQueryRequest.getCurrent(); // 获取当前页码
        long size = spaceQueryRequest.getPageSize(); // 获取每页大小
        // 查询数据库，使用MyBatis-Plus的分页功能
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest)); // 执行分页查询
        return ResultUtils.success(spacePage); // 返回查询结果
    }

/**
 * 分页获取空间列表（封装类）
 * 该接口用于分页查询空间信息，并返回封装后的视图对象(VO)
 *
 * @param spaceQueryRequest 分页查询条件请求对象，包含当前页码和每页大小等信息
 * @param request HTTP请求对象，用于获取请求上下文信息
 * @return BaseResponse<Page<SpaceVO>> 返回分页结果，包含空间视图对象列表
*/
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                         HttpServletRequest request) {

    // 获取请求中的分页参数
        long current = spaceQueryRequest.getCurrent();    // 当前页码
        long size = spaceQueryRequest.getPageSize();      // 每页大小
        // 限制爬虫：设置每页最大记录数为20，防止恶意大量数据请求
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库：根据分页参数和查询条件获取空间数据
        Page<Space> spacePage = spaceService.page(new Page<>(current, size),
                spaceService.getQueryWrapper(spaceQueryRequest));
        // 获取封装类：将数据库实体对象转换为视图对象(VO)并返回
        return ResultUtils.success(spaceService.getSpaceVOPage(spacePage, request));
    }

/**
 * 编辑空间（给用户使用）
 * 该接口用于允许用户编辑自己创建的空间，只有空间创建者或管理员有权限编辑
 * @PostMapping("/edit") 表示这是一个HTTP POST请求，映射到/edit路径
 * @param spaceEditRequest 包含要编辑的空间信息的请求体，必须包含有效的空间ID
 * @param request HTTP请求对象，用于获取当前登录用户信息
 * @return BaseResponse<Boolean> 返回操作结果，成功返回true，失败抛出异常
*/
    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
    // 参数校验：检查请求体是否为空或空间ID是否无效
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
    // 将SpaceEditRequest对象转换为Space实体对象
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        // 自动填充数据：根据空间等级自动填充相关信息
        spaceService.fillSpaceBySpaceLevel(space);
        // 设置编辑时间：更新空间的编辑时间为当前时间
        space.setEditTime(new Date());
        // 数据校验：验证空间数据的合法性
        spaceService.validSpace(space, false);
    // 获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在：查询数据库中是否存在要编辑的空间
        long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
       spaceService.checkSpaceAuth(loginUser, oldSpace);
        // 操作数据库
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取空间级别列表，便于前端展示
     * 该接口用于获取所有可用的空间级别信息，包括级别值、文本描述、最大数量和最大大小
     * @return BaseResponse<List<SpaceLevel>> 返回空间级别列表的响应结果
     * 包含所有空间级别的详细信息，每个级别包含：
     *         - 级别值
     *         - 文本描述
     *         - 最大数量限制
     *         - 最大大小限制
     */
    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
    // 使用Stream API将枚举值转换为SpaceLevel对象列表
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
            // 将每个枚举值映射为SpaceLevel对象
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),      // 设置级别值
                        spaceLevelEnum.getText(),      // 设置文本描述
                        spaceLevelEnum.getMaxCount(),  // 设置最大数量
                        spaceLevelEnum.getMaxSize()    // 设置最大大小
                ))
            // 将Stream收集为List
                .collect(Collectors.toList());
    // 返回成功响应，包含空间级别列表数据
        return ResultUtils.success(spaceLevelList);
    }
}
