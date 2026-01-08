package com.acaah.artsync.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.acaah.artsync.exception.BusinessException;
import com.acaah.artsync.exception.ErrorCode;
import com.acaah.artsync.exception.ThrowUtils;
import com.acaah.artsync.model.dto.spaceuser.SpaceUserAddRequest;
import com.acaah.artsync.model.dto.spaceuser.SpaceUserQueryRequest;
import com.acaah.artsync.model.entity.Space;
import com.acaah.artsync.model.entity.SpaceUser;
import com.acaah.artsync.model.entity.User;
import com.acaah.artsync.model.enums.SpaceRoleEnum;
import com.acaah.artsync.model.vo.SpaceUserVO;
import com.acaah.artsync.model.vo.SpaceVO;
import com.acaah.artsync.model.vo.UserVO;
import com.acaah.artsync.service.SpaceService;
import com.acaah.artsync.service.SpaceUserService;
import com.acaah.artsync.mapper.SpaceUserMapper;
import com.acaah.artsync.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2026-01-03 14:05:19
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{



    @Resource
    @Lazy
    private SpaceService spaceService;


    @Resource
    private UserService userService;

/**
 * 添加空间用户的方法
 * @param spaceUserAddRequest 添加空间用户的请求参数
 * @return 返回新添加用户的ID
 */
    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 参数校验：检查请求参数是否为空
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
    // 创建新的SpaceUser对象并复制请求属性
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
    // 验证空间用户信息，第二个参数true表示为新增操作
        validSpaceUser(spaceUser, true);

        // 数据库操作：保存用户信息
        boolean result = this.save(spaceUser);
    // 检查保存操作是否成功
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    // 返回新添加用户的ID
        return spaceUser.getId();
    }



    /**
     * 校验空间用户信息
     * @param spaceUser 空间用户对象，包含空间ID、用户ID和空间角色等信息
     * @param add 是否为新增操作，true表示新增，false表示更新
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        // 校验空间用户对象是否为空，为空则抛出参数错误异常
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        // 创建时，空间 id 和用户 id 必填
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        // 如果是新增操作，需要校验空间ID和用户ID是否为空
        if (add) {
            // 如果空间ID或用户ID为空，则抛出参数错误异常
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
            // 根据用户ID查询用户信息，如果用户不存在则抛出"用户不存在"异常
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            // 根据空间ID查询空间信息，如果空间不存在则抛出"空间不存在"异常
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 校验空间角色
        String spaceRole = spaceUser.getSpaceRole();
        // 根据角色值获取对应的枚举对象
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        // 如果角色值不为空但对应的枚举对象为空，说明角色不存在，抛出参数错误异常
        if (spaceRole != null && spaceRoleEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间角色不存在");
        }
    }



    /**
     * 根据查询条件构建QueryWrapper对象
     * @param spaceUserQueryRequest 查询条件对象，包含空间用户的各项查询参数
     * @return QueryWrapper<SpaceUser> 构建好的查询条件包装器，用于数据库查询
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        // 初始化查询条件包装器
        QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
        // 如果查询条件对象为空，直接返回空的查询条件包装器
        if (spaceUserQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        return queryWrapper;
    }


/**
 * 获取空间用户视图对象的方法
 * 该方法将SpaceUser实体对象转换为包含关联用户和空间信息的视图对象
 *
 * @param spaceUser 空间用户实体对象
 * @param request HTTP请求对象，用于获取请求相关信息
 * @return SpaceUserVO 包含完整关联信息的空间用户视图对象
 */
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        // 对象转封装类：将SpaceUser实体对象转换为SpaceUserVO视图对象
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        // 关联查询用户信息：根据用户ID查询并设置用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }
        // 关联查询空间信息：根据空间ID查询并设置空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
            spaceUserVO.setSpace(spaceVO);
        }
        return spaceUserVO;
    }

/**
 * 将SpaceUser列表转换为SpaceUserVO列表
 * @param spaceUserList SpaceUser对象列表
 * @return SpaceUserVO对象列表
 */
    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 判断输入列表是否为空，如果为空则返回空列表
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }
        // 对象列表 => 封装对象列表，使用Stream进行对象转换
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream().map(SpaceUserVO::objToVo).collect(Collectors.toList());
        // 1. 收集需要关联查询的用户 ID 和空间 ID
        Set<Long> userIdSet = spaceUserList.stream().map(SpaceUser::getUserId).collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream().map(SpaceUser::getSpaceId).collect(Collectors.toSet());
        // 2. 批量查询用户和空间信息，并将结果转换为ID到对象的映射
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream()
                .collect(Collectors.groupingBy(Space::getId));
        // 3. 填充 SpaceUserVO 的用户和空间信息
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            // 填充用户信息
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceUserVO.setUser(userService.getUserVO(user));
            // 填充空间信息
            Space space = null;
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                space = spaceIdSpaceListMap.get(spaceId).get(0);
            }
            spaceUserVO.setSpace(SpaceVO.objToVo(space));
        });
        return spaceUserVOList;
    }


}




