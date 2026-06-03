package com.huimao.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huimao.common.core.exception.ServiceException;
import com.huimao.common.core.utils.DateUtils;
import com.huimao.common.core.utils.StringUtils;
import com.huimao.common.security.utils.SecurityUtils;
import com.huimao.parking.constant.ParkingConstants;
import com.huimao.parking.domain.ParkingAccount;
import com.huimao.parking.domain.ParkingAccountFlow;
import com.huimao.parking.domain.ParkingPayment;
import com.huimao.parking.domain.ParkingTenantSettleConfig;
import com.huimao.parking.domain.ParkingWithdrawOrder;
import com.huimao.parking.dto.ParkingWithdrawApplyRequest;
import com.huimao.parking.dto.ParkingWithdrawReviewRequest;
import com.huimao.parking.mapper.ParkingAccountFlowMapper;
import com.huimao.parking.mapper.ParkingAccountMapper;
import com.huimao.parking.mapper.ParkingPaymentMapper;
import com.huimao.parking.mapper.ParkingTenantSettleConfigMapper;
import com.huimao.parking.mapper.ParkingWithdrawOrderMapper;
import com.huimao.parking.mapper.SysTenantLiteMapper;
import com.huimao.parking.service.IParkingAccountService;
import com.huimao.parking.util.ParkingNoUtils;
import com.huimao.parking.vo.ParkingAccountSummaryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 停车账户与提现服务。
 */
@Service
public class ParkingAccountServiceImpl extends AbstractParkingTenantService implements IParkingAccountService
{
    private static final BigDecimal DEFAULT_FEE_RATE = new BigDecimal("0.0060");

    private static final BigDecimal DEFAULT_MIN_WITHDRAW_AMOUNT = new BigDecimal("0.10");

    @Autowired
    private ParkingAccountMapper parkingAccountMapper;

    @Autowired
    private ParkingAccountFlowMapper parkingAccountFlowMapper;

    @Autowired
    private ParkingPaymentMapper parkingPaymentMapper;

    @Autowired
    private ParkingTenantSettleConfigMapper parkingTenantSettleConfigMapper;

    @Autowired
    private ParkingWithdrawOrderMapper parkingWithdrawOrderMapper;

    @Autowired
    private SysTenantLiteMapper sysTenantLiteMapper;

    @Override
    public ParkingAccountSummaryVo selectAccountSummary(Long tenantId)
    {
        Long targetTenantId = resolveAccountTenantId(tenantId);
        syncMissingParkingIncomeFlows(targetTenantId);
        List<ParkingWithdrawOrder> withdrawRecords = parkingWithdrawOrderMapper.selectList(Wrappers.<ParkingWithdrawOrder>lambdaQuery()
                .eq(ParkingWithdrawOrder::getTenantId, targetTenantId)
                .orderByDesc(ParkingWithdrawOrder::getCreateTime)
                .last("limit 5"));
        List<ParkingAccountFlow> flows = parkingAccountFlowMapper.selectList(Wrappers.<ParkingAccountFlow>lambdaQuery()
                .eq(ParkingAccountFlow::getTenantId, targetTenantId)
                .orderByDesc(ParkingAccountFlow::getCreateTime)
                .last("limit 10"));

        ParkingAccountSummaryVo vo = buildAccountInfo(targetTenantId);
        vo.setWithdrawRecords(withdrawRecords);
        vo.setFlows(flows);
        return vo;
    }

    @Override
    public ParkingAccountSummaryVo selectAccountInfo(Long tenantId)
    {
        Long targetTenantId = resolveAccountTenantId(tenantId);
        syncMissingParkingIncomeFlows(targetTenantId);
        return buildAccountInfo(targetTenantId);
    }

    @Override
    public IPage<ParkingWithdrawOrder> selectWithdrawPage(ParkingWithdrawOrder query)
    {
        LambdaQueryWrapper<ParkingWithdrawOrder> wrapper = Wrappers.lambdaQuery();
        Long tenantId = resolveQueryTenantId(query.getTenantId());
        if (tenantId != null)
        {
            wrapper.eq(ParkingWithdrawOrder::getTenantId, tenantId);
        }
        if (StringUtils.isNotEmpty(query.getWithdrawNo()))
        {
            wrapper.like(ParkingWithdrawOrder::getWithdrawNo, query.getWithdrawNo());
        }
        if (StringUtils.isNotEmpty(query.getStatus()))
        {
            wrapper.eq(ParkingWithdrawOrder::getStatus, query.getStatus());
        }
        Object beginTime = query.getParams().get("beginCreateTime");
        Object endTime = query.getParams().get("endCreateTime");
        if (beginTime != null)
        {
            wrapper.ge(ParkingWithdrawOrder::getCreateTime, beginTime);
        }
        if (endTime != null)
        {
            wrapper.le(ParkingWithdrawOrder::getCreateTime, endTime);
        }
        wrapper.orderByDesc(ParkingWithdrawOrder::getCreateTime);
        IPage<ParkingWithdrawOrder> page = parkingWithdrawOrderMapper.selectPage(wrapper);
        enrichTenantName(page.getRecords());
        return page;
    }

    @Override
    public IPage<ParkingAccountFlow> selectFlowPage(ParkingAccountFlow query)
    {
        LambdaQueryWrapper<ParkingAccountFlow> wrapper = Wrappers.lambdaQuery();
        Long tenantId = resolveQueryTenantId(query.getTenantId());
        if (tenantId != null)
        {
            syncMissingParkingIncomeFlows(tenantId);
            wrapper.eq(ParkingAccountFlow::getTenantId, tenantId);
        }
        if (query.getAccountId() != null)
        {
            wrapper.eq(ParkingAccountFlow::getAccountId, query.getAccountId());
        }
        if (StringUtils.isNotEmpty(query.getBizType()))
        {
            wrapper.eq(ParkingAccountFlow::getBizType, query.getBizType());
        }
        if (StringUtils.isNotEmpty(query.getBizNo()))
        {
            wrapper.like(ParkingAccountFlow::getBizNo, query.getBizNo());
        }
        if (StringUtils.isNotEmpty(query.getChangeType()))
        {
            wrapper.eq(ParkingAccountFlow::getChangeType, query.getChangeType());
        }
        Object beginTime = query.getParams().get("beginCreateTime");
        Object endTime = query.getParams().get("endCreateTime");
        if (beginTime != null)
        {
            wrapper.ge(ParkingAccountFlow::getCreateTime, beginTime);
        }
        if (endTime != null)
        {
            wrapper.le(ParkingAccountFlow::getCreateTime, endTime);
        }
        wrapper.orderByDesc(ParkingAccountFlow::getCreateTime).orderByDesc(ParkingAccountFlow::getFlowId);
        return parkingAccountFlowMapper.selectPage(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int applyWithdraw(ParkingWithdrawApplyRequest request)
    {
        if (request == null || request.getApplyAmount() == null)
        {
            throw new ServiceException("提现金额不能为空");
        }
        if (currentTenantId() == null)
        {
            throw new ServiceException("当前登录租户不存在");
        }

        ParkingAccount account = ensureAccount(currentTenantId());
        if (!ParkingConstants.ACCOUNT_STATUS_NORMAL.equals(account.getStatus()))
        {
            throw new ServiceException("当前账户状态不允许提现");
        }

        ParkingTenantSettleConfig settleConfig = getSettleConfig(currentTenantId());
        BigDecimal minWithdrawAmount = defaultMinWithdrawAmount(settleConfig);
        BigDecimal feeRate = defaultFeeRate(settleConfig);
        BigDecimal applyAmount = request.getApplyAmount().setScale(2, RoundingMode.HALF_UP);
        if (applyAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("提现金额必须大于 0");
        }
        if (applyAmount.compareTo(minWithdrawAmount) < 0)
        {
            throw new ServiceException("提现金额不能低于最小提现金额");
        }
        if (nullSafe(account.getAvailableAmount()).compareTo(applyAmount) < 0)
        {
            throw new ServiceException("可提现余额不足");
        }

        BigDecimal feeAmount = applyAmount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal realAmount = applyAmount.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP);
        if (realAmount.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("提现金额异常，请检查手续费配置");
        }

        ParkingWithdrawOrder order = new ParkingWithdrawOrder();
        order.setTenantId(account.getTenantId());
        order.setWithdrawNo(ParkingNoUtils.withdrawNo());
        order.setApplyAmount(applyAmount);
        order.setFeeAmount(feeAmount);
        order.setRealAmount(realAmount);
        order.setFeeRate(feeRate);
        order.setStatus(ParkingConstants.WITHDRAW_STATUS_WAIT_AUDIT);
        order.setAuditRemark(request.getApplyRemark());
        parkingWithdrawOrderMapper.insert(order);

        BigDecimal beforeAvailable = nullSafe(account.getAvailableAmount());
        ParkingAccount update = new ParkingAccount();
        update.setAccountId(account.getAccountId());
        update.setAvailableAmount(beforeAvailable.subtract(applyAmount));
        update.setFrozenAmount(nullSafe(account.getFrozenAmount()).add(applyAmount));
        parkingAccountMapper.updateById(update);

        insertFlow(account.getTenantId(), account.getAccountId(), ParkingConstants.ACCOUNT_BIZ_TYPE_WITHDRAW_FREEZE,
                order.getWithdrawNo(), "OUT", applyAmount, beforeAvailable, beforeAvailable.subtract(applyAmount), "提现申请冻结");
        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reviewWithdraw(ParkingWithdrawReviewRequest request)
    {
        if (!isAdmin())
        {
            throw new ServiceException("仅平台管理员可审核提现");
        }
        if (request == null || request.getWithdrawId() == null)
        {
            throw new ServiceException("提现单ID不能为空");
        }
        if (!ParkingConstants.WITHDRAW_STATUS_PAID.equals(request.getStatus())
                && !ParkingConstants.WITHDRAW_STATUS_REJECTED.equals(request.getStatus()))
        {
            throw new ServiceException("审核结果不合法");
        }

        ParkingWithdrawOrder order = parkingWithdrawOrderMapper.selectById(request.getWithdrawId());
        if (order == null)
        {
            throw new ServiceException("提现单不存在");
        }
        if (!ParkingConstants.WITHDRAW_STATUS_WAIT_AUDIT.equals(order.getStatus()))
        {
            throw new ServiceException("当前提现单已审核，请勿重复操作");
        }

        ParkingAccount account = ensureAccount(order.getTenantId());
        Date now = DateUtils.getNowDate();
        ParkingWithdrawOrder updateOrder = new ParkingWithdrawOrder();
        updateOrder.setWithdrawId(order.getWithdrawId());
        updateOrder.setStatus(request.getStatus());
        updateOrder.setAuditBy(SecurityUtils.getUsername());
        updateOrder.setAuditTime(now);
        updateOrder.setAuditRemark(request.getAuditRemark());
        if (ParkingConstants.WITHDRAW_STATUS_PAID.equals(request.getStatus()))
        {
            updateOrder.setPayTime(now);
            BigDecimal beforeBalance = nullSafe(account.getBalanceAmount());
            ParkingAccount update = new ParkingAccount();
            update.setAccountId(account.getAccountId());
            update.setBalanceAmount(beforeBalance.subtract(order.getApplyAmount()));
            update.setFrozenAmount(nullSafe(account.getFrozenAmount()).subtract(order.getApplyAmount()));
            parkingAccountMapper.updateById(update);
            insertFlow(account.getTenantId(), account.getAccountId(), ParkingConstants.ACCOUNT_BIZ_TYPE_WITHDRAW_PAY,
                    order.getWithdrawNo(), "OUT", order.getApplyAmount(), beforeBalance, beforeBalance.subtract(order.getApplyAmount()), "提现打款出账");
        }
        else
        {
            BigDecimal beforeAvailable = nullSafe(account.getAvailableAmount());
            ParkingAccount update = new ParkingAccount();
            update.setAccountId(account.getAccountId());
            update.setAvailableAmount(beforeAvailable.add(order.getApplyAmount()));
            update.setFrozenAmount(nullSafe(account.getFrozenAmount()).subtract(order.getApplyAmount()));
            parkingAccountMapper.updateById(update);
            insertFlow(account.getTenantId(), account.getAccountId(), ParkingConstants.ACCOUNT_BIZ_TYPE_WITHDRAW_REJECT,
                    order.getWithdrawNo(), "IN", order.getApplyAmount(), beforeAvailable, beforeAvailable.add(order.getApplyAmount()), "提现驳回退回可提现余额");
        }
        return parkingWithdrawOrderMapper.updateById(updateOrder);
    }

    private Long resolveAccountTenantId(Long tenantId)
    {
        Long targetTenantId = resolveQueryTenantId(tenantId);
        if (targetTenantId == null)
        {
            throw new ServiceException("请指定需要查看的租户");
        }
        return targetTenantId;
    }

    private ParkingAccount ensureAccount(Long tenantId)
    {
        ParkingAccount account = parkingAccountMapper.selectOne(Wrappers.<ParkingAccount>lambdaQuery()
                .eq(ParkingAccount::getTenantId, tenantId)
                .last("limit 1"));
        if (account != null)
        {
            return account;
        }
        ParkingAccount create = new ParkingAccount();
        create.setTenantId(tenantId);
        create.setTotalIncomeAmount(BigDecimal.ZERO);
        create.setBalanceAmount(BigDecimal.ZERO);
        create.setAvailableAmount(BigDecimal.ZERO);
        create.setFrozenAmount(BigDecimal.ZERO);
        create.setStatus(ParkingConstants.ACCOUNT_STATUS_NORMAL);
        parkingAccountMapper.insert(create);
        return parkingAccountMapper.selectById(create.getAccountId());
    }

    private ParkingAccountSummaryVo buildAccountInfo(Long tenantId)
    {
        ParkingAccount account = ensureAccount(tenantId);
        ParkingTenantSettleConfig settleConfig = getSettleConfig(tenantId);
        ParkingAccountSummaryVo vo = new ParkingAccountSummaryVo();
        BeanUtils.copyProperties(account, vo);
        vo.setFeeRate(defaultFeeRate(settleConfig));
        vo.setMinWithdrawAmount(defaultMinWithdrawAmount(settleConfig));
        vo.setMerchantName(settleConfig == null ? null : settleConfig.getMerchantName());
        vo.setSettleMerchantName(settleConfig == null ? null : settleConfig.getWithdrawAccountName());
        vo.setSettleBankName(settleConfig == null ? null : settleConfig.getWithdrawBankName());
        vo.setSettleAccountNo(settleConfig == null ? null : settleConfig.getWithdrawAccountNo());
        vo.setWithdrawRecords(Collections.emptyList());
        vo.setFlows(Collections.emptyList());
        return vo;
    }

    private void syncMissingParkingIncomeFlows(Long tenantId)
    {
        if (tenantId == null)
        {
            return;
        }
        List<ParkingPayment> payments = parkingPaymentMapper.selectList(Wrappers.<ParkingPayment>lambdaQuery()
                .eq(ParkingPayment::getTenantId, tenantId)
                .eq(ParkingPayment::getPayStatus, ParkingConstants.PAY_STATUS_SUCCESS)
                .gt(ParkingPayment::getPayAmount, BigDecimal.ZERO)
                .orderByAsc(ParkingPayment::getPaySuccessTime)
                .orderByAsc(ParkingPayment::getPaymentId));
        if (payments.isEmpty())
        {
            return;
        }

        Set<String> paymentNos = payments.stream().map(ParkingPayment::getPaymentNo)
                .filter(StringUtils::isNotEmpty).collect(Collectors.toSet());
        if (paymentNos.isEmpty())
        {
            return;
        }
        Set<String> existingIncomeNos = parkingAccountFlowMapper.selectList(Wrappers.<ParkingAccountFlow>lambdaQuery()
                        .eq(ParkingAccountFlow::getTenantId, tenantId)
                        .eq(ParkingAccountFlow::getBizType, ParkingConstants.ACCOUNT_BIZ_TYPE_PARKING_INCOME)
                        .in(ParkingAccountFlow::getBizNo, paymentNos))
                .stream().map(ParkingAccountFlow::getBizNo).filter(StringUtils::isNotEmpty).collect(Collectors.toSet());

        List<ParkingPayment> missingPayments = payments.stream()
                .filter(item -> StringUtils.isNotEmpty(item.getPaymentNo()) && !existingIncomeNos.contains(item.getPaymentNo()))
                .collect(Collectors.toList());
        if (missingPayments.isEmpty())
        {
            return;
        }

        ParkingAccount account = ensureAccount(tenantId);
        BigDecimal totalIncome = nullSafe(account.getTotalIncomeAmount());
        BigDecimal balance = nullSafe(account.getBalanceAmount());
        BigDecimal available = nullSafe(account.getAvailableAmount());
        for (ParkingPayment payment : missingPayments)
        {
            BigDecimal amount = nullSafe(payment.getPayAmount());
            if (amount.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            BigDecimal beforeBalance = balance;
            balance = balance.add(amount);
            available = available.add(amount);
            totalIncome = totalIncome.add(amount);

            ParkingAccountFlow flow = new ParkingAccountFlow();
            flow.setTenantId(tenantId);
            flow.setAccountId(account.getAccountId());
            flow.setBizType(ParkingConstants.ACCOUNT_BIZ_TYPE_PARKING_INCOME);
            flow.setBizNo(payment.getPaymentNo());
            flow.setChangeType("IN");
            flow.setChangeAmount(amount);
            flow.setBeforeBalance(beforeBalance);
            flow.setAfterBalance(balance);
            flow.setRemark("停车缴费入账");
            flow.setCreateTime(payment.getPaySuccessTime());
            parkingAccountFlowMapper.insert(flow);
        }

        ParkingAccount update = new ParkingAccount();
        update.setAccountId(account.getAccountId());
        update.setTotalIncomeAmount(totalIncome);
        update.setBalanceAmount(balance);
        update.setAvailableAmount(available);
        parkingAccountMapper.updateById(update);
    }

    private ParkingTenantSettleConfig getSettleConfig(Long tenantId)
    {
        return parkingTenantSettleConfigMapper.selectOne(Wrappers.<ParkingTenantSettleConfig>lambdaQuery()
                .eq(ParkingTenantSettleConfig::getTenantId, tenantId)
                .last("limit 1"));
    }

    private BigDecimal defaultFeeRate(ParkingTenantSettleConfig settleConfig)
    {
        if (settleConfig == null || settleConfig.getFeeRate() == null)
        {
            return DEFAULT_FEE_RATE;
        }
        return settleConfig.getFeeRate();
    }

    private BigDecimal defaultMinWithdrawAmount(ParkingTenantSettleConfig settleConfig)
    {
        if (settleConfig == null || settleConfig.getMinWithdrawAmount() == null)
        {
            return DEFAULT_MIN_WITHDRAW_AMOUNT;
        }
        return settleConfig.getMinWithdrawAmount();
    }

    private void insertFlow(Long tenantId, Long accountId, String bizType, String bizNo, String changeType,
                            BigDecimal changeAmount, BigDecimal beforeBalance, BigDecimal afterBalance, String remark)
    {
        ParkingAccountFlow flow = new ParkingAccountFlow();
        flow.setTenantId(tenantId);
        flow.setAccountId(accountId);
        flow.setBizType(bizType);
        flow.setBizNo(bizNo);
        flow.setChangeType(changeType);
        flow.setChangeAmount(changeAmount);
        flow.setBeforeBalance(beforeBalance);
        flow.setAfterBalance(afterBalance);
        flow.setRemark(remark);
        parkingAccountFlowMapper.insert(flow);
    }

    private void enrichTenantName(List<ParkingWithdrawOrder> records)
    {
        if (records == null || records.isEmpty())
        {
            return;
        }
        Set<Long> tenantIds = records.stream().map(ParkingWithdrawOrder::getTenantId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (tenantIds.isEmpty())
        {
            return;
        }
        Map<Long, String> tenantNameMap = sysTenantLiteMapper.selectList(Wrappers.<com.huimao.parking.domain.SysTenantLite>lambdaQuery()
                        .in(com.huimao.parking.domain.SysTenantLite::getId, tenantIds))
                .stream().collect(Collectors.toMap(com.huimao.parking.domain.SysTenantLite::getId,
                        com.huimao.parking.domain.SysTenantLite::getTenantName, (a, b) -> a));
        for (ParkingWithdrawOrder record : records)
        {
            record.setTenantName(tenantNameMap.get(record.getTenantId()));
        }
    }

    private BigDecimal nullSafe(BigDecimal amount)
    {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
