package com.huimao.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huimao.parking.domain.ParkingAccountFlow;
import com.huimao.parking.domain.ParkingWithdrawOrder;
import com.huimao.parking.dto.ParkingWithdrawApplyRequest;
import com.huimao.parking.dto.ParkingWithdrawReviewRequest;
import com.huimao.parking.vo.ParkingAccountSummaryVo;

public interface IParkingAccountService
{
    ParkingAccountSummaryVo selectAccountSummary(Long tenantId);

    ParkingAccountSummaryVo selectAccountInfo(Long tenantId);

    IPage<ParkingWithdrawOrder> selectWithdrawPage(ParkingWithdrawOrder query);

    IPage<ParkingAccountFlow> selectFlowPage(ParkingAccountFlow query);

    int applyWithdraw(ParkingWithdrawApplyRequest request);

    int reviewWithdraw(ParkingWithdrawReviewRequest request);
}
