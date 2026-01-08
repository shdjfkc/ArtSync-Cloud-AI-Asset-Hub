package com.acaah.artsync.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.acaah.artsync.exception.BusinessException;
import com.acaah.artsync.exception.ErrorCode;
import com.acaah.artsync.exception.ThrowUtils;
import com.acaah.artsync.manager.sharding.DynamicShardingManager;
import com.acaah.artsync.mapper.SpaceMapper;

import com.acaah.artsync.model.dto.space.SpaceAddRequest;
import com.acaah.artsync.model.dto.space.SpaceQueryRequest;
import com.acaah.artsync.model.entity.Space;
import com.acaah.artsync.model.entity.SpaceUser;
import com.acaah.artsync.model.entity.User;
import com.acaah.artsync.model.enums.SpaceLevelEnum;
import com.acaah.artsync.model.enums.SpaceRoleEnum;
import com.acaah.artsync.model.enums.SpaceTypeEnum;
import com.acaah.artsync.model.vo.SpaceVO;
import com.acaah.artsync.model.vo.UserVO;
import com.acaah.artsync.service.SpaceService;

import com.acaah.artsync.service.SpaceUserService;
import com.acaah.artsync.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author Administrator
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-12-29 13:22:58
*/
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    @Lazy
    private DynamicShardingManager dynamicShardingManager;

    /**
     * 创建空间的方法
     * 该方法用于根据用户请求创建一个新的空间，并进行参数校验、权限验证和数据库操作

 *
     * @param spaceAddRequest 包含空间创建信息的请求对象
     * @param loginUser       当前登录用户信息
     * @return 新创建空间的ID，如果创建失败则返回-1
     */
    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 1. 填充参数默认值
        // 转换实体类和 DTO (Data Transfer Object)
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
    // 如果空间名称为空，则设置为默认值
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
    // 如果空间级别为空，则设置为普通级别
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (spaceAddRequest.getSpaceType() == null) {
            spaceAddRequest.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        // 在此处将实体类和 DTO 进行转换
       // Space space = new Space();
        //BeanUtils.copyProperties(spaceAddRequest, space);
        // 填充容量和大小
        this.fillSpaceBySpaceLevel(space);
        // 2. 校验参数
        this.validSpace(space, true);
        // 3. 校验权限，非管理员只能创建普通级别的空间
        Long userId = loginUser.getId();
        space.setUserId(userId);
        if (SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定级别的空间");
        }


        // 4. 控制同一用户只能创建一个私有空间,以及一个团队空间

        String lock = String.valueOf(userId).intern();
        synchronized (lock) {
            Long newSpaceId = transactionTemplate.execute(status -> {
                if (!userService.isAdmin(loginUser)) {
                    boolean exists = this.lambdaQuery()
                            .eq(Space::getUserId, userId)
                            .eq(Space::getSpaceType, spaceAddRequest.getSpaceType())
                            .exists();
                    ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户每类空间仅能创建一个");
                }
                // 写入数据库
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
                // 如果是团队空间，关联新增团队成员记录
                if (SpaceTypeEnum.TEAM.getValue() == spaceAddRequest.getSpaceType()) {
                    SpaceUser spaceUser = new SpaceUser();
                    spaceUser.setSpaceId(space.getId());
                    spaceUser.setUserId(userId);
                    spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
                    result = spaceUserService.save(spaceUser);
                    ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
                }
                // 创建分表
                dynamicShardingManager.createSpacePictureTable(space);
                // 返回新写入的数据 id
                return space.getId();
            });
            return Optional.ofNullable(newSpaceId).orElse(-1L);
        }
    }




    /**
     * 校验空间信息的方法
     * @param space 空间对象，包含需要校验的空间信息
     * @param add 布尔值，true表示创建空间时的校验，false表示修改空间时的校验
     */
    @Override
    public void validSpace(Space space, boolean add) {
        // 校验空间对象是否为空，为空则抛出参数错误异常
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        String spaceName = space.getSpaceName();
        // 空间级别
        Integer spaceLevel = space.getSpaceLevel();
        // 空间级别枚举
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 空间类型
        Integer spaceType = space.getSpaceType();
        // 空间类型枚举
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(spaceType);


        // 创建时校验
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
            if (spaceType == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不能为空");
            }
        }
        // 修改数据时，空间名称进行校验
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }
        // 修改数据时，空间级别进行校验
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
        // 修改数据时，如果要改空间级别
        if (spaceType != null && spaceTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不存在");
        }
    }

    /**
 * 将Space对象转换为SpaceVO对象，并关联用户信息
 * @param space Space对象，包含空间基本信息
 * @param request HttpServletRequest对象，用于获取请求相关信息
 * @return SpaceVO对象，包含空间信息和关联的用户信息
 */
    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类：将Space对象转换为SpaceVO对象
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息：根据空间中的用户ID获取用户信息并封装到VO中
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
        // 根据用户ID获取用户实体对象
            User user = userService.getById(userId);
        // 将用户实体对象转换为用户VO对象
            UserVO userVO = userService.getUserVO(user);
        // 将用户VO对象设置到空间VO对象中
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }


    /**
     * 获取空间视图对象（SpaceVO）的分页数据
     * @param spacePage 空间数据的分页对象
     * @param request HTTP请求对象，可用于获取请求相关信息
     * @return 返回填充了用户信息的空间视图对象分页数据
     */
    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        // 从分页对象中获取空间数据列表
        List<Space> spaceList = spacePage.getRecords();
        // 创建新的SpaceVO分页对象，保持原有的分页参数
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        // 如果空间列表为空，直接返回空的分页对象
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 将Space对象列表转换为SpaceVO对象列表
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream()
                .map(SpaceVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 从空间列表中提取所有用户ID集合
        // 1,2,3,4
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        // 根据用户ID查询用户信息，并按用户ID分组
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }


    /**
     * 根据空间查询请求参数构建查询条件包装器
     * @param spaceQueryRequest 空间查询请求对象，包含查询条件
     * @return QueryWrapper<Space> 返回构建好的查询条件包装器
     */
    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        // 创建查询条件包装器实例
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        // 如果查询请求对象为空，直接返回空的查询条件包装器
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();
        Integer spaceType = spaceQueryRequest.getSpaceType();


        // 拼接查询条件
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceType), "spaceType", spaceType);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

/**
 * 根据空间级别填充空间信息的方法
 * @param space 需要填充的空间对象
 */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
    // 根据空间的级别值获取对应的枚举类型
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
    // 如果获取到了对应的枚举类型
        if (spaceLevelEnum != null) {
        // 获取枚举中定义的最大空间大小
            long maxSize = spaceLevelEnum.getMaxSize();
        // 如果传入的空间对象的最大大小为空，则设置为枚举中定义的最大大小
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
        // 获取枚举中定义的最大数量
            long maxCount = spaceLevelEnum.getMaxCount();
        // 如果传入的空间对象的最大数量为空，则设置为枚举中定义的最大数量
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }



    /**
     * 检查用户是否有权限操作指定空间
     * @param loginUser 当前登录用户
     * @param space 需要检查权限的空间对象
     * @throws BusinessException 如果用户没有权限则抛出业务异常
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        // 仅本人或管理员可编辑
        // 判断当前用户是否为空间创建者或管理员
        if (!space.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            // 如果既不是空间创建者也不是管理员，则抛出无权限异常
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }
}




