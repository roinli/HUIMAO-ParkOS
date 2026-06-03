package com.huimao.system.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.huimao.common.core.utils.poi.ExcelUtil;
import com.huimao.common.core.web.controller.BaseController;
import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.common.log.annotation.Log;
import com.huimao.common.log.enums.BusinessType;
import com.huimao.common.security.annotation.InnerAuth;
import com.huimao.common.security.annotation.RequiresPermissions;
import com.huimao.system.api.domain.SysLogininfor;
import com.huimao.system.service.ISysLogininforService;
import com.huimao.common.redis.service.RedisService;
import com.huimao.common.core.constant.CacheConstants;



/**
 * 系统访问记录
 *
 * @author huimao
 */
@RestController
@RequestMapping("/logininfor")
public class SysLogininforController extends BaseController
{
    @Autowired
    private ISysLogininforService logininforService;

    @Autowired
    private RedisService redisService;


    @RequiresPermissions("system:logininfor:list")
    @GetMapping("/list")
    public AjaxResult list(SysLogininfor logininfor)
    {
        IPage<SysLogininfor> list = logininforService.selectLogininforPage(logininfor);
        return AjaxResult.success(list);
    }

    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:logininfor:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysLogininfor logininfor)
    {
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
        util.exportExcel(response, list, "登录日志");
    }

    @RequiresPermissions("system:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public AjaxResult remove(@PathVariable Long[] infoIds)
    {
        return toAjax(logininforService.deleteLogininforByIds(infoIds));
    }

    @RequiresPermissions("system:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        logininforService.cleanLogininfor();
        return success();
    }

    @RequiresPermissions("system:logininfor:unlock")
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @GetMapping("/unlock/{userName}")
    public AjaxResult unlock(@PathVariable("userName") String userName)
    {
        redisService.deleteObject(CacheConstants.PWD_ERR_CNT_KEY + userName);
        return success();
    }


    @InnerAuth
    @PostMapping
    public AjaxResult add(@RequestBody SysLogininfor logininfor)
    {
        return toAjax(logininforService.insertLogininfor(logininfor));
    }

    /**
     * 查询统计信息
     * @return
     */
    @GetMapping("/logincount")
    public AjaxResult logincount()
    {
        return logininforService.logincount();
    }
}
