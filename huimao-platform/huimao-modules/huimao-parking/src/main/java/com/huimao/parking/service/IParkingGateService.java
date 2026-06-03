package com.huimao.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huimao.parking.domain.ParkingGate;

/**
 * 停车通道服务。
 */
public interface IParkingGateService
{
    /**
     * 分页查询通道。
     */
    IPage<ParkingGate> selectPage(ParkingGate query);

    /**
     * 查询通道详情。
     */
    ParkingGate selectById(Long gateId);

    /**
     * 新增通道。
     */
    int insert(ParkingGate parkingGate);

    /**
     * 更新通道。
     */
    int update(ParkingGate parkingGate);

    /**
     * 批量删除通道。
     */
    int deleteByIds(Long[] ids);
}
