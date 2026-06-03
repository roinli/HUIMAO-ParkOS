<template>
  <div class="parking-statistics">
    <!-- 顶部筛选栏 -->
    <div class="statistics-header">
      <el-select
        v-model="parkingLot"
        placeholder="全部停车场"
        class="parking-select"
        @change="handleLotChange"
      >
        <el-option label="全部停车场" value="all"></el-option>
        <el-option
          v-for="item in lotOptions"
          :key="item.lotId"
          :label="item.lotName"
          :value="item.lotId"
        ></el-option>
      </el-select>

      <div class="parking-info">
        <span><img src="@/assets/images/dw.png" class="parking-img-1"/>{{ lotInfo.lotName || '全部停车场' }}</span>
        <span><img src="@/assets/images/tc.png" class="parking-img-2"/>车位{{ lotInfo.totalSpace || 0 }}</span>
        <span><img src="@/assets/images/in.png" class="parking-img-3"/> 入口{{ lotInfo.entryGateCount || 0 }}</span>
        <span><img src="@/assets/images/out.png" class="parking-img-4"/> 出口{{ lotInfo.exitGateCount || 0 }}</span>
      </div>
    </div>

    <!-- 今日核心数据 -->
    <div class="today-data">
      <div class="data-card">
        <div class="data-title">今日进场(辆)</div>
        <div class="data-value">{{ todayStats.entryCount || 0 }}</div>
        <div class="data-trend" :class="trendClass(todayStats.entryCompare)">
          <i :class="trendIcon(todayStats.entryCompare)"></i> 
          <div class="data-trend-value">{{ formatPercent(todayStats.entryCompare) }}</div>
          <div class="data-tips">较昨日</div>
        </div>
        <!-- <div class="chart-wrapper">
          <div id="in-car-chart" class="data-chart"></div>
        </div> -->
      </div>
      <div class="data-card">
        <div class="data-title">今日出场(辆)</div>
        <div class="data-value">{{ todayStats.exitCount || 0 }}</div>
        <div class="data-trend" :class="trendClass(todayStats.exitCompare)">
          <i :class="trendIcon(todayStats.exitCompare)"></i> 
          <div class="data-trend-value">{{ formatPercent(todayStats.exitCompare) }}</div>
          <div class="data-tips">较昨日</div>
        </div>
        <!-- <div class="chart-wrapper">
          <div id="out-car-chart" class="data-chart"></div>
        </div> -->
      </div>
      <div class="data-card">
        <div class="data-title">正在停车(辆)</div>
        <div class="data-value">{{ todayStats.inParkCount || 0 }}</div>
        <div class="data-desc">
            <div class="data-used-tips">车位使用率</div>
            <div class="data-used-value">{{ formatPercent(todayStats.spaceUseRate) }}</div>
        </div>
        <!-- <div class="chart-wrapper">
          <div id="parking-car-chart" class="data-chart"></div>
        </div> -->
      </div>
      <div class="data-card">
        <div class="data-title">今日收入(元)</div>
        <div class="data-value">{{ formatAmount(todayStats.incomeAmount) }}</div>
        <div class="data-trend" :class="trendClass(todayStats.incomeCompare)">
          <i :class="trendIcon(todayStats.incomeCompare)"></i> 
          <div class="data-trend-value">{{ formatPercent(todayStats.incomeCompare) }}</div>
          <div class="data-tips">较昨日</div>
        </div>
        <!-- <div class="chart-wrapper">
          <div id="income-chart" class="data-chart"></div>
        </div> -->
      </div>
    </div>

    <!-- 月度经营概况（ECharts 柱状图） -->
    <div class="monthly-overview">
      <div class="overview-title">月度经营概况</div>
      <div class="overview-content">
        <div class="overview-item">
          <div class="item-title">本月总收入(元)</div>
          <div class="item-value">{{ formatAmount(monthStats.incomeAmount) }}</div>
          <div class="item-trend" :class="trendClass(monthStats.incomeCompare)">
            <i :class="trendIcon(monthStats.incomeCompare)"></i> 
            <div class="item-trend-value">{{ formatPercent(monthStats.incomeCompare) }}</div>
            <div class="item-tips">较上月</div>
          </div>

          <div class="chart-container">
            <div id="month-income-chart" class="overview-chart"></div>
          </div>
        </div>

        <div class="overview-line"></div>

        <div class="overview-item">
          <div class="item-title">本月总笔数(笔)</div>
          <div class="item-value">{{ monthStats.orderCount || 0 }}</div>
          <div class="item-trend" :class="trendClass(monthStats.orderCompare)">
            <i :class="trendIcon(monthStats.orderCompare)"></i> 
            <div class="item-trend-value">{{ formatPercent(monthStats.orderCompare) }}</div>
            <div class="item-tips">较上月</div>
          </div>
          <div class="chart-container">
            <div id="month-count-chart" class="overview-chart"></div>
          </div>
        </div>

        <div class="overview-line"></div>

        <div class="overview-item">
          <div class="item-title">平均每笔(元)</div>
          <div class="item-value">{{ formatAmount(monthStats.avgOrderAmount) }}</div>
          <div class="item-trend" :class="trendClass(monthStats.avgOrderCompare)">
            <i :class="trendIcon(monthStats.avgOrderCompare)"></i> 
            <div class="item-trend-value">{{ formatPercent(monthStats.avgOrderCompare) }}</div>
            <div class="item-tips">较上月</div>
          </div>
          <div class="chart-container">
            <div id="month-average-chart" class="overview-chart"></div>
          </div>
        </div>

        <div class="overview-line"></div>

        <div class="overview-item">
          <div class="item-title">车位周转率</div>
          <div class="item-value">{{ formatAmount(monthStats.turnoverRate) }}</div>
          <div class="item-trend" :class="trendClass(monthStats.turnoverCompare)">
            <i :class="trendIcon(monthStats.turnoverCompare)"></i> 
            <div class="item-trend-value">{{ formatPercent(monthStats.turnoverCompare) }}</div>
            <div class="item-tips">较上月</div>
          </div>
          <div class="chart-container">
            <div id="month-turnover-chart" class="overview-chart"></div>
          </div>
        </div>
      </div>
    </div>

    <!-- 收费趋势图 -->
    <div class="fee-trend">
      <div class="trend-top">
        <div class="trend-title">收费趋势图</div>
        <div class="trend-header">

           <div class="time-tabs time-datetime">
              <span class="tab-item" :class="{active: dateType === 'week'}" @click="setDateRange('week')">近7天</span>
              <span class="tab-item" :class="{active: dateType === 'month'}" @click="setDateRange('month')">近30天</span>
              <span class="tab-item" :class="{active: dateType === 'custom'}" @click="setDateRange('custom')">自定义</span>

              <el-date-picker
                  @change="dateChange"
                  class="elDark"
                  v-model="query.duration"
                  format="yyyy-MM-dd"
                  type="datetimerange"
                  prefix-icon="el-icon-date"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  :default-time="['00:00:00', '23:59:59']"
              ></el-date-picker>

            </div>

            <div class="refresh-box" @click="handleRefresh">
              <img src="@/assets/images/refresh.png" :class="isRefresh ? 'rotate-once' : ''"/>
            </div>
        </div>
      </div>

      <div class="trend-chart">
        <div id="feeTrendChart" class="line-chart-container"></div>
      </div>

    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import { mapGetters } from 'vuex'; // 保留你原有代码的vuex引入
import moment from "moment";
import { getDashboard } from '@/api/parking/stat'

export default {
  name: "ParkingStatistics",
  data() {
    return {
      parkingLot: "all",
      lotOptions: [],
      lotInfo: {
        lotName: '全部停车场',
        totalSpace: 0,
        entryGateCount: 0,
        exitGateCount: 0
      },
      todayStats: {
        entryCount: 0,
        exitCount: 0,
        inParkCount: 0,
        incomeAmount: 0,
        entryCompare: 0,
        exitCompare: 0,
        incomeCompare: 0,
        spaceUseRate: 0
      },
      monthStats: {
        incomeAmount: 0,
        orderCount: 0,
        avgOrderAmount: 0,
        turnoverRate: 0,
        incomeCompare: 0,
        orderCompare: 0,
        avgOrderCompare: 0,
        turnoverCompare: 0,
        xData: [],
        incomeData: [],
        orderData: [],
        avgOrderData: [],
        turnoverData: []
      },
      // ECharts实例存储
      charts: {},
      // 24小时时间轴数据
      hourXData: ['00', '01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', 
                  '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23'],
      // 模拟24小时数据
      hourData: {
        inCar: [5, 3, 2, 1, 2, 4, 6, 8, 10, 9, 8, 7, 9, 10, 8, 7, 9, 11, 8, 6, 5, 4, 3, 2],
        outCar: [3, 2, 1, 2, 1, 3, 5, 7, 9, 8, 7, 6, 8, 9, 7, 6, 8, 10, 7, 5, 4, 3, 2, 1],
        parkingCar: [2, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
        income: [20, 15, 10, 5, 10, 25, 40, 60, 80, 75, 70, 65, 75, 85, 70, 65, 75, 90, 70, 50, 40, 30, 20, 15]
      },
      trendData: {
        date: [],
        money: []
      },
      dateType: 'week', // 默认选中近七日
      query: {
        duration: [],
        startTime: '',
        endTime: '',
      },
      isRefresh:false,

    };
  },
  computed: {
    ...mapGetters(['sidebar']), // 保留你原有代码的sidebar计算属性
  },
  mounted() {
    // 监听窗口变化，自适应图表
    window.addEventListener('resize', this.resizeCharts);

    //初始化收入趋势图
    this.setDateRange('week')
  },
  beforeDestroy() {
    // 销毁图表实例，避免内存泄漏
    Object.values(this.charts).forEach(chart => chart.dispose());
    window.removeEventListener('resize', this.resizeCharts);
  },
  methods: {
    trendClass(value) {
      return Number(value) >= 0 ? 'add' : 'reduce'
    },
    trendIcon(value) {
      return Number(value) >= 0 ? 'el-icon-top' : 'el-icon-bottom'
    },
    formatPercent(value) {
      const num = Number(value || 0)
      return `${Math.abs(num).toFixed(2)}%`
    },
    formatAmount(value) {
      return Number(value || 0).toFixed(2)
    },
    handleLotChange() {
      this.handleSearch()
    },
    loadDashboard() {
      const params = {
        lotId: this.parkingLot === 'all' ? undefined : this.parkingLot,
        startTime: this.query.startTime,
        endTime: this.query.endTime
      }
      getDashboard(params).then(res => {
        const data = res.data || {}
        this.lotOptions = data.lotOptions || []
        this.lotInfo = data.lotInfo || this.lotInfo
        this.todayStats = data.today || this.todayStats
        this.monthStats = data.month || this.monthStats
        this.trendData = data.trend || this.trendData
        this.$nextTick(() => {
          this.initMonthCharts()
          this.initFeeTrendChart()
        })
      })
    },
    // 初始化24小时折线图（今日核心数据）
    // initHourCharts() {
    //   // 进场车辆折线图
    //   this.charts.inCar = echarts.init(document.getElementById('in-car-chart'));
    //   this.charts.inCar.setOption({
    //     grid: { top: 0, right: 0, bottom: 10, left: 0, containLabel: true },
    //     xAxis: {
    //       type: 'category',
    //       data: this.hourXData,
    //       axisLine: { show: false },
    //       axisTick: { show: false },
    //       axisLabel: { show: false },
    //       splitLine: { show: false }
    //     },
    //     yAxis: {
    //       type: 'value',
    //       axisLine: { show: false },
    //       axisTick: { show: false },
    //       axisLabel: { show: false },
    //       splitLine: { show: false }
    //     },
    //     series: [{
    //       data: this.hourData.inCar,
    //       type: 'line',
    //       smooth: true,
    //       lineStyle: { color: '#67c23a', width: 2 },
    //       areaStyle: { 
    //         color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    //           { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
    //           { offset: 1, color: 'rgba(103, 194, 58, 0)' }
    //         ])
    //       },
    //       symbol: 'none'
    //     }],
    //     tooltip: {
    //       trigger: 'axis',
    //       formatter: '{b}时: {c}辆'
    //     }
    //   });

    //   // 出场车辆折线图
    //   this.charts.outCar = echarts.init(document.getElementById('out-car-chart'));
    //   this.charts.outCar.setOption({
    //     grid: { top: 0, right: 0, bottom: 10, left: 0, containLabel: true },
    //     xAxis: { type: 'category', data: this.hourXData, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { show: false }, splitLine: { show: false } },
    //     yAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, axisLabel: { show: false }, splitLine: { show: false } },
    //     series: [{
    //       data: this.hourData.outCar,
    //       type: 'line',
    //       smooth: true,
    //       lineStyle: { color: '#e6a23c', width: 2 },
    //       areaStyle: { 
    //         color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    //           { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
    //           { offset: 1, color: 'rgba(230, 162, 60, 0)' }
    //         ])
    //       },
    //       symbol: 'none'
    //     }],
    //     tooltip: { trigger: 'axis', formatter: '{b}时: {c}辆' }
    //   });

    //   // 正在停车折线图
    //   this.charts.parkingCar = echarts.init(document.getElementById('parking-car-chart'));
    //   this.charts.parkingCar.setOption({
    //     grid: { top: 0, right: 0, bottom: 10, left: 0, containLabel: true },
    //     xAxis: { type: 'category', data: this.hourXData, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { show: false }, splitLine: { show: false } },
    //     yAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, axisLabel: { show: false }, splitLine: { show: false } },
    //     series: [{
    //       data: this.hourData.parkingCar,
    //       type: 'line',
    //       smooth: true,
    //       lineStyle: { color: '#e6a23c', width: 2 },
    //       areaStyle: { 
    //         color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    //           { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
    //           { offset: 1, color: 'rgba(230, 162, 60, 0)' }
    //         ])
    //       },
    //       symbol: 'none'
    //     }],
    //     tooltip: { trigger: 'axis', formatter: '{b}时: {c}辆' }
    //   });

    //   // 今日收入折线图
    //   this.charts.income = echarts.init(document.getElementById('income-chart'));
    //   this.charts.income.setOption({
    //     grid: { top: 0, right: 0, bottom: 10, left: 0, containLabel: true },
    //     xAxis: { type: 'category', data: this.hourXData, axisLine: { show: false }, axisTick: { show: false }, axisLabel: { show: false }, splitLine: { show: false } },
    //     yAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, axisLabel: { show: false }, splitLine: { show: false } },
    //     series: [{
    //       data: this.hourData.income,
    //       type: 'line',
    //       smooth: true,
    //       lineStyle: { color: '#e6a23c', width: 2 },
    //       areaStyle: { 
    //         color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    //           { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
    //           { offset: 1, color: 'rgba(230, 162, 60, 0)' }
    //         ])
    //       },
    //       symbol: 'none'
    //     }],
    //     tooltip: { trigger: 'axis', formatter: '{b}时: {c}元' }
    //   });
    // },

    // 初始化月度柱状图（月度经营概况）
    initMonthCharts() {
      const monthXData = this.monthStats.xData || []
      const commonMonthOption = {
        xAxis: {
          type: 'category',
          show: false,
          data: monthXData,
          boundaryGap: true
        },
        yAxis: {
          type: 'value',
          show: false
        },
        grid: {
          top: 0,
          bottom: 0,
          left: 0,
          right: 0,
        },
        tooltip: {
          show: true,
          trigger: 'axis',
          axisPointer: {
            type: "line",
            lineStyle: {
              color: '#eee'
            }
          },
          transitionDuration: 0,
          backgroundColor: 'white',
          extraCssText: 'box-shadow: 3px 3px 3px rgba(100, 100, 100, 0.3);padding:10px',
          textStyle: {
            color: 'black'
          }
        }
      };

      // ========== 1. 本月总收入柱状图 ==========
      this.charts.monthIncome = echarts.init(document.getElementById('month-income-chart'));
      const incomeOption = {
        ...commonMonthOption, // 继承公共配置
        series: [
          {
            data: this.monthStats.incomeData || [],
            type: 'bar',
            barWidth: 6,
            barGap: '0%',
            itemStyle: {
              color: {
                type: 'linear', // 线性渐变
                x: 0, y: 0,     // 起点：顶部
                x2: 0, y2: 1,   // 终点：底部
                colorStops: [
                  { offset: 0, color: '#2E6EEF' }, // 顶部亮色
                  { offset: 1, color: '#3E9AF9' }  // 底部暗色
                ]
              },
              barBorderRadius: [10, 10, 0, 0]
            }
          }
        ],
      };
      this.charts.monthIncome.setOption(incomeOption);


      // ========== 2. 本月总笔数柱状图 ==========
      this.charts.monthCount = echarts.init(document.getElementById('month-count-chart'));
      const countOption = {
        ...commonMonthOption, // 继承公共配置
        series: [
          {
            data: this.monthStats.orderData || [],
            type: 'bar',
            barWidth: 6,
            barGap: '0%',
            itemStyle: {
              color: {
                type: 'linear', // 线性渐变
                x: 0, y: 0,     // 起点：顶部
                x2: 0, y2: 1,   // 终点：底部
                colorStops: [
                  { offset: 0, color: '#3EC9DC' }, // 顶部亮色
                  { offset: 1, color: '#46D4DC' }  // 底部暗色
                ]
              },
              barBorderRadius: [10, 10, 0, 0] // 统一圆角风格
            }
          }
        ],
      };
      this.charts.monthCount.setOption(countOption);

      // ========== 3. 平均每笔柱状图 ==========
      this.charts.monthAverage = echarts.init(document.getElementById('month-average-chart'));
      const averageOption = {
        ...commonMonthOption, // 继承公共配置
        series: [
          {
            data: this.monthStats.avgOrderData || [],
            type: 'bar',
            barWidth: 6,
            barGap: '0%',
            itemStyle: {
              color: {
                type: 'linear', // 线性渐变
                x: 0, y: 0,     // 起点：顶部
                x2: 0, y2: 1,   // 终点：底部
                colorStops: [
                  { offset: 0, color: '#867EEC' }, // 顶部亮色
                  { offset: 1, color: '#9789EC' }  // 底部暗色
                ]
              },
              barBorderRadius: [10, 10, 0, 0] // 统一圆角风格
            }
          }
        ],
      };
      this.charts.monthAverage.setOption(averageOption);

      // ========== 4. 车位周转率柱状图 ==========
      this.charts.monthTurnover = echarts.init(document.getElementById('month-turnover-chart'));
      const turnoverOption = {
        ...commonMonthOption, // 继承公共配置
        series: [
          {
            data: this.monthStats.turnoverData || [],
            type: 'bar',
            barWidth: 6,
            barGap: '0%',
            itemStyle: {
              color: {
                type: 'linear', // 线性渐变
                x: 0, y: 0,     // 起点：顶部
                x2: 0, y2: 1,   // 终点：底部
                colorStops: [
                  { offset: 0, color: '#FCBC3D' }, // 顶部亮色
                  { offset: 1, color: '#FCCD48' }  // 底部暗色
                ]
              },
              barBorderRadius: [10, 10, 0, 0] // 统一圆角风格
            }
          }
        ],
      };
      this.charts.monthTurnover.setOption(turnoverOption);
    },

    // 收费趋势折线图
    initFeeTrendChart() {
      this.charts.feeTrend = echarts.init(document.getElementById('feeTrendChart'));
      this.charts.feeTrend.setOption({
        tooltip: {
          trigger: "axis",
          axisPointer: {
            type: "line",
            lineStyle: {
              color: '#eee'
            },
          },
          show: true,
          backgroundColor: 'white',
          extraCssText: 'box-shadow: 3px 3px 3px rgba(100, 100, 100, 0.3);;padding:10px',
          textStyle: {
            color: 'black'
          }
        },
        grid: {
          left: "3%",
          right: "4%",
          bottom: "8%",
          top: '30%',
          containLabel: true,
        },
        yAxis: {
          type: "value",
          axisLine: {
            lineStyle: {
              color: "#ececec",
            },
          },
          axisTick: {
            show: false,
          },
          axisLabel: {
            color: "#999",
          },
        },
        xAxis: {
          type: "category",
          data: this.trendData.date,
          axisLine: {
            lineStyle: {
              color: "#ececec",
            },
          },
          axisTick: {
            show: false,
          },
          axisLabel: {
            color: "#999",
          },
        },
        series: [
          {
            name: "缴费金额",
            type: "line",
            smooth: true,
            symbolSize: [10, 10],
            data: this.trendData.money,
            lineStyle: {
              width: 3,
              color: "#4784FF",
            },
            itemStyle: {
              color: "#4784FF",
            },
          },
        ],
      });

    },

    // 图表自适应
    resizeCharts() {
      Object.values(this.charts).forEach(chart => chart.resize());
    },

    // 快捷时间选择
    setDateRange(type) {
      this.dateType = type
      const end = moment().format("YYYY-MM-DD 23:59:59");
      let start = "";

      if (type === "week") {
        start = moment().subtract(7, "days").format("YYYY-MM-DD 00:00:00");
      } else if (type === "half") {
        start = moment().subtract(15, "days").format("YYYY-MM-DD 00:00:00");
      } else if (type === "month") {
        start = moment().subtract(30, "days").format("YYYY-MM-DD 00:00:00");
      } else if (type === "year") {
        start = moment().subtract(1, "years").format("YYYY-MM-DD 00:00:00");
      } else if (type === "custom") {
        // 自定义清空时间，手动选择
        this.query.duration = [];
        return;
      }

      this.query.duration = [start, end];
      this.handleSearch();
    },

    // 手动选择日期 自动切为自定义选中
    dateChange() {
      if(this.query.duration && this.query.duration.length > 0){
        this.dateType = 'custom'
      }
      this.handleSearch()
    },

    handleSearch(){
        this.query.startTime = this.query.duration && this.query.duration[0] ? this.query.duration[0] : ''
        this.query.endTime = this.query.duration && this.query.duration[1] ? this.query.duration[1] : ''
        this.loadDashboard()
    },

    handleRefresh(){
      this.isRefresh = true
      this.dateType = 'week';
      this.query.duration = [];
      this.query.startTime = '';
      this.query.endTime = '';
      this.setDateRange('week')

       this.$nextTick(()=>{
       
        // 动画跑完关闭
        setTimeout(() => {
          this.isRefresh = false
        }, 800)
       
      })
 

    }

  }
};
</script>

<style scoped lang="scss">
// 全局样式
.parking-statistics {
  width: 100%;
  padding: 0px 20px 20px 20px;
  background: #f5f7fa;
  font-size: 14px;
  color: #333;
}

// 顶部筛选栏
.statistics-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 0px 15px;
  border-radius: 4px;
  height: 64px;

  .parking-select {
    width: 166px;
    height: 36px;
    margin-right: 39px;
  }

  .parking-info {
    display: flex;
    gap: 29px;
    color: #666;

    .parking-img-1{
      width: 14px;
      height: 16px;
      margin-right:9px;
    }

    .parking-img-2{
      width: 11px;
      height: 16px;
      margin-right:19px;
    }

    .parking-img-3{
      width: 16px;
      height: 16px;
      margin-right:19px;
    }

    .parking-img-4{
      width: 16px;
      height: 16px;
      margin-right:19px;
    }

    span {
      font-weight: 400;
      font-size: 13px;
      color: #666666;
      display: flex;
      align-items: center;

      i {
        color: #409eff;
      }
    }
  }
}

// 今日核心数据（适配ECharts）
.today-data {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;

  .data-card {
    flex: 1;
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    position: relative;

    .data-title {
      font-weight: 400;
      font-size: 13px;
      color: #666666;
    }

    .data-value {
      font-weight: bold;
      font-size: 30px;
      color: #090909;
      margin-bottom: 12px;
      margin-top: 15px;
    }

    .data-trend,
    .data-desc {
      font-weight: 400;
      font-size: 13px;
      display: flex;
      align-items: center;
      i {
        margin-right: 3px;
      }
    }

    .data-used-tips{
      font-weight: 400;
      font-size: 13px;
      color: #666666;
      line-height: 13px;
    }

    .data-used-value{
      font-weight: 400;
      font-size: 13px;
      color: #5168F2;
      margin-left: 4px;
    }

    .data-tips{
      font-weight: 400;
      font-size: 13px;
      color: #666666;
      margin-left: 4px;
      line-height: 13px;
    }

    .data-trend-value{
      font-weight: 400;
      font-size: 13px;
    }

    .reduce {
      color: #FF3E1D;
    }

    .add {
      color: #05CA00;
    }

    .chart-wrapper {
      width: 100%;
      height: 40px;
      position: absolute;
      bottom: 20px;
      right: 20px;
      left: 20px;
    }

    .data-chart {
      width: 100%;
      height: 100%;
    }
  }
}

// 月度经营概况（适配ECharts）
.monthly-overview {
  background: #fff;
  padding: 20px 0px;
  border-radius: 8px;
  margin-bottom: 20px;

  .overview-title {
    font-weight: 600;
    font-size: 14px;
    color: #292929;
    margin-bottom: 30px;
    display: flex;
    align-items: center;
    margin-left:20px;


    &::before {
      content: "";
      width: 5px;
      height: 14px;
      background: #436EFE;
      margin-right: 12px;
      border-radius: 4px;
    }
  }

  .overview-content {
    display: flex;
    flex-wrap: nowrap;
    width: 100%;

    .overview-item {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 0px 20px;
      box-sizing: border-box; 
      position: relative;

      .item-title {
        font-weight: 400;
        font-size: 13px;
        color: #666666;
      }

      .item-value {
        font-weight: bold;
        font-size: 30px;
        color: #090909;
        margin-bottom: 12px;
        margin-top: 15px;
      }

      .item-trend,
      .item-desc {
        display:flex;
        font-size: 12px;
        margin-bottom: 10px;

        i {
          margin-right: 3px;
        }
      }

      .item-used-tips{
        font-weight: 400;
        font-size: 13px;
        color: #666666;
        line-height: 13px;
      }

      .item-used-value{
        font-weight: 400;
        font-size: 13px;
        color: #5168F2;
        margin-left: 4px;
      }

      .item-tips{
        font-weight: 400;
        font-size: 13px;
        color: #666666;
        margin-left: 4px;
        line-height: 13px;
      }

      .item-trend-value{
        font-weight: 400;
        font-size: 13px;
      }

      .reduce {
        color: #FF3E1D;
      }

      .add {
        color: #05CA00;
      }

      .chart-container {
        width: 100%;
        height: 91px; // 柱状图高度
        margin-top: 20px;
      }

      .overview-chart {
        width: 100%;
        height: 100%;
      }
    }

    .overview-line{
        width:1px;
        height:218px;
        background-color: #E2EBF0;
    }


  }
}

.fee-trend {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  .trend-top{ display:flex;justify-content:space-between; }
  .trend-title {
    font-weight:600;font-size:14px;color:#292929;margin-bottom:30px;display:flex;align-items:center;
    &::before { content:"";width:5px;height:14px;background:#436EFE;margin-right:12px;border-radius:4px; }
  }
  .trend-header {
    display:flex;align-items:center;justify-content:flex-end;
    gap:15px;margin-bottom:15px;
  }
  .trend-chart {
    width: 100%;
    height: 300px;
  }
  .line-chart-container {
    width: 100%;
    height: 100%;
  }
}



// 响应式适配
@media (max-width: 1440px) {
  .today-data {
    flex-wrap: wrap;

    .data-card {
      /*flex: 0 0 calc(25% - 10px);*/
    }
  }

  .monthly-overview .overview-content {
    flex-wrap: wrap;

    .overview-item {
      /*flex: 0 0 calc(50% - 10px);*/
    }
  }
}

@media (max-width: 768px) {
  .today-data .data-card,
  .monthly-overview .overview-content .overview-item {
    flex: 0 0 100%;
  }

  .statistics-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;

    .parking-info {
      flex-wrap: wrap;
      gap: 10px;
    }
  }
}

.time-tabs {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: 4px;
  border: 1px solid #EBEBEF;
  height: 36px;
  width: fit-content;

  .tab-item {
    padding: 10px 21px;
    font-weight: 400;
    font-size: 13px;
    color: #9A9A9A;
    cursor: pointer;
    transition: all 0.2s;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-right: 1px solid #EBEBEF;

    min-width: 70px;     
    white-space: nowrap;   
    overflow: hidden;     
    text-overflow: ellipsis;
  }
  .tab-item:first-child{
    border-top-left-radius: 5px;
    border-bottom-left-radius: 5px;
  }
  .tab-item:last-child{
    border-right: 0px solid #EBEBEF;
    border-top-right-radius: 5px;
    border-bottom-right-radius: 5px;
  }
  .tab-item:hover {
    color: #435971;
  }
  .tab-item.active {
    background: #5F7DF9;
    color: #fff;
  }
}


.refresh-box {
  margin-left: 22px;
  justify-content: center;
  text-align: center;
  cursor: pointer
}

/* 刷新按钮旋转一圈动画 */
@keyframes rotateOnce {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.rotate-once {
  animation: rotateOnce 1s linear forwards;
  display: inline-block;
}


/*select，daterange组件样式调整*/
.time-datetime ::v-deep .el-range-editor {
  border: none !important;
  border-radius: 5px;
  background: #fff !important;
}

// 覆盖原有日期宽度样式
.time-datetime ::v-deep .el-range-editor--medium.el-input__inner {
  width: 250px !important;
  height: 32px;
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
}

.time-datetime ::v-deep .el-range-input {
  border: none !important;
  background: transparent !important;
}
.time-datetime ::v-deep .el-range-separator {
  border: none !important;
  background: transparent !important;
}


</style>
