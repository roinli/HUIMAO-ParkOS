package com.huimao.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huimao.common.core.exception.ServiceException;
import com.huimao.common.core.utils.StringUtils;
import com.huimao.parking.constant.ParkingConstants;
import com.huimao.parking.domain.ParkingChargeRule;
import com.huimao.parking.domain.ParkingLot;
import com.huimao.parking.mapper.ParkingChargeRuleMapper;
import com.huimao.parking.mapper.ParkingLotMapper;
import com.huimao.parking.service.IParkingChargeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 收费规则服务实现。
 */
@Service
public class ParkingChargeRuleServiceImpl extends AbstractParkingTenantService implements IParkingChargeRuleService
{
    @Autowired
    private ParkingChargeRuleMapper parkingChargeRuleMapper;

    @Autowired
    private ParkingLotMapper parkingLotMapper;

    /**
     * 分页查询收费规则。
     */
    @Override
    public IPage<ParkingChargeRule> selectPage(ParkingChargeRule query)
    {
        LambdaQueryWrapper<ParkingChargeRule> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ParkingChargeRule::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL);
        Long tenantId = resolveQueryTenantId(query.getTenantId());
        if (tenantId != null)
        {
            wrapper.eq(ParkingChargeRule::getTenantId, tenantId);
        }
        if (StringUtils.isNotEmpty(query.getRuleName()))
        {
            wrapper.like(ParkingChargeRule::getRuleName, query.getRuleName());
        }
        if (StringUtils.isNotEmpty(query.getStatus()))
        {
            wrapper.eq(ParkingChargeRule::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(ParkingChargeRule::getCreateTime);
        return parkingChargeRuleMapper.selectPage(wrapper);
    }

    /**
     * 查询收费规则详情。
     */
    @Override
    public ParkingChargeRule selectById(Long ruleId)
    {
        ParkingChargeRule rule = parkingChargeRuleMapper.selectById(ruleId);
        if (rule == null || ParkingConstants.DEL_FLAG_DELETED.equals(rule.getDelFlag()))
        {
            throw new ServiceException("收费规则不存在");
        }
        validateTenant(rule.getTenantId());
        return rule;
    }

    /**
     * 新增收费规则。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(ParkingChargeRule rule)
    {
        rule.setTenantId(resolveCreateTenantId(rule.getTenantId()));
        rule.setDefaultFlag("0");
        rule.setDelFlag(ParkingConstants.DEL_FLAG_NORMAL);
        fillFollowUnitPrice(rule);
        return parkingChargeRuleMapper.insert(rule);
    }

    /**
     * 更新收费规则。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ParkingChargeRule rule)
    {
        ParkingChargeRule db = selectById(rule.getRuleId());
        rule.setTenantId(db.getTenantId());
        rule.setDefaultFlag(db.getDefaultFlag());
        rule.setDelFlag(db.getDelFlag());
        fillFollowUnitPrice(rule);
        return parkingChargeRuleMapper.updateById(rule);
    }

    /**
     * 批量逻辑删除收费规则。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids)
    {
        int rows = 0;
        for (Long id : ids)
        {
            ParkingChargeRule rule = selectById(id);
            Long bindCount = parkingLotMapper.selectCount(Wrappers.<ParkingLot>lambdaQuery()
                    .eq(ParkingLot::getChargeRuleId, id)
                    .eq(ParkingLot::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL));
            if (bindCount != null && bindCount > 0)
            {
                throw new ServiceException("收费规则已被停车场绑定，不能删除");
            }
            ParkingChargeRule update = new ParkingChargeRule();
            update.setRuleId(rule.getRuleId());
            update.setDelFlag(ParkingConstants.DEL_FLAG_DELETED);
            rows += parkingChargeRuleMapper.updateById(update);
        }
        return rows;
    }

    private void fillFollowUnitPrice(ParkingChargeRule rule)
    {
        if (rule.getFollowUnitPrice() == null)
        {
            rule.setFollowUnitPrice(rule.getUnitPrice());
        }
    }
}
