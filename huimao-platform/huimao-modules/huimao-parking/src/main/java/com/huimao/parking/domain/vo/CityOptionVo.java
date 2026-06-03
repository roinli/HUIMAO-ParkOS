package com.huimao.parking.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 行政区划下拉选项。
 */
@ApiModel("行政区划下拉选项")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityOptionVo
{
    @ApiModelProperty("编码")
    private String id;

    @ApiModelProperty("父级编码")
    private String pid;

    @ApiModelProperty("名称")
    private String name;
}
