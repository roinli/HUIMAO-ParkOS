package com.huimao.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huimao.parking.domain.ParkingDailyStat;
import com.huimao.parking.vo.ParkingDashboardVo;
import com.huimao.parking.vo.ParkingStatOverviewVo;

public interface IParkingStatService
{
    IPage<ParkingDailyStat> selectDailyPage(ParkingDailyStat query);

    ParkingStatOverviewVo selectOverview(ParkingDailyStat query);

    ParkingDashboardVo selectDashboard(Long lotId, String startTime, String endTime);

    int refreshDailyStat(ParkingDailyStat query);
}
