package com.huimao.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huimao.common.core.exception.ServiceException;
import com.huimao.common.core.utils.StringUtils;
import com.huimao.parking.constant.ParkingConstants;
import com.huimao.parking.domain.ParkingDevice;
import com.huimao.parking.domain.ParkingGate;
import com.huimao.parking.mapper.ParkingDeviceMapper;
import com.huimao.parking.mapper.ParkingGateMapper;
import com.huimao.parking.service.IParkingDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 设备服务实现。
 */
@Service
public class ParkingDeviceServiceImpl extends AbstractParkingTenantService implements IParkingDeviceService
{
    @Autowired
    private ParkingDeviceMapper parkingDeviceMapper;

    @Autowired
    private ParkingGateMapper parkingGateMapper;

    /**
     * 分页查询设备。
     */
    @Override
    public IPage<ParkingDevice> selectPage(ParkingDevice query)
    {
        LambdaQueryWrapper<ParkingDevice> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ParkingDevice::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL);
        Long tenantId = resolveQueryTenantId(query.getTenantId());
        if (tenantId != null)
        {
            wrapper.eq(ParkingDevice::getTenantId, tenantId);
        }
        if (query.getLotId() != null)
        {
            wrapper.eq(ParkingDevice::getLotId, query.getLotId());
        }
        if (query.getGateId() != null)
        {
            wrapper.eq(ParkingDevice::getGateId, query.getGateId());
        }
        if (StringUtils.isNotEmpty(query.getDeviceCode()))
        {
            wrapper.like(ParkingDevice::getDeviceCode, query.getDeviceCode());
        }
        if (StringUtils.isNotEmpty(query.getDeviceName()))
        {
            wrapper.like(ParkingDevice::getDeviceName, query.getDeviceName());
        }
        if (StringUtils.isNotEmpty(query.getStatus()))
        {
            wrapper.eq(ParkingDevice::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(ParkingDevice::getCreateTime);
        return parkingDeviceMapper.selectPage(wrapper);
    }

    /**
     * 查询设备详情。
     */
    @Override
    public ParkingDevice selectById(Long deviceId)
    {
        ParkingDevice device = parkingDeviceMapper.selectById(deviceId);
        if (device == null || ParkingConstants.DEL_FLAG_DELETED.equals(device.getDelFlag()))
        {
            throw new ServiceException("设备不存在");
        }
        validateTenant(device.getTenantId());
        return device;
    }

    /**
     * 新增设备，自动继承所属通道的租户和停车场。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(ParkingDevice parkingDevice)
    {
        ParkingGate gate = parkingGateMapper.selectById(parkingDevice.getGateId());
        if (gate == null || ParkingConstants.DEL_FLAG_DELETED.equals(gate.getDelFlag()))
        {
            throw new ServiceException("通道不存在");
        }
        validateTenant(gate.getTenantId());
        parkingDevice.setTenantId(gate.getTenantId());
        parkingDevice.setLotId(gate.getLotId());
        parkingDevice.setDelFlag(ParkingConstants.DEL_FLAG_NORMAL);
        if (parkingDeviceMapper.selectCount("device_code", parkingDevice.getDeviceCode()) > 0)
        {
            throw new ServiceException("设备编码已存在");
        }
        return parkingDeviceMapper.insert(parkingDevice);
    }

    /**
     * 更新设备，但不允许改绑到其他通道。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ParkingDevice parkingDevice)
    {
        ParkingDevice db = selectById(parkingDevice.getDeviceId());
        parkingDevice.setTenantId(db.getTenantId());
        parkingDevice.setLotId(db.getLotId());
        parkingDevice.setGateId(db.getGateId());
        parkingDevice.setDelFlag(db.getDelFlag());
        return parkingDeviceMapper.updateById(parkingDevice);
    }

    /**
     * 批量逻辑删除设备。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids)
    {
        int rows = 0;
        for (Long id : ids)
        {
            ParkingDevice device = selectById(id);
            ParkingDevice update = new ParkingDevice();
            update.setDeviceId(device.getDeviceId());
            update.setDelFlag(ParkingConstants.DEL_FLAG_DELETED);
            rows += parkingDeviceMapper.updateById(update);
        }
        return rows;
    }
}
