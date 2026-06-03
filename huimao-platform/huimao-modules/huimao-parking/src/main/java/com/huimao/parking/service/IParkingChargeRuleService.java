package com.huimao.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huimao.parking.domain.ParkingChargeRule;

/**
 * 停车收费规则服务。
 */
public interface IParkingChargeRuleService
{
    /**
     * 分页查询收费规则。
     */
    IPage<ParkingChargeRule> selectPage(ParkingChargeRule query);

    /**
     * 查询收费规则详情。
     */
    ParkingChargeRule selectById(Long ruleId);

    /**
     * 新增收费规则。
     */
    int insert(ParkingChargeRule rule);

    /**
     * 更新收费规则。
     */
    int update(ParkingChargeRule rule);

    /**
     * 批量删除收费规则。
     */
    int deleteByIds(Long[] ids);
}
