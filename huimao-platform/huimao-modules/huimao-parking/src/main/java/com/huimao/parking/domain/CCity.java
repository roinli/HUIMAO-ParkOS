package com.huimao.parking.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 省市区字典。
 */
@Data
@TableName("c_city")
public class CCity
{
    @TableField("Id")
    private Integer id;

    @TableField("Name")
    private String name;

    @TableField("Pid")
    private Integer pid;

    private Long tenantId;
}
