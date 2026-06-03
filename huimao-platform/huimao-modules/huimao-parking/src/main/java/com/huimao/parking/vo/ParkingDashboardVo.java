package com.huimao.parking.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 首页停车统计看板数据。
 */
@ApiModel("首页停车统计看板数据")
@Data
public class ParkingDashboardVo
{
    @ApiModelProperty("停车场下拉列表")
    private List<LotOption> lotOptions = new ArrayList<>();

    @ApiModelProperty("当前停车场信息")
    private LotInfo lotInfo = new LotInfo();

    @ApiModelProperty("今日核心数据")
    private TodayStats today = new TodayStats();

    @ApiModelProperty("月度经营概况")
    private MonthStats month = new MonthStats();

    @ApiModelProperty("收费趋势")
    private TrendStats trend = new TrendStats();

    @Data
    public static class LotOption
    {
        private Long lotId;
        private String lotName;
    }

    @Data
    public static class LotInfo
    {
        private Long lotId;
        private String lotName;
        private Integer totalSpace = 0;
        private Integer entryGateCount = 0;
        private Integer exitGateCount = 0;
    }

    @Data
    public static class TodayStats
    {
        private Integer entryCount = 0;
        private Integer exitCount = 0;
        private Integer inParkCount = 0;
        private BigDecimal incomeAmount = BigDecimal.ZERO;
        private BigDecimal entryCompare = BigDecimal.ZERO;
        private BigDecimal exitCompare = BigDecimal.ZERO;
        private BigDecimal incomeCompare = BigDecimal.ZERO;
        private BigDecimal spaceUseRate = BigDecimal.ZERO;
    }

    @Data
    public static class MonthStats
    {
        private BigDecimal incomeAmount = BigDecimal.ZERO;
        private Integer orderCount = 0;
        private BigDecimal avgOrderAmount = BigDecimal.ZERO;
        private BigDecimal turnoverRate = BigDecimal.ZERO;
        private BigDecimal incomeCompare = BigDecimal.ZERO;
        private BigDecimal orderCompare = BigDecimal.ZERO;
        private BigDecimal avgOrderCompare = BigDecimal.ZERO;
        private BigDecimal turnoverCompare = BigDecimal.ZERO;
        private List<String> xData = new ArrayList<>();
        private List<BigDecimal> incomeData = new ArrayList<>();
        private List<Integer> orderData = new ArrayList<>();
        private List<BigDecimal> avgOrderData = new ArrayList<>();
        private List<BigDecimal> turnoverData = new ArrayList<>();
    }

    @Data
    public static class TrendStats
    {
        private List<String> date = new ArrayList<>();
        private List<BigDecimal> money = new ArrayList<>();
    }
}
