package com.huimao.modules.monitor.controller;


import com.huimao.common.core.web.domain.AjaxResult;
import com.huimao.modules.monitor.oshi.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控
 *
 * @author huimao
 */
@Slf4j
@RestController
@RequestMapping("/server")
public class ServerController
{
    @GetMapping()
    public AjaxResult getInfo() throws Exception
    {
        Server server = new Server();
        server.copyTo();
        return AjaxResult.success(server);
    }
}
