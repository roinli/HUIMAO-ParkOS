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
import com.huimao.parking.mapper.ParkingSessionMapper;
import com.huimao.parking.service.IParkingLotService;
import com.huimao.parking.util.ParkingNoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 停车场服务实现。
 */
@Service
public class ParkingLotServiceImpl extends AbstractParkingTenantService implements IParkingLotService
{
    private static final String DEFAULT_RULE_NAME = "租户-默认规则";

    private static final int DEFAULT_FREE_MINUTES = 15;

    private static final int DEFAULT_UNIT_MINUTES = 60;

    private static final BigDecimal DEFAULT_UNIT_PRICE = new BigDecimal("5.00");

    private static final BigDecimal DEFAULT_FOLLOW_UNIT_PRICE = new BigDecimal("2.00");

    private static final BigDecimal DEFAULT_DAY_CAP_AMOUNT = new BigDecimal("30.00");

    private static final int DEFAULT_PAY_GRACE_MINUTES = 15;

    @Autowired
    private ParkingLotMapper parkingLotMapper;

    @Autowired
    private ParkingChargeRuleMapper parkingChargeRuleMapper;

    @Autowired
    private ParkingSessionMapper parkingSessionMapper;

    @Value("${parking.h5.pay-url:http://localhost:81}")
    private String h5PayUrl;

    /**
     * 按租户、编码、名称、状态分页查询停车场。
     */
    @Override
    public IPage<ParkingLot> selectPage(ParkingLot query)
    {
        LambdaQueryWrapper<ParkingLot> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ParkingLot::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL);
        Long tenantId = resolveQueryTenantId(query.getTenantId());
        if (tenantId != null)
        {
            wrapper.eq(ParkingLot::getTenantId, tenantId);
        }
        if (StringUtils.isNotEmpty(query.getLotName()))
        {
            wrapper.like(ParkingLot::getLotName, query.getLotName());
        }
        if (StringUtils.isNotEmpty(query.getLotCode()))
        {
            wrapper.like(ParkingLot::getLotCode, query.getLotCode());
        }
        if (StringUtils.isNotEmpty(query.getStatus()))
        {
            wrapper.eq(ParkingLot::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(ParkingLot::getCreateTime);
        return parkingLotMapper.selectPage(wrapper);
    }

    /**
     * 查询停车场详情，并做逻辑删除与租户权限校验。
     */
    @Override
    public ParkingLot selectById(Long lotId)
    {
        ParkingLot lot = parkingLotMapper.selectById(lotId);
        if (lot == null || ParkingConstants.DEL_FLAG_DELETED.equals(lot.getDelFlag()))
        {
            throw new ServiceException("停车场不存在");
        }
        validateTenant(lot.getTenantId());
        return lot;
    }

    /**
     * 新增停车场。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(ParkingLot parkingLot)
    {
        parkingLot.setTenantId(resolveCreateTenantId(parkingLot.getTenantId()));
        parkingLot.setLotCode(generateLotCode());
        parkingLot.setDelFlag(ParkingConstants.DEL_FLAG_NORMAL);
        fillQrFields(parkingLot);
        validateUnique(parkingLot, null);
        parkingLot.setChargeRuleId(resolveChargeRuleId(parkingLot));
        return parkingLotMapper.insert(parkingLot);
    }

    /**
     * 更新停车场，但不允许通过入参篡改所属租户和删除标记。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ParkingLot parkingLot)
    {
        ParkingLot db = selectById(parkingLot.getLotId());
        parkingLot.setTenantId(db.getTenantId());
        parkingLot.setLotCode(db.getLotCode());
        parkingLot.setDelFlag(db.getDelFlag());
        fillQrFields(parkingLot);
        validateUnique(parkingLot, db.getLotId());
        if (parkingLot.getChargeRuleId() == null)
        {
            parkingLot.setChargeRuleId(resolveDefaultChargeRuleId(db.getTenantId()));
        }
        else
        {
            validateChargeRule(parkingLot.getChargeRuleId(), db.getTenantId());
        }
        return parkingLotMapper.updateById(parkingLot);
    }

    private void fillQrFields(ParkingLot parkingLot)
    {
        parkingLot.setPayQrContent(buildPayQrContent(parkingLot.getLotCode()));
        parkingLot.setPayQrUrl("/parking/open/h5/lot/" + parkingLot.getLotCode() + "/qrcode");
    }

    private String buildPayQrContent(String lotCode)
    {
        String baseUrl = StringUtils.isEmpty(h5PayUrl) ? "http://localhost:81" : h5PayUrl.trim();
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "lotCode=" + lotCode;
    }

    private void validateUnique(ParkingLot parkingLot, Long excludeLotId)
    {
        if (StringUtils.isEmpty(parkingLot.getLotCode()))
        {
            throw new ServiceException("停车场编码不能为空");
        }
        if (StringUtils.isEmpty(parkingLot.getLotName()))
        {
            throw new ServiceException("停车场名称不能为空");
        }
        if (existsLot(Wrappers.<ParkingLot>lambdaQuery()
                .eq(ParkingLot::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                .eq(ParkingLot::getLotCode, parkingLot.getLotCode()), excludeLotId))
        {
            throw new ServiceException("停车场编码已存在");
        }
        if (existsLot(Wrappers.<ParkingLot>lambdaQuery()
                .eq(ParkingLot::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                .eq(ParkingLot::getTenantId, parkingLot.getTenantId())
                .eq(ParkingLot::getLotName, parkingLot.getLotName()), excludeLotId))
        {
            throw new ServiceException("当前租户下停车场名称已存在");
        }
        if (StringUtils.isNotEmpty(parkingLot.getPayQrContent()) && existsLot(Wrappers.<ParkingLot>lambdaQuery()
                .eq(ParkingLot::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                .eq(ParkingLot::getPayQrContent, parkingLot.getPayQrContent()), excludeLotId))
        {
            throw new ServiceException("停车场二维码内容已被使用");
        }
        if (StringUtils.isNotEmpty(parkingLot.getPayQrUrl()) && existsLot(Wrappers.<ParkingLot>lambdaQuery()
                .eq(ParkingLot::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                .eq(ParkingLot::getPayQrUrl, parkingLot.getPayQrUrl()), excludeLotId))
        {
            throw new ServiceException("停车场二维码链接已被使用");
        }
    }

    private String generateLotCode()
    {
        for (int i = 0; i < 5; i++)
        {
            String lotCode = ParkingNoUtils.lotCode();
            Long count = parkingLotMapper.selectCount(Wrappers.<ParkingLot>lambdaQuery()
                    .eq(ParkingLot::getLotCode, lotCode));
            if (count == null || count == 0)
            {
                return lotCode;
            }
        }
        throw new ServiceException("停车场编码生成失败，请重试");
    }

    private boolean existsLot(LambdaQueryWrapper<ParkingLot> wrapper, Long excludeLotId)
    {
        if (excludeLotId != null)
        {
            wrapper.ne(ParkingLot::getLotId, excludeLotId);
        }
        return parkingLotMapper.selectCount(wrapper) > 0;
    }

    /**
     * 删除停车场前先校验是否仍有未完成停车会话。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids)
    {
        int rows = 0;
        for (Long id : ids)
        {
            ParkingLot lot = selectById(id);
            Long activeCount = parkingSessionMapper.selectCount(Wrappers.<com.huimao.parking.domain.ParkingSession>lambdaQuery()
                    .eq(com.huimao.parking.domain.ParkingSession::getLotId, id)
                    .ne(com.huimao.parking.domain.ParkingSession::getStatus, ParkingConstants.SESSION_STATUS_COMPLETED));
            if (activeCount != null && activeCount > 0)
            {
                throw new ServiceException("停车场存在未完成停车记录，禁止删除");
            }
            ParkingLot update = new ParkingLot();
            update.setLotId(lot.getLotId());
            update.setDelFlag(ParkingConstants.DEL_FLAG_DELETED);
            rows += parkingLotMapper.updateById(update);
        }
        return rows;
    }

    private Long resolveChargeRuleId(ParkingLot parkingLot)
    {
        if (parkingLot.getChargeRuleId() != null)
        {
            validateChargeRule(parkingLot.getChargeRuleId(), parkingLot.getTenantId());
            return parkingLot.getChargeRuleId();
        }
        return resolveDefaultChargeRuleId(parkingLot.getTenantId());
    }

    private Long resolveDefaultChargeRuleId(Long tenantId)
    {
        ParkingChargeRule rule = parkingChargeRuleMapper.selectOne(Wrappers.<ParkingChargeRule>lambdaQuery()
                .eq(ParkingChargeRule::getTenantId, tenantId)
                .eq(ParkingChargeRule::getDefaultFlag, "1")
                .eq(ParkingChargeRule::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL)
                .orderByDesc(ParkingChargeRule::getCreateTime)
                .last("limit 1"));
        if (rule == null)
        {
            rule = createTenantDefaultChargeRule(tenantId);
        }
        return rule.getRuleId();
    }

    private void validateChargeRule(Long chargeRuleId, Long tenantId)
    {
        ParkingChargeRule rule = parkingChargeRuleMapper.selectById(chargeRuleId);
        if (rule == null || ParkingConstants.DEL_FLAG_DELETED.equals(rule.getDelFlag()))
        {
            throw new ServiceException("默认收费规则不存在");
        }
        if (!tenantId.equals(rule.getTenantId()))
        {
            throw new ServiceException("默认收费规则不属于当前租户");
        }
    }

    private ParkingChargeRule createTenantDefaultChargeRule(Long tenantId)
    {
        ParkingChargeRule rule = new ParkingChargeRule();
        rule.setTenantId(tenantId);
        rule.setDefaultFlag("1");
        rule.setRuleName(DEFAULT_RULE_NAME);
        rule.setFreeMinutes(DEFAULT_FREE_MINUTES);
        rule.setUnitMinutes(DEFAULT_UNIT_MINUTES);
        rule.setUnitPrice(DEFAULT_UNIT_PRICE);
        rule.setFollowUnitPrice(DEFAULT_FOLLOW_UNIT_PRICE);
        rule.setDayCapAmount(DEFAULT_DAY_CAP_AMOUNT);
        rule.setPayGraceMinutes(DEFAULT_PAY_GRACE_MINUTES);
        rule.setOvertimeRecalc("1");
        rule.setStatus(ParkingConstants.COMMON_STATUS_ENABLED);
        rule.setEffectiveTime(new Date());
        rule.setDelFlag(ParkingConstants.DEL_FLAG_NORMAL);
        rule.setRemark("停车场初始化时自动补齐");
        parkingChargeRuleMapper.insert(rule);
        return rule;
    }
}
