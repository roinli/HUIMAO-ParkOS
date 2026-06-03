package com.huimao.common.log.service;

import com.huimao.common.security.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.huimao.common.core.constant.SecurityConstants;
import com.huimao.system.api.RemoteLogService;
import com.huimao.system.api.domain.SysOperLog;

/**
 * 异步调用日志服务
 *
 * @author huimao
 */
@Service
public class AsyncLogService
{
    @Autowired
    private RemoteLogService remoteLogService;

    /**
     * 保存系统日志记录
     */
    @Async
    public void saveSysLog(SysOperLog sysOperLog) throws Exception {
        sysOperLog.setTenantId(SecurityUtils.getTenantId());
        remoteLogService.saveLog(sysOperLog, SecurityConstants.INNER);
    }
}
