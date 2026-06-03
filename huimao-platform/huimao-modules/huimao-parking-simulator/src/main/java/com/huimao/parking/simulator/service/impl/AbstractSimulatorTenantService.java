package com.huimao.parking.simulator.service.impl;

import com.huimao.common.core.exception.ServiceException;
import com.huimao.common.security.utils.SecurityUtils;

/**
 * 模拟器模块租户辅助基类。
 */
public abstract class AbstractSimulatorTenantService
{
    protected boolean isAdmin()
    {
        return SecurityUtils.isAdmin(SecurityUtils.getUserId());
    }

    protected Long currentTenantId()
    {
        return SecurityUtils.getTenantId();
    }

    protected Long resolveQueryTenantId(Long tenantId)
    {
        if (isAdmin())
        {
            return tenantId;
        }
        return currentTenantId();
    }

    protected void validateTenant(Long tenantId)
    {
        if (!isAdmin() && tenantId != null && !tenantId.equals(currentTenantId()))
        {
            throw new ServiceException("无权访问当前数据");
        }
    }
}
