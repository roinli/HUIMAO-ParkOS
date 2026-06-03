<template>
  <div class="my-app-container">
    <!-- 搜索区域 -->
    <search-form
      :labelWidth="90"
      v-model="queryParams"
      :forms="searchModels"
      :loading="loading"
      @search="handleQuery"
      @reset="resetQuery"
    >
      <!-- 自定义按钮插槽（如果 search-form 支持） -->
      <template #extraButtons>
        <el-button type="primary" plain icon="el-icon-refresh-right" @click="handleRefresh">刷新统计</el-button>
      </template>
    </search-form>

    <!-- 统计概览卡片 -->
    <el-row :gutter="10" class="summary-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="summary-card income">
          <div class="summary-info">
            <div class="summary-label">统计周期收入</div>
            <div class="summary-value">￥{{ overview.totalIncome }}</div>
            <div class="summary-trend">
              <span class="trend-up">
                <i class="el-icon-top" /> 实时累计
              </span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="summary-card entry">
          <div class="summary-info">
            <div class="summary-label">总进场数</div>
            <div class="summary-value">{{ overview.totalEntry }}</div>
            <div class="summary-trend">
              <span>车辆入场总量</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="summary-card exit">
          <div class="summary-info">
            <div class="summary-label">总出场数</div>
            <div class="summary-value">{{ overview.totalExit }}</div>
            <div class="summary-trend">
              <span>车辆离场总量</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="summary-card avg">
          <div class="summary-info">
            <div class="summary-label">平均每单</div>
            <div class="summary-value">￥{{ overview.avgOrder }}</div>
            <div class="summary-trend">
              <span>单均消费金额</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 表格区域 -->
    <table-container
      :data="statList"
      :columns="columns"
      :loading="loading"
      :total="total"
      :pageSize="queryParams.pageSize"
      :currentPage="queryParams.pageNum"
      @pageChange="pageChange"
    >
      <!-- 停车场名称 -->
      <template #lotId="{ scope }">
        <span>{{ getLotName(scope.row.lotId) }}</span>
      </template>

      <!-- 周转率 -->
      <template #turnoverRate="{ scope }">
        <span>{{ scope.row.turnoverRate }} 次/位</span>
      </template>
    </table-container>
  </div>
</template>

<script>
import { listLot } from '@/api/parking/lot'
import { listStat, getStatOverview, refreshStat } from '@/api/parking/stat'

export default {
  name: 'ParkingStat',
  data() {
    return {
      loading: false,
      total: 0,
      lotOptions: [],
      statList: [],
      overview: {
        totalIncome: '0.00',
        totalEntry: 0,
        totalExit: 0,
        avgOrder: '0.00'
      },

      // 查询条件
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lotId: undefined,
        dateRange: []  // 辅助字段
      },

      // 搜索表单配置
      searchModels: [
        {
          label: '所属停车场',
          modelValue: 'lotId',
          itemType: 'select',
          placeholder: '请选择停车场',
          clearable: true,
          filterable: true,
          options: [],
          valueKey: 'lotId',
          labelKey: 'lotName'
        },
        {
          label: '统计日期',
          modelValue: 'dateRange',
          itemType: 'daterange',
          placeholder: '请选择统计日期',
          clearable: true,
          valueFormat: 'yyyy-MM-dd',
          rangeSeparator: '-',
          startPlaceholder: '开始日期',
          endPlaceholder: '结束日期',
        }
      ],

      // 表格列配置
      columns: [
        {
          prop: 'statDate',
          label: '统计日期',
          align: 'center',
          width: 100
        },
        {
          prop: 'lotId',
          label: '停车场',
          align: 'center',
          minWidth: 130,
          showOverflowTooltip: true,
          slotName: 'lotId'
        },
        {
          prop: 'entryCount',
          label: '进场数',
          align: 'center',
          width: 90
        },
        {
          prop: 'exitCount',
          label: '出场数',
          align: 'center',
          width: 90
        },
        {
          prop: 'inParkCount',
          label: '在场数',
          align: 'center',
          width: 90
        },
        {
          prop: 'orderCount',
          label: '订单数',
          align: 'center',
          width: 90
        },
        {
          prop: 'paidOrderCount',
          label: '已支付订单',
          align: 'center',
          width: 90
        },
        {
          prop: 'incomeAmount',
          label: '收入金额',
          align: 'center',
          width: 90
        },
        {
          prop: 'avgOrderAmount',
          label: '平均每单',
          align: 'center',
          width: 90
        },
        {
          prop: 'turnoverRate',
          label: '周转率',
          align: 'center',
          width: 120,
          slotName: 'turnoverRate'
        }
      ]
    }
  },
  created() {
    this.getList()
    this.loadLotOptions()
  },
  methods: {
    /**
     * 构建查询参数：处理日期范围
     * addDateRange 后缀是 StatDate
     */
    buildParams() {
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        lotId: this.queryParams.lotId,
        params: {}
      }
      if (this.queryParams.dateRange && this.queryParams.dateRange.length === 2) {
        params.params.beginStatDate = `${this.queryParams.dateRange[0]} 00:00:00`
        params.params.endStatDate = `${this.queryParams.dateRange[1]} 23:59:59`
      }
      return params
    },

    loadLotOptions() {
      listLot({
        pageNum: 1,
        pageSize: 1000
      }).then(response => {
        this.lotOptions = response.data || []
        this.searchModels[0].options = this.lotOptions
      })
    },

    getLotName(lotId) {
      const match = this.lotOptions.find(item => String(item.lotId) === String(lotId))
      return match ? match.lotName : lotId || '-'
    },

    getList(skipRefresh = false) {
      this.loading = true
      const params = this.buildParams()
      const beforeQuery = skipRefresh ? Promise.resolve() : refreshStat(params)
      beforeQuery.then(() => Promise.all([listStat(params), getStatOverview(params)])).then(([listRes, overviewRes]) => {
        this.statList = listRes.data || []
        this.total = listRes.total || 0
        this.overview = Object.assign({
          totalIncome: '0.00',
          totalEntry: 0,
          totalExit: 0,
          avgOrder: '0.00'
        }, overviewRes.data || {})
      }).finally(() => {
        this.loading = false
      })
    },

    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    resetQuery() {
      this.queryParams = {
        ...this.$options.data().queryParams
      }
      this.getList()
    },

    pageChange({ size, page }) {
      this.queryParams.pageSize = size
      this.queryParams.pageNum = page
      this.getList()
    },

    handleRefresh() {
      this.loading = true
      refreshStat(this.buildParams()).then(() => {
        this.$message.success('刷新成功')
        return this.getList(true)
      }).finally(() => {
        this.loading = false
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.summary-row {
  margin: 0 0 0px 0;
}

.summary-card {
  display: flex;
  align-items: center;
  padding: 20px 24px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px 0 rgba(0, 0, 0, 0.1);
  }

  /* 左侧装饰条 */
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    border-radius: 12px 0 0 12px;
  }

}

.summary-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;

  i {
    font-size: 24px;
    color: #fff;
  }

  .income & {
    background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  }
  .entry & {
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  }
  .exit & {
    background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
  }
  .avg & {
    background: linear-gradient(135deg, #909399 0%, #c0c4cc 100%);
  }
}

.summary-info {
  flex: 1;
  min-width: 0;
}

.summary-label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
  font-weight: 500;
}

.summary-value {
  color: #303133;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 6px;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.summary-trend {
  font-size: 12px;
  color: #c0c4cc;

  .trend-up {
    color: #67c23a;
    font-weight: 500;

    i {
      margin-right: 2px;
    }
  }

  .trend-down {
    color: #f56c6c;
  }
}

/* 响应式：小屏幕时卡片间距调整 */
@media screen and (max-width: 768px) {
  .summary-card {
    margin-bottom: 12px;
  }
}
</style>