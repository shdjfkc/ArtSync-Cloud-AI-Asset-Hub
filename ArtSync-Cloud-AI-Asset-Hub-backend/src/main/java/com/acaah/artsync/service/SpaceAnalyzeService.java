package com.acaah.artsync.service;

import com.acaah.artsync.model.dto.space.analyze.*;
import com.acaah.artsync.model.vo.space.analyze.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.acaah.artsync.model.entity.Space;
import com.acaah.artsync.model.entity.User;

import java.util.List;

public interface SpaceAnalyzeService extends IService<Space> {




/**
 * 获取空间使用分析的方法
 * @param spaceUsageAnalyzeRequest 空间使用分析请求对象，包含分析所需的参数
 * @param loginUser 当前登录用户对象，用于权限验证
 * @return SpaceUsageAnalyzeResponse 空间使用分析结果对象，包含分析后的数据
 */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

/**
 * 获取空间类别分析数据的方法
 *
 * @param spaceCategoryAnalyzeRequest 空间类别分析请求参数，包含分析所需的查询条件
 * @param loginUser 当前登录用户信息，用于权限验证和数据过滤
 * @return 返回空间类别分析结果列表，每个元素包含对应类别的分析数据
 */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

/**
 * 获取空间标签分析结果
 *
 * @param spaceTagAnalyzeRequest 空间标签分析请求参数，包含分析所需的各项条件
 * @param loginUser 当前登录用户信息，用于权限验证
 * @return 返回空间标签分析结果列表，每个元素包含一个标签的分析数据
 */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

/**
 * 获取空间大小分析结果的方法
 *
 * @param spaceSizeAnalyzeRequest 空间大小分析请求参数对象，包含分析所需的各种参数
 * @param loginUser 当前登录用户对象，用于权限验证和用户关联
 * @return 返回空间大小分析结果的列表，每个元素包含一个空间的分析数据
 */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

/**
 * 获取空间用户分析数据的方法
 *
 * @param spaceUserAnalyzeRequest 空间用户分析请求参数，包含分析所需的查询条件
 * @param loginUser 当前登录用户信息，用于权限验证
 * @return 返回空间用户分析结果列表，每个元素包含一个用户的分析数据
 */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

/**
 * 获取空间排名分析数据
 *
 * @param spaceRankAnalyzeRequest 空间排名分析请求参数，包含分析所需的各项条件
 * @param loginUser 当前登录用户信息，用于权限验证和数据隔离
 * @return 返回空间排名分析结果列表，每个元素包含一个空间的详细排名分析数据
 */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
