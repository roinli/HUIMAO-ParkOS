package com.huimao.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huimao.parking.domain.ParkingLot;

/**
 * 停车场基础信息服务。
 */
public interface IParkingLotService
{
    /**
     * 分页查询当前租户下的停车场。
     */
    IPage<ParkingLot> selectPage(ParkingLot query);

    /**
     * 按主键查询停车场详情。
     */
    ParkingLot selectById(Long lotId);

    /**
     * 新增停车场。
     */
    int insert(ParkingLot parkingLot);

    /**
     * 更新停车场。
     */
    int update(ParkingLot parkingLot);

    /**
     * 批量逻辑删除停车场。
     */
    int deleteByIds(Long[] ids);
}
