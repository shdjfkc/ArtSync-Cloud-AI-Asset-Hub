package com.acaah.artsync.controller;

import com.acaah.artsync.model.dto.user.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.acaah.artsync.annontation.AuthCheck;
import com.acaah.artsync.common.BaseResponse;
import com.acaah.artsync.common.DeleteRequest;
import com.acaah.artsync.common.ResultUtils;
import com.acaah.artsync.constant.UserConstant;
import com.acaah.artsync.exception.BusinessException;
import com.acaah.artsync.exception.ErrorCode;
import com.acaah.artsync.exception.ThrowUtils;
import com.acaah.artsync.model.entity.User;
import com.acaah.artsync.model.vo.LoginUserVO;
import com.acaah.artsync.model.vo.UserVO;
import com.acaah.artsync.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

/**
 * 用户注册
 * 处理用户注册请求的接口方法
 * @param userRegisterRequest 用户注册请求参数，包含用户账号、密码和确认密码
 * @return 返回注册结果，包含用户ID
 */
    @PostMapping("/register")  // HTTP POST请求映射到/register路径
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
    // 检查请求参数是否为空，如果为空则抛出参数错误异常
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);


    // 从请求中提取用户注册所需的参数
        String userAccount = userRegisterRequest.getUserAccount();    // 用户账号
        String userPassword = userRegisterRequest.getUserPassword();  // 用户密码
        String checkPassword = userRegisterRequest.getCheckPassword(); // 确认密码
    // 调用用户服务处理注册逻辑，返回注册后的用户ID
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
    // 返回成功响应，包含用户ID
        return ResultUtils.success(result);
    }

/**
 * 用户登录接口
 * @param userLoginRequest 用户登录请求参数，包含用户账号和密码
 * @param request HTTP请求对象，用于获取请求相关信息
 * @return 返回登录用户信息视图对象(LoginUserVO)
 */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
    // 参数校验：如果请求参数为空，则抛出参数错误异常
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
    // 从请求参数中获取用户账号
        String userAccount = userLoginRequest.getUserAccount();
    // 从请求参数中获取用户密码
        String userPassword = userLoginRequest.getUserPassword();
    // 调用userService的userLogin方法处理登录逻辑，并返回登录用户信息
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
    // 返回成功响应，包含登录用户信息
        return ResultUtils.success(loginUserVO);
    }

/**
 * 获取当前登录用户信息的接口方法
 * 通过HTTP GET请求访问"/get/login"路径时触发此方法
 *
 * @param request HTTP请求对象，用于获取请求信息
 * @return BaseResponse<LoginUserVO> 返回包含用户信息的响应对象
 */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
    // 调用userService的getLoginUser方法从请求中获取当前登录用户信息
        User loginUser = userService.getLoginUser(request);
    // 使用ResultUtils.success方法包装并返回用户信息的视图对象
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

/**
 * 用户登出接口
 * @param request HttpServletRequest对象，用于获取请求信息
 * @return 返回一个BaseResponse对象，包含操作是否成功的布尔值
 */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
    // 参数校验：如果request为null，抛出参数错误异常
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
    // 调用userService的userLogout方法执行登出操作，并获取操作结果
        boolean result = userService.userLogout(request);
    // 使用ResultUtils.success方法封装操作结果并返回
        return ResultUtils.success(result);
    }

/**
 * 创建用户
 * 该接口用于添加新用户，需要管理员权限
 * 参数为UserAddRequest对象，包含用户基本信息
 * 返回新创建用户的ID
 */
    @PostMapping("/add")  // HTTP POST请求映射到/add路径
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，要求必须是管理员角色
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
    // 检查请求参数是否为空，为空则抛出参数错误异常
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
    // 创建User对象并复制请求参数中的属性
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }



/**
 * 根据 id 获取用户（仅管理员）
 * 该接口需要管理员权限才能访问
 * @GetMapping("/get") 表示这是一个 GET 请求，映射到 /get 路径
 * @AuthCheck(mustRole = UserConstant.ADMIN_ROLE) 表示必须有管理员角色才能访问此接口
 *
 * @param id 用户 id，必须大于 0
 * @return BaseResponse<User> 返回用户信息，封装在 BaseResponse 中
 */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
    // 参数校验：如果 id 小于等于 0，抛出参数错误异常
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
    // 根据id查询用户信息
        User user = userService.getById(id);
    // 如果用户不存在，抛出未找到错误异常
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
    // 返回成功响应，包含用户信息
        return ResultUtils.success(user);
    }

/**
 * 根据 id 获取包装类
 * 该方法通过用户ID获取用户视图对象(UserVO)
 * @GetMapping("/get/vo") 表示这是一个GET请求映射到"/get/vo"路径
 * @param id 用户ID，用于查询特定用户
 * @return 返回一个BaseResponse类型的对象，其中包含用户视图对象(UserVO)
 */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
    // 首先调用getUserById方法获取用户基础信息
        BaseResponse<User> response = getUserById(id);
    // 从响应中提取用户数据
        User user = response.getData();
    // 调用userService的getUserVO方法将用户对象转换为视图对象，并封装成成功响应返回
        return ResultUtils.success(userService.getUserVO(user));
    }

/**
 * 删除用户的接口方法
 * 需要管理员权限才能访问
 * 使用POST请求方式
 *
 * @param deleteRequest 包含要删除的用户ID的请求对象
 * @return 返回操作结果，成功返回true，失败返回false
 * @throws BusinessException 当请求参数无效时抛出
 */
    @PostMapping("/delete")  // 设置请求路径为/delete，请求方法为POST
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，要求必须是管理员角色
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {  // 接收请求体中的DeleteRequest对象
    // 参数校验：检查请求对象是否为空或ID是否小于等于0
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);  // 参数错误则抛出业务异常
        }
    // 调用服务层方法根据ID删除用户
        boolean b = userService.removeById(deleteRequest.getId());
    // 返回操作结果
        return ResultUtils.success(b);
    }

/**
 * 更新用户
 * 这是一个处理用户更新请求的接口方法
 * 只有管理员角色可以调用此接口
 *
 * @param userUpdateRequest 包含用户更新信息的请求对象，必须包含用户ID
 * @return BaseResponse<Boolean> 操作结果，成功返回true，失败抛出异常
 * @throws BusinessException 当请求参数为空或用户ID为空时抛出参数错误异常
 *
*/
    @PostMapping("/update")  // HTTP POST请求映射到/update路径
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，要求必须是管理员角色
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        // 参数校验：检查请求对象和用户ID是否为空
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 创建用户对象并复制请求属性
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        // 执行更新操作
        boolean result = userService.updateById(user);
        // 如果更新失败，抛出操作异常
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回成功结果
        return ResultUtils.success(true);
    }

/**
 * 分页获取用户封装列表（仅管理员）
 * 该接口仅允许管理员角色访问，用于获取分页的用户视图对象列表
 *
 * @param userQueryRequest 查询请求参数，包含分页信息和查询条件
 * @return BaseResponse<Page<UserVO>> 返回分页后的用户视图对象列表
*/
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)  // 权限检查，必须是管理员角色才能访问
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
    // 参数校验，如果请求参数为空则抛出参数错误异常
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);


    // 获取分页参数
        long current = userQueryRequest.getCurrent();  // 当前页码
        long pageSize = userQueryRequest.getPageSize();  // 每页大小
    // 调用服务层获取分页数据
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
    // 创建用户视图对象的分页对象
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
    // 将用户实体列表转换为视图对象列表
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
    // 设置视图对象列表到分页对象中
        userVOPage.setRecords(userVOList);
    // 返回成功响应，包含分页后的用户视图对象列表
        return ResultUtils.success(userVOPage);
    }


}
