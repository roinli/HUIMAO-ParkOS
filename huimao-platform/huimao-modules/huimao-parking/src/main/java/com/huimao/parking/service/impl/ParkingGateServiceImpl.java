package com.huimao.parking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huimao.common.core.exception.ServiceException;
import com.huimao.common.core.utils.StringUtils;
import com.huimao.parking.constant.ParkingConstants;
import com.huimao.parking.domain.ParkingGate;
import com.huimao.parking.domain.ParkingLot;
import com.huimao.parking.mapper.ParkingGateMapper;
import com.huimao.parking.mapper.ParkingLotMapper;
import com.huimao.parking.service.IParkingGateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通道服务实现。
 */
@Service
public class ParkingGateServiceImpl extends AbstractParkingTenantService implements IParkingGateService
{
    @Autowired
    private ParkingGateMapper parkingGateMapper;

    @Autowired
    private ParkingLotMapper parkingLotMapper;

    /**
     * 分页查询通道。
     */
    @Override
    public IPage<ParkingGate> selectPage(ParkingGate query)
    {
        LambdaQueryWrapper<ParkingGate> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ParkingGate::getDelFlag, ParkingConstants.DEL_FLAG_NORMAL);
        Long tenantId = resolveQueryTenantId(query.getTenantId());
        if (tenantId != null)
        {
            wrapper.eq(ParkingGate::getTenantId, tenantId);
        }
        if (query.getLotId() != null)
        {
            wrapper.eq(ParkingGate::getLotId, query.getLotId());
        }
        if (StringUtils.isNotEmpty(query.getGateType()))
        {
            wrapper.eq(ParkingGate::getGateType, query.getGateType());
        }
        if (StringUtils.isNotEmpty(query.getGateName()))
        {
            wrapper.like(ParkingGate::getGateName, query.getGateName());
        }
        wrapper.orderByAsc(ParkingGate::getSortNum).orderByDesc(ParkingGate::getCreateTime);
        return parkingGateMapper.selectPage(wrapper);
    }

    /**
     * 查询通道详情。
     */
    @Override
    public ParkingGate selectById(Long gateId)
    {
        ParkingGate gate = parkingGateMapper.selectById(gateId);
        if (gate == null || ParkingConstants.DEL_FLAG_DELETED.equals(gate.getDelFlag()))
        {
            throw new ServiceException("通道不存在");
        }
        validateTenant(gate.getTenantId());
        return gate;
    }

    /**
     * 新增通道，所属租户以停车场为准。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insert(ParkingGate parkingGate)
    {
        ParkingLot lot = parkingLotMapper.selectById(parkingGate.getLotId());
        if (lot == null || ParkingConstants.DEL_FLAG_DELETED.equals(lot.getDelFlag()))
        {
            throw new ServiceException("停车场不存在");
        }
        validateTenant(lot.getTenantId());
        parkingGate.setTenantId(lot.getTenantId());
        parkingGate.setDelFlag(ParkingConstants.DEL_FLAG_NORMAL);
        if (parkingGateMapper.selectCount("gate_code", parkingGate.getGateCode()) > 0)
        {
            throw new ServiceException("通道编码已存在");
        }
        return parkingGateMapper.insert(parkingGate);
    }

    /**
     * 更新通道，但不允许修改其归属的停车场与租户。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(ParkingGate parkingGate)
    {
        ParkingGate db = selectById(parkingGate.getGateId());
        parkingGate.setTenantId(db.getTenantId());
        parkingGate.setLotId(db.getLotId());
        parkingGate.setDelFlag(db.getDelFlag());
        return parkingGateMapper.updateById(parkingGate);
    }

    /**
     * 批量逻辑删除通道。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(Long[] ids)
    {
        int rows = 0;
        for (Long id : ids)
        {
            ParkingGate gate = selectById(id);
            ParkingGate update = new ParkingGate();
            update.setGateId(gate.getGateId());
            update.setDelFlag(ParkingConstants.DEL_FLAG_DELETED);
            rows += parkingGateMapper.updateById(update);
        }
        return rows;
    }
}
