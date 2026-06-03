package com.huimao.parking.service.impl;

import com.huimao.common.core.exception.ServiceException;
import com.huimao.common.security.utils.SecurityUtils;

/**
 * 停车模块租户辅助基类。
 *
 * <p>因为停车模块同时存在后台接口和开放接口，不能完全依赖统一租户插件，
 * 所以这里集中处理“当前登录租户”“平台管理员是否可跨租户操作”等规则。</p>
 */
public abstract class AbstractParkingTenantService
{
    /**
     * 判断当前登录人是否为平台管理员。
     */
    protected boolean isAdmin()
    {
        return SecurityUtils.isAdmin(SecurityUtils.getUserId());
    }

    /**
     * 获取当前登录人的租户 ID。
     */
    protected Long currentTenantId()
    {
        return SecurityUtils.getTenantId();
    }

    /**
     * 创建数据时解析最终租户。
     *
     * <p>平台管理员必须显式指定租户，租户管理员则固定落到自己的租户。</p>
     */
    protected Long resolveCreateTenantId(Long tenantId)
    {
        if (isAdmin())
        {
            if (tenantId == null)
            {
                throw new ServiceException("平台管理员新增数据时必须指定租户");
            }
            return tenantId;
        }
        return currentTenantId();
    }

    /**
     * 查询数据时解析租户条件。
     *
     * <p>平台管理员允许跨租户查询；租户管理员只能查本租户。</p>
     */
    protected Long resolveQueryTenantId(Long tenantId)
    {
        if (isAdmin())
        {
            return tenantId;
        }
        return currentTenantId();
    }

    /**
     * 校验目标数据是否属于当前可访问租户。
     */
    protected void validateTenant(Long tenantId)
    {
        if (!isAdmin() && tenantId != null && !tenantId.equals(currentTenantId()))
        {
            throw new ServiceException("无权访问当前数据");
        }
    }
}
