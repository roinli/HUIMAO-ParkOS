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
      :data="recordList"
      :columns="columns"
      :loading="loading"
      :total="total"
      :pageSize="queryParams.pageSize"
      :currentPage="queryParams.pageNum"
      @pageChange="pageChange"
    >
      <!-- 状态 -->
      <template #status="{ scope }">
        <el-tag :type="getWithdrawTagType(scope.row.status)" size="small">
          {{ getOptionLabel(withdrawStatusOptions, scope.row.status) }}
        </el-tag>
      </template>

      <!-- 操作 -->
      <template #actionSlot="{ scope }">
        <el-button
          v-if="scope.row.status === 'WAIT_AUDIT'"
          v-hasPermi="['parking:withdraw:review']"
          type="text"
          icon="el-icon-finished"
          class="review-btn"
          @click="handleReview(scope.row)"
        >审核</el-button>
        <span v-else class="action-placeholder">-</span>
      </template>
    </table-container>

    <!-- 提现审核弹窗 -->
    <el-dialog title="提现审核" :visible.sync="reviewOpen" width="520px" append-to-body custom-class="review-dialog">
      <div class="review-info">
        <div class="review-info-item">
          <span class="info-label">提现单号：</span>
          <span class="info-value">{{ currentRecord.withdrawNo || '-' }}</span>
        </div>
        <div class="review-info-item">
          <span class="info-label">申请金额：</span>
          <span class="info-value highlight">￥{{ currentRecord.applyAmount || '0.00' }}</span>
        </div>
        <div class="review-info-item">
          <span class="info-label">实到账金额：</span>
          <span class="info-value highlight">￥{{ currentRecord.realAmount || '0.00' }}</span>
        </div>
      </div>
      <el-form ref="reviewForm" :model="reviewForm" :rules="reviewRules" label-width="100px" class="review-form">
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="reviewForm.status">
            <el-radio label="PAID">
              <el-tag type="success" size="small" effect="plain">已打款</el-tag>
            </el-radio>
            <el-radio label="REJECTED">
              <el-tag type="danger" size="small" effect="plain">驳回</el-tag>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注" prop="auditRemark">
          <el-input
            v-model="reviewForm.auditRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入审核备注"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" icon="el-icon-check" @click="submitReview">确 定</el-button>
        <el-button icon="el-icon-close" @click="reviewOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listWithdraw, reviewWithdraw } from '@/api/parking/withdraw'
import { listTenant } from '@/api/system/tenant'
import { checkPermi } from '@/utils/permission'
import { getTenant } from '@/utils/auth'
import { withdrawStatusOptions, getOptionLabel } from '@/views/parking/constants'

export default {
  name: 'ParkingWithdraw',
  data() {
    return {
      loading: false,
      total: 0,
      tenantOptions: [],
      hasTenantPermission: false,
      recordList: [],
      reviewOpen: false,
      currentRecord: {},
      reviewForm: {
        withdrawId: undefined,
        status: 'PAID',
        auditRemark: ''
      },

      // 查询条件
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        withdrawNo: '',
        status: '',
        tenantId: undefined,
        dateRange: []  // 辅助字段
      },

      // 搜索表单配置
      searchModels: [
        {
          label: '所属租户',
          modelValue: 'tenantId',
          itemType: 'select',
          placeholder: '请选择租户',
          clearable: true,
          filterable: true,
          style: 'width: 220px',
          options: [],
          valueKey: 'id',
          labelKey: 'tenantName',
        },
        {
          label: '提现单号',
          modelValue: 'withdrawNo',
          itemType: 'input',
          placeholder: '请输入提现单号',
          clearable: true,
          enterSearch: true
        },
        {
          label: '状态',
          modelValue: 'status',
          itemType: 'select',
          placeholder: '请选择状态',
          clearable: true,
          options: withdrawStatusOptions
        },
        {
          label: '申请时间',
          modelValue: 'dateRange',
          itemType: 'daterange',
          placeholder: '请选择申请时间',
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
          prop: 'tenantName',
          label: '租户',
          minWidth: 150,
          showOverflowTooltip: true
        },
        {
          prop: 'withdrawNo',
          label: '提现单号',
          minWidth: 200,
          showOverflowTooltip: true
        },
        {
          prop: 'applyAmount',
          label: '申请金额',
          align: 'center',
          width: 100
        },
        {
          prop: 'feeAmount',
          label: '手续费',
          align: 'center',
          width: 100
        },
        {
          prop: 'realAmount',
          label: '实到账金额',
          align: 'center',
          width: 110
        },
        {
          prop: 'feeRate',
          label: '费率',
          align: 'center',
          width: 90
        },
        {
          prop: 'status',
          label: '状态',
          align: 'center',
          width: 100,
          slotName: 'status'
        },
        {
          prop: 'auditBy',
          label: '审核人',
          align: 'center',
          width: 100
        },
        {
          prop: 'auditTime',
          label: '审核时间',
          align: 'center',
          width: 170
        },
        {
          prop: 'payTime',
          label: '打款时间',
          align: 'center',
          width: 170
        },
        {
          prop: 'createTime',
          label: '申请时间',
          align: 'center',
          width: 170
        },
        {
          prop: 'auditRemark',
          label: '审核备注',
          minWidth: 180,
          showOverflowTooltip: true
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

      reviewRules: {
        status: [
          { required: true, message: '请选择审核结果', trigger: 'change' }
        ],
        auditRemark: [
          { required: true, message: '请输入审核备注', trigger: 'blur' }
        ]
      },

      withdrawStatusOptions
    }
  },
  created() {
    this.hasTenantPermission = checkPermi(['system:tenant:list'])
    this.initTenantQuery()
    this.loadTenantOptions()
    this.getList()
  },
  methods: {
    getOptionLabel,

    getCurrentTenantId() {
      return getTenant()
    },

    getCurrentTenantOption() {
      const tenantId = this.getCurrentTenantId()
      return tenantId ? [{ id: tenantId, tenantName: `当前租户(${tenantId})` }] : []
    },

    initTenantQuery() {
      if (!this.hasTenantPermission) {
        this.queryParams.tenantId = this.getCurrentTenantId()
        this.tenantOptions = this.getCurrentTenantOption()
      }
      this.syncTenantSearchModel()
    },

    syncTenantSearchModel() {
      this.searchModels[0].options = this.tenantOptions
      this.searchModels[0].disabled = !this.hasTenantPermission
      this.searchModels[0].clearable = this.hasTenantPermission
    },

    loadTenantOptions() {
      if (!this.hasTenantPermission) {
        this.tenantOptions = this.getCurrentTenantOption()
        if (!this.queryParams.tenantId) {
          this.queryParams.tenantId = this.getCurrentTenantId()
        }
        this.syncTenantSearchModel()
        return
      }
      listTenant({
        pageNum: 1,
        pageSize: 1000
      }).then(response => {
        this.tenantOptions = response.data || []
        this.syncTenantSearchModel()
      })
    },

    /**
     * 构建查询参数：处理日期范围
     */
    buildParams() {
      const params = {
        pageNum: this.queryParams.pageNum,
        pageSize: this.queryParams.pageSize,
        withdrawNo: this.queryParams.withdrawNo,
        status: this.queryParams.status,
        tenantId: this.queryParams.tenantId,
        params: {}
      }
      if (this.queryParams.dateRange && this.queryParams.dateRange.length === 2) {
        params.params.beginCreateTime = `${this.queryParams.dateRange[0]} 00:00:00`
        params.params.endCreateTime = `${this.queryParams.dateRange[1]} 23:59:59`
      }
      return params
    },

    getList() {
      this.loading = true
      listWithdraw(this.buildParams()).then(response => {
        this.recordList = response.data || []
        this.total = response.total || 0
      }).finally(() => {
        this.loading = false
      })
    },

    getWithdrawTagType(status) {
      if (status === 'PAID') {
        return 'success'
      }
      if (status === 'REJECTED') {
        return 'danger'
      }
      return 'warning'
    },

    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    resetQuery() {
      this.queryParams = {
        ...this.$options.data().queryParams
      }
      if (!this.hasTenantPermission) {
        this.queryParams.tenantId = this.getCurrentTenantId()
      }
      this.syncTenantSearchModel()
      this.getList()
    },

    pageChange({ size, page }) {
      this.queryParams.pageSize = size
      this.queryParams.pageNum = page
      this.getList()
    },

    handleReview(row) {
      this.currentRecord = Object.assign({}, row)
      this.reviewForm = {
        withdrawId: row.withdrawId,
        status: 'PAID',
        auditRemark: ''
      }
      this.reviewOpen = true
      this.$nextTick(() => {
        if (this.$refs.reviewForm) {
          this.$refs.reviewForm.clearValidate()
        }
      })
    },

    submitReview() {
      this.$refs.reviewForm.validate(valid => {
        if (!valid) {
          return
        }
        reviewWithdraw(this.reviewForm).then(() => {
          this.$modal.msgSuccess('审核成功')
          this.reviewOpen = false
          this.getList()
        })
      })
    }
  }
}
</script>

<style lang="scss" scoped>
/* ========== 审核按钮 ========== */

.review-btn {
  color: #409eff;
  font-weight: 500;

  &:hover {
    color: #66b1ff;
  }
}

.action-placeholder {
  color: #c0c4cc;
}

/* ========== 审核弹窗美化 ========== */

.review-dialog {
  ::v-deep .el-dialog__header {
    padding: 20px 24px;
    border-bottom: 1px solid #f0f0f0;

    .el-dialog__title {
      font-weight: 600;
      font-size: 16px;
    }
  }

  ::v-deep .el-dialog__body {
    padding: 24px;
  }
}

.review-info {
  background: #f8f9fb;
  border-radius: 10px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.review-info-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;

  &:last-child {
    margin-bottom: 0;
  }
}

.info-label {
  color: #909399;
  font-size: 13px;
  width: 90px;
  flex-shrink: 0;
}

.info-value {
  color: #303133;
  font-size: 14px;
  font-weight: 500;

  &.highlight {
    color: #409eff;
    font-size: 16px;
    font-weight: 700;
  }
}

.review-form {
  ::v-deep .el-radio-group {
    .el-radio {
      margin-right: 20px;

      &:last-child {
        margin-right: 0;
      }
    }
  }
}
</style>