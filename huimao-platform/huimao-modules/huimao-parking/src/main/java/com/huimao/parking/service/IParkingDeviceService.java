package com.huimao.parking.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.huimao.parking.domain.ParkingDevice;

/**
 * 车场设备服务。
 */
public interface IParkingDeviceService
{
    /**
     * 分页查询设备。
     */
    IPage<ParkingDevice> selectPage(ParkingDevice query);

    /**
     * 查询设备详情。
     */
    ParkingDevice selectById(Long deviceId);

    /**
     * 新增设备。
     */
    int insert(ParkingDevice parkingDevice);

    /**
     * 更新设备。
     */
    int update(ParkingDevice parkingDevice);

    /**
     * 批量删除设备。
     */
    int deleteByIds(Long[] ids);
}
