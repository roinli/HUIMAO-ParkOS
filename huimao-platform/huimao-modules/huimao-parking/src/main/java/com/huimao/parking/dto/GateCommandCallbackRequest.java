package com.huimao.parking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 指令回调
 */
@ApiModel("开闸指令回调请求")
@Data
public class GateCommandCallbackRequest
{
    /**
     * 下发给设备的指令流水号。
     */
    @ApiModelProperty(value = "指令流水号", required = true, example = "PC20260423123000123456")
    @NotBlank(message = "指令号不能为空")
    private String commandNo;

    /**
     * 执行结果，true 成功，false 失败。
     */
    @ApiModelProperty(value = "执行结果", required = true, example = "true")
    @NotNull(message = "执行结果不能为空")
    private Boolean success;

    /**
     * 设备回传结果的时间。
     */
    @ApiModelProperty(value = "回调时间", example = "2026-04-23 12:36:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date callbackTime;

    /**
     * 回调原始内容。
     */
    @ApiModelProperty(value = "回调原始内容")
    private String rawContent;
}
