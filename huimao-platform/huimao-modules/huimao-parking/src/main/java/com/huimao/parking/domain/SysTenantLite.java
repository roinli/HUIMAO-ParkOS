package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 租户轻量对象
 */
@ApiModel("租户轻量信息")
@Data
@TableName("sys_tenant")
public class SysTenantLite
{
    @TableId
    @ApiModelProperty(value = "租户ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "租户名称", example = "演示租户")
    private String tenantName;
}
