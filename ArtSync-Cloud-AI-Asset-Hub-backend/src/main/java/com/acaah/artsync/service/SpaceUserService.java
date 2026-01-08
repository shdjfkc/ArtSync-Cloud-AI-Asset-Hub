package com.acaah.artsync.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.acaah.artsync.model.dto.spaceuser.SpaceUserAddRequest;
import com.acaah.artsync.model.dto.spaceuser.SpaceUserQueryRequest;
import com.acaah.artsync.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.acaah.artsync.model.vo.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Administrator
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2026-01-03 14:05:19
*/
public interface SpaceUserService extends IService<SpaceUser> {

/**
 * 添加空间用户的方法
 *
 * @param spaceUserAddRequest 添加空间用户的请求参数对象，包含添加用户所需的所有信息
 * @return 返回一个long类型的值，通常表示操作结果或新添加用户的ID
 */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

/**
 * 验证空间用户的有效性
 * @param spaceUser 需要验证的空间用户对象
 * @param add 标识是否为添加操作的布尔值
 *        true - 表示添加新用户时的验证
 *        false - 表示更新用户信息时的验证
 */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

/**
 * 根据查询请求参数创建查询包装器
 *
 * @param spaceUserQueryRequest 查询请求参数对象，包含查询条件
 * @return QueryWrapper<SpaceUser> 返回一个包含查询条件的查询包装器，用于构建数据库查询条件
 */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

/**
 * 根据SpaceUser对象和HttpServletRequest请求对象获取SpaceUserVO对象
 *
 * @param spaceUser 用户空间实体对象，包含用户空间相关信息
 * @param request HttpServletRequest请求对象，包含当前请求的相关信息
 * @return SpaceUserVO 用户空间视图对象，用于前端展示
 */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 将 SpaceUser 列表转换为 SpaceUserVO 列表
     * @param spaceUserList SpaceUser 对象列表，包含需要转换的用户数据
     * @return SpaceUserVO 对象列表，转换后的视图对象列表，用于前端展示
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
