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
    </search-form>

    <!-- 表格区域 -->
    <table-container
      :data="sessionList"
      :columns="columns"
      :loading="loading"
      :total="total"
      :pageSize="queryParams.pageSize"
      :currentPage="queryParams.pageNum"
      @pageChange="pageChange"
    >
      <!-- 序号 -->
      <template #index="{ scope }">
        <span>{{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}</span>
      </template>

      <!-- 进场时间 -->
      <template #entryTime="{ scope }">
        <span>{{ parseTime(scope.row.entryTime) }}</span>
      </template>

      <!-- 出场时间 -->
      <template #exitTime="{ scope }">
        <span>{{ parseTime(scope.row.exitTime) || '-' }}</span>
      </template>

      <!-- 停车时长 -->
      <template #parkingMinutes="{ scope }">
        <span>{{ scope.row.parkingMinutes || 0 }} 分钟</span>
      </template>

      <!-- 异常 -->
      <template #isAbnormal="{ scope }">
        <el-tag :type="scope.row.isAbnormal === '1' ? 'danger' : 'success'">
          {{ scope.row.isAbnormal === '1' ? '异常' : '正常' }}
        </el-tag>
      </template>

      <!-- 当前状态 -->
      <template #status="{ scope }">
        <el-tag :type="getSessionTagType(scope.row.status)">
          {{ getOptionLabel(sessionStatusOptions, scope.row.status) }}
        </el-tag>
      </template>

      <!-- 操作 -->
      <template #actionSlot="{ scope }">
        <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row)">详情</el-button>
      </template>
    </table-container>

    <!-- 详情弹窗 -->
    <el-dialog :title="detailTitle" :visible.sync="detailOpen" width="1080px" append-to-body>
      <div v-if="detail.session">
        <el-descriptions :column="3" border size="small" class="session-desc">
          <el-descriptions-item label="车牌号">{{ detail.session.plateNo }}</el-descriptions-item>
          <el-descriptions-item label="所属租户">{{ detail.session.tenantName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="停车场">{{ detail.session.lotName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="进场时间">{{ parseTime(detail.session.entryTime) }}</el-descriptions-item>
          <el-descriptions-item label="出场时间">{{ parseTime(detail.session.exitTime) || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getSessionTagType(detail.session.status)">
              {{ getOptionLabel(sessionStatusOptions, detail.session.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="停车时长">{{ detail.session.parkingMinutes || 0 }} 分钟</el-descriptions-item>
          <el-descriptions-item label="应收金额">{{ detail.session.totalPayableAmount || '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="已付金额">{{ detail.session.totalPaidAmount || '0.00' }}</el-descriptions-item>
          <el-descriptions-item label="当前订单号" :span="2">{{ detail.session.currentOrderNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="离场截止">{{ parseTime(detail.session.leaveDeadlineTime) || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-tabs value="orders" class="session-tabs">
          <el-tab-pane label="关联订单" name="orders">
            <el-table :data="detail.orders || []" size="small">
              <el-table-column label="订单号" prop="orderNo" min-width="170" show-overflow-tooltip />
              <el-table-column label="订单类型" width="100">
                <template slot-scope="scope">
                  <span>{{ getOptionLabel(orderTypeOptions, scope.row.orderType) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="停车时长" prop="parkingMinutes" width="100">
                <template slot-scope="scope">
                  <span>{{ scope.row.parkingMinutes || 0 }} 分钟</span>
                </template>
              </el-table-column>
              <el-table-column label="应付金额" prop="payableAmount" width="100" />
              <el-table-column label="已付金额" prop="paidAmount" width="100" />
              <el-table-column label="订单状态" width="100">
                <template slot-scope="scope">
                  <span>{{ getOptionLabel(orderStatusOptions, scope.row.orderStatus) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="支付状态" width="100">
                <template slot-scope="scope">
                  <span>{{ getOptionLabel(payStatusOptions, scope.row.payStatus) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="计费时间" prop="calcTime" width="170">
                <template slot-scope="scope">
                  <span>{{ parseTime(scope.row.calcTime) || '-' }}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="支付流水" name="payments">
            <el-table :data="detail.payments || []" size="small">
              <el-table-column label="支付流水号" prop="paymentNo" min-width="170" show-overflow-tooltip />
              <el-table-column label="订单号" prop="orderNo" min-width="170" show-overflow-tooltip />
              <el-table-column label="支付渠道" prop="payChannel" width="100" />
              <el-table-column label="支付金额" prop="payAmount" width="100" />
              <el-table-column label="支付状态" width="100">
                <template slot-scope="scope">
                  <span>{{ getOptionLabel(payStatusOptions, scope.row.payStatus) }}</span>
                </template>
              </el-table-column>
              <el-table-column label="支付成功时间" prop="paySuccessTime" width="170">
                <template slot-scope="scope">
                  <span>{{ parseTime(scope.row.paySuccessTime) || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="第三方流水号" prop="thirdTradeNo" min-width="180" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="开闸指令" name="commands">
            <el-table :data="detail.commands || []" size="small">
              <el-table-column label="指令号" prop="commandNo" min-width="180" show-overflow-tooltip />
              <el-table-column label="会话ID" prop="sessionId" width="90" />
              <el-table-column label="订单号" prop="orderNo" min-width="170" show-overflow-tooltip />
              <el-table-column label="指令状态" prop="commandStatus" width="110" />
              <el-table-column label="下发时间" prop="createTime" width="170">
                <template slot-scope="scope">
                  <span>{{ parseTime(scope.row.createTime) || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="回调时间" prop="callbackTime" width="170">
                <template slot-scope="scope">
                  <span>{{ parseTime(scope.row.callbackTime) || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="备注" prop="remark" min-width="150" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listSession, getSession } from '@/api/parking/session'
import { listLot } from '@/api/parking/lot'
import { parseTime } from '@/utils/huimao'
import {
  sessionStatusOptions,
  orderTypeOptions,
  orderStatusOptions,
  payStatusOptions,
  getOptionLabel
} from '@/views/parking/constants'

export default {
  name: 'ParkingSession',
  data() {
    return {
      loading: true,
      total: 0,
      lotOptions: [],
      sessionList: [],
      detailOpen: false,
      detailTitle: '停车记录详情',
      detail: {
        session: null,
        orders: [],
        payments: [],
        commands: []
      },

      // 查询条件
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lotId: undefined,
        plateNo: undefined,
        status: undefined,
        beginEntryTime: undefined,
        endEntryTime: undefined
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
          style: 'width: 220px',
          options: [],
          valueKey: 'lotId',
          labelKey: 'lotName'
        },
        {
          label: '车牌号',
          modelValue: 'plateNo',
          itemType: 'input',
          placeholder: '请输入车牌号',
          clearable: true,
          enterSearch: true
        },
        {
          label: '会话状态',
          modelValue: 'status',
          itemType: 'select',
          placeholder: '请选择状态',
          clearable: true,
          options: sessionStatusOptions
        },
        {
          label: '进场时间',
          modelValue: 'dateRange',
          itemType: 'daterange',
          placeholder: '请选择进场时间',
          clearable: true,
          valueFormat: 'yyyy-MM-dd',
          rangeSeparator: '-',
          startPlaceholder: '开始日期',
          endPlaceholder: '结束日期',
          style: 'width: 260px'
        }
      ],

      // 表格列配置
      columns: [
        {
          prop: 'plateNo',
          label: '车牌号',
          align: 'center',
          width: 120
        },
        {
          prop: 'tenantName',
          label: '所属租户',
          align: 'center',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'lotName',
          label: '所属停车场',
          align: 'center',
          minWidth: 170,
          showOverflowTooltip: true
        },
        {
          prop: 'entryTime',
          label: '进场时间',
          align: 'center',
          width: 170,
          slotName: 'entryTime'
        },
        {
          prop: 'exitTime',
          label: '出场时间',
          align: 'center',
          width: 170,
          slotName: 'exitTime'
        },
        {
          prop: 'parkingMinutes',
          label: '停车时长',
          align: 'center',
          width: 100,
          slotName: 'parkingMinutes'
        },
        {
          prop: 'totalPayableAmount',
          label: '应收金额',
          align: 'center',
          width: 100
        },
        {
          prop: 'totalPaidAmount',
          label: '已付金额',
          align: 'center',
          width: 100
        },
        {
          prop: 'isAbnormal',
          label: '异常',
          align: 'center',
          width: 80,
          slotName: 'isAbnormal'
        },
        {
          prop: 'status',
          label: '当前状态',
          align: 'center',
          width: 130,
          slotName: 'status'
        },
        {
          prop: 'action',
          label: '操作',
          align: 'center',
          slotName: 'actionSlot',
          fixed: 'right',
          width: 120
        }
      ],

      sessionStatusOptions,
      orderTypeOptions,
      orderStatusOptions,
      payStatusOptions
    }
  },
  created() {
    this.getList()
    this.loadLotOptions()
    // 初始化停车场下拉选项
    this.searchModels[0].options = this.lotOptions
  },
  methods: {
    parseTime,
    getOptionLabel,

    getList() {
      this.loading = true
      const params = Object.assign({}, this.queryParams)

      params.params = {}
      if (this.queryParams.dateRange && this.queryParams.dateRange.length === 2) {
        params.params.beginEntryTime = `${this.queryParams.dateRange[0]} 00:00:00`
        params.params.endEntryTime = `${this.queryParams.dateRange[1]} 23:59:59`
      }

      // 删除辅助字段，只保留接口需要的
      delete params.dateRange
      listSession(params).then(response => {
        this.sessionList = response.data || []
        this.total = response.total || 0
        this.loading = false
      })
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

    getSessionTagType(status) {
      if (status === 'COMPLETED') {
        return 'success'
      }
      if (status === 'WAIT_PAY') {
        return 'warning'
      }
      if (status === 'PAID_PENDING_EXIT') {
        return ''
      }
      return 'info'
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

    handleDetail(row) {
      getSession(row.sessionId).then(response => {
        this.detail = response.data
        this.detailOpen = true
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.session-desc {
  margin-bottom: 16px;
}

.session-tabs {
  margin-top: 16px;
}
</style>
