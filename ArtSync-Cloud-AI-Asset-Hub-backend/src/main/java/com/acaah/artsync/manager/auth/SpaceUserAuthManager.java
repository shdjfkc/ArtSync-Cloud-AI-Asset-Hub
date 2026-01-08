package com.acaah.artsync.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.acaah.artsync.manager.auth.model.SpaceUserAuthConfig;
import com.acaah.artsync.manager.auth.model.SpaceUserRole;
import com.acaah.artsync.model.entity.Space;
import com.acaah.artsync.model.entity.SpaceUser;
import com.acaah.artsync.model.entity.User;
import com.acaah.artsync.model.enums.SpaceRoleEnum;
import com.acaah.artsync.model.enums.SpaceTypeEnum;
import com.acaah.artsync.service.SpaceUserService;
import com.acaah.artsync.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 空间成员权限管理器
 * 读取权限配置
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

/**
 * 根据角色获取权限列表
 * 该方法接收一个角色标识符作为参数，返回该角色对应的权限列表
 * @param spaceUserRole 角色标识符，用于在配置中查找对应角色
 * @return 权限列表，如果角色不存在或参数为空则返回空列表
 */
    public List<String> getPermissionsByRole(String spaceUserRole) {
    // 检查输入参数是否为空或空白字符串
        if (StrUtil.isBlank(spaceUserRole)) {
        // 如果参数无效，返回空列表
            return new ArrayList<>();
        }
        // 找到匹配的角色
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(r -> spaceUserRole.equals(r.getKey()))
                .findFirst()
                .orElse(null);
        if (role == null) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }


/**
 * 根据空间和登录用户获取权限列表
 * @param space 空间对象，可能为null
 * @param loginUser 登录用户对象，可能为null
 * @return 权限列表，如果用户无权限则返回空列表
 */
    public List<String> getPermissionList(Space space, User loginUser) {
    // 如果登录用户为null，直接返回空列表
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权限
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return new ArrayList<>();
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权限
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权限
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查询 SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }

}
