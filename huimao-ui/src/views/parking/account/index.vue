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

    <!-- 统计概览卡片 -->
    <el-row :gutter="10" class="summary-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="summary-card income">
          <div class="summary-info">
            <div class="summary-label">累计收入</div>
            <div class="summary-value">￥{{ account.totalIncomeAmount }}</div>
            <div class="summary-desc">历史总收入</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="summary-card balance">
          <div class="summary-info">
            <div class="summary-label">账户余额</div>
            <div class="summary-value">￥{{ account.balanceAmount }}</div>
            <div class="summary-desc">当前账户总额</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="summary-card available">
          <div class="summary-info">
            <div class="summary-label">可提现余额</div>
            <div class="summary-value">￥{{ account.availableAmount }}</div>
            <div class="summary-desc">可立即提现</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <div class="summary-card frozen">
          <div class="summary-info">
            <div class="summary-label">冻结金额</div>
            <div class="summary-value">￥{{ account.frozenAmount }}</div>
            <div class="summary-desc">冻结中不可提现</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 下方两列布局 -->
    <el-row :gutter="10">
      <!-- 账户信息 -->
      <el-col :xs="24" :lg="10">
        <el-card v-loading="loading" shadow="hover" class="account-card">
          <div slot="header" class="account-header">
            <span class="header-title">
              <i class="el-icon-user-solid" />
              账户信息
            </span>
            <el-button
              v-if="!hasTenantPermission"
              v-hasPermi="['parking:withdraw:apply']"
              type="primary"
              size="small"
              icon="el-icon-bank-card"
              class="withdraw-btn"
              @click="handleApply"
            >
              发起提现
            </el-button>
          </div>
          <el-descriptions :column="1" border size="small" class="account-desc">
            <el-descriptions-item label="账户状态">
              <el-tag :type="account.status === '0' ? 'success' : 'danger'" size="small">
                {{ getOptionLabel(accountStatusOptions, account.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="手续费率">
              <span class="highlight-text">{{ Number(account.feeRate || 0) * 100 }}%</span>
            </el-descriptions-item>
            <el-descriptions-item label="最小提现金额">
              <span class="highlight-text">￥{{ account.minWithdrawAmount }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="结算户名">
              {{ account.settleMerchantName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="开户行">
              {{ account.settleBankName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="结算账号">
              <span class="account-no">{{ account.settleAccountNo || '-' }}</span>
            </el-descriptions-item>
            <el-descriptions-item label="更新时间">
              <i class="el-icon-time time-icon" />
              {{ account.updateTime || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 近期待提现记录 -->
      <el-col :xs="24" :lg="14">
        <el-card v-loading="loading" shadow="hover" class="withdraw-card">
          <div slot="header" class="withdraw-header">
            <span class="header-title">
              <i class="el-icon-document" />
              近期待提现记录
            </span>
            <el-tag type="info" size="mini" effect="plain">最近5条</el-tag>
          </div>
          <table-container
            :data="withdrawRecords"
            :columns="withdrawColumns"
            :loading="false"
            :total="withdrawRecords.length"
            :pageSize="withdrawRecords.length"
            :currentPage="1"
            :showPagination="false"
            :showToolbar="false"
            :hasPagination="false"
          >
            <template #status="{ scope }">
              <el-tag :type="getWithdrawTagType(scope.row.status)" size="small">
                {{ getOptionLabel(withdrawStatusOptions, scope.row.status) }}
              </el-tag>
            </template>
          </table-container>
        </el-card>
      </el-col>
    </el-row>

    <!-- 账户流水 -->
    <el-card v-loading="loading" shadow="hover" class="flow-card">
      <div slot="header" class="flow-header">
        <span class="header-title">
          <i class="el-icon-s-order" />
          账户流水
        </span>
      </div>
      <table-container
        :data="flows"
        :columns="flowColumns"
        :loading="false"
        :total="flowTotal"
        :pageSize="flowQueryParams.pageSize"
        :currentPage="flowQueryParams.pageNum"
        :showPagination="true"
        :showToolbar="false"
        @pageChange="handleFlowPageChange"
      >
        <template #changeType="{ scope }">
          <el-tag
            :type="scope.row.changeType === 'IN' ? 'success' : scope.row.changeType === 'OUT' ? 'danger' : 'info'"
            size="small"
            effect="plain"
          >
            {{ scope.row.changeType === 'IN' ? '收入' : scope.row.changeType === 'OUT' ? '支出' : scope.row.changeType }}
          </el-tag>
        </template>
        <template #changeAmount="{ scope }">
          <span :class="scope.row.changeType === 'IN' ? 'amount-in' : 'amount-out'">
            {{ scope.row.changeType === 'IN' ? '+' : '-' }}￥{{ scope.row.changeAmount }}
          </span>
        </template>
      </table-container>
    </el-card>


    <!-- 发起提现弹窗 -->
    <el-dialog title="发起提现" :visible.sync="open" width="520px" append-to-body custom-class="withdraw-dialog">
      <div class="withdraw-tips">
        <i class="el-icon-info" />
        可提现余额：<span class="tips-amount">￥{{ account.availableAmount }}</span>
      </div>
      <el-form ref="form" :model="form" :rules="rules" label-width="110px" class="withdraw-form">
        <el-form-item label="提现金额" prop="applyAmount">
          <el-input-number
            v-model="form.applyAmount"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
            style="width: 100%"
            placeholder="请输入提现金额"
          />
        </el-form-item>
        <el-form-item label="申请备注" prop="applyRemark">
          <el-input
            v-model="form.applyRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入申请备注（选填）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" icon="el-icon-check" @click="submitForm">确 定</el-button>
        <el-button icon="el-icon-close" @click="open = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { 
getAccountSummary, 
applyWithdraw,
accountInfo,
withdrawList,
flowList
} from '@/api/parking/account'
import { listTenant } from '@/api/system/tenant'
import { checkPermi } from '@/utils/permission'
import { getTenant } from '@/utils/auth'
import { accountStatusOptions, withdrawStatusOptions, getOptionLabel } from '@/views/parking/constants'

export default {
  name: 'ParkingAccount',
  data() {
    const validateAmount = (rule, value, callback) => {
      if (value === undefined || value === null || value <= 0) {
        callback(new Error('请输入提现金额'))
        return
      }
      if (value < Number(this.account.minWithdrawAmount)) {
        callback(new Error(`提现金额不能低于 ${this.account.minWithdrawAmount} 元`))
        return
      }
      if (value > Number(this.account.availableAmount)) {
        callback(new Error('提现金额不能大于可提现余额'))
        return
      }
      callback()
    }
    return {
      loading: false,
      tenantOptions: [],
      hasTenantPermission: false,
      queryParams: {
        tenantId: undefined
      },
      account: {
        totalIncomeAmount: '0.00',
        balanceAmount: '0.00',
        availableAmount: '0.00',
        frozenAmount: '0.00',
        status: '0',
        feeRate: '0.0000',
        minWithdrawAmount: '0.00'
      },
      flows: [],
      withdrawRecords: [],
      open: false,
      form: {
        applyAmount: undefined,
        applyRemark: ''
      },
      rules: {
        applyAmount: [
          { validator: validateAmount, trigger: 'blur' }
        ]
      },
      accountStatusOptions,
      withdrawStatusOptions,

      // 搜索表单配置
      searchModels: [
        {
          label: '所属租户',
          modelValue: 'tenantId',
          itemType: 'select',
          placeholder: '请选择租户',
          clearable: true,
          filterable: true,
          style: 'width: 260px',
          options: [],
          valueKey: 'id',
          labelKey: 'tenantName',
          disabled: true
        }
      ],

      // 近期待提现记录列配置
      withdrawColumns: [
        {
          prop: 'withdrawNo',
          label: '提现单号',
          minWidth: 160,
          showOverflowTooltip: true
        },
        {
          prop: 'applyAmount',
          label: '申请金额',
          align: 'center',
          width: 100
        },
        {
          prop: 'realAmount',
          label: '实到账',
          align: 'center',
          width: 100
        },
        {
          prop: 'status',
          label: '状态',
          align: 'center',
          width: 100,
          slotName: 'status'
        },
        {
          prop: 'createTime',
          label: '申请时间',
          align: 'center',
          width: 170
        }
      ],

      // 账户流水列配置
      flowColumns: [
        {
          prop: 'bizType',
          label: '业务类型',
          width: 150
        },
        {
          prop: 'bizNo',
          label: '业务单号',
          minWidth: 170,
          showOverflowTooltip: true
        },
        {
          prop: 'changeType',
          label: '变动方向',
          align: 'center',
          width: 100,
          slotName: 'changeType'
        },
        {
          prop: 'changeAmount',
          label: '变动金额',
          align: 'center',
          width: 100,
          slotName: 'changeAmount'
        },
        {
          prop: 'beforeBalance',
          label: '变动前余额',
          align: 'center',
          width: 110
        },
        {
          prop: 'afterBalance',
          label: '变动后余额',
          align: 'center',
          width: 110
        },
        {
          prop: 'remark',
          label: '备注',
          minWidth: 160,
          showOverflowTooltip: true
        },
        {
          prop: 'createTime',
          label: '时间',
          align: 'center',
          width: 170
        }
      ],

      // 流水分页参数
      flowQueryParams: {
        pageNum: 1,
        pageSize: 10
      },
      flowTotal: 0  // 总条数

    }
  },
  created() {
    this.hasTenantPermission = checkPermi(['system:tenant:list'])
    this.initTenantQuery()
    this.loadTenantOptions()
    
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

      if(!this.queryParams.tenantId){
        this.queryParams.tenantId = this.tenantOptions.length > 0 ?  this.tenantOptions[0].id : 0;
      }

      // 等待 DOM 更新完成再请求数据，保证租户参数正确传递
      this.$nextTick(() => {
        this.getSummary()
        this.accountInfo()
        this.withdrawList()
        this.flowList()
      })

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
    getSummary() {
      if (this.hasTenantPermission && !this.queryParams.tenantId) {
        this.account = Object.assign({}, this.account, {
          totalIncomeAmount: '0.00',
          balanceAmount: '0.00',
          availableAmount: '0.00',
          frozenAmount: '0.00'
        })
        this.withdrawRecords = []
        this.flows = []
        return
      }
      this.loading = true
      getAccountSummary(this.queryParams).then(response => {
        const data = response.data || {}
        //this.account = Object.assign({}, this.account, data)
        //this.withdrawRecords = data.withdrawRecords || []
        //this.flows = data.flows || []
      }).finally(() => {
        this.loading = false
      })
    },
    accountInfo() {
      accountInfo(this.queryParams).then(response => {
        this.account = response.data || {}
      }).finally(() => {
      })
    },
    withdrawList() {
      let request = JSON.parse(JSON.stringify(this.queryParams));
      request.pageNum = 1;
      request.pageSize = 5;
      request.status = 'WAIT_AUDIT';

      withdrawList(request).then(response => {
        this.withdrawRecords = response.data || []
      }).finally(() => {
      })
    },
    flowList() {
      let request = {
        ...this.queryParams,
        ...this.flowQueryParams
      };

      flowList(request).then(response => {
        this.flows = response.data || [];
        this.flowTotal = response.total || 0; // 接口返回的总条数
      });
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
    handleApply() {
      this.form = {
        applyAmount: undefined,
        applyRemark: ''
      }
      this.resetForm('form')
      this.open = true
    },
    handleQuery() {
      this.flowQueryParams.pageNum = 1; // 搜索重置页码

      this.$nextTick(() => {
        this.getSummary()
        this.accountInfo()
        this.withdrawList()
        this.flowList()
      })
    },
    resetQuery() {
      this.queryParams = {
        ...this.$options.data().queryParams
      }
      if (!this.hasTenantPermission) {
        this.queryParams.tenantId = this.getCurrentTenantId()
      }
      this.syncTenantSearchModel()
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        applyWithdraw(this.form).then(() => {
          this.$modal.msgSuccess('提现申请已提交')
          this.open = false
          this.getSummary()
          this.accountInfo()
          this.withdrawList()
          this.flowList()
        })
      })
    },
    // 流水分页改变
    handleFlowPageChange({ size, page }) {
      this.flowQueryParams.pageNum = page
      this.flowQueryParams.pageSize = size
      this.flowList();
    },

  }
}
</script>

<style lang="scss" scoped>
/* ========== 统计卡片美化 ========== */

.summary-row {
  margin: 0 0 0px 0;
}

.summary-card {
  display: flex;
  align-items: center;
  padding: 22px 24px;
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
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 18px;
  flex-shrink: 0;

  i {
    font-size: 26px;
    color: #fff;
  }

  .income & {
    background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  }
  .balance & {
    background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  }
  .available & {
    background: linear-gradient(135deg, #e6a23c 0%, #f0c78a 100%);
  }
  .frozen & {
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
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
  margin-bottom: 6px;
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

.summary-desc {
  font-size: 12px;
  color: #c0c4cc;
}

.account-card,
.withdraw-card
{
   height: 413px;
   padding: 0px !important;
}

.account-card,
.withdraw-card,
.flow-card {
  border-radius: 12px;

  ::v-deep .el-card__header {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f0f0;
  }

  ::v-deep .el-card__body {
    padding: 20px;
  }
}

/* 卡片头部 */
.account-header,
.withdraw-header,
.flow-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;

  i {
    margin-right: 8px;
    font-size: 16px;
    color: #409eff;
  }
}

/* 发起提现按钮 */
.withdraw-btn {
  padding: 8px 20px;
  font-size: 13px;
  border-radius: 6px;

  i {
    margin-right: 4px;
  }
}

/* 账户信息描述列表 */
.account-desc {
  ::v-deep .el-descriptions__body {
    .el-descriptions-item__label {
      width: 100px;
      background: #f8f9fb;
      color: #606266;
      font-weight: 500;
    }
    .el-descriptions-item__content {
      color: #303133;
    }
  }
}

.highlight-text {
  color: #409eff;
  font-weight: 600;
}

.account-no {
  font-family: 'Courier New', monospace;
  letter-spacing: 1px;
  color: #606266;
}

.time-icon {
  color: #909399;
  margin-right: 4px;
}

/* ========== 流水金额颜色 ========== */

.amount-in {
  color: #67c23a;
  font-weight: 600;
}

.amount-out {
  color: #f56c6c;
  font-weight: 600;
}

/* ========== 提现弹窗美化 ========== */

.withdraw-dialog {
  ::v-deep .el-dialog__header {
    padding: 20px;
    border-bottom: 1px solid #f0f0f0;

    .el-dialog__title {
      font-weight: 600;
      font-size: 16px;
    }
  }

  ::v-deep .el-dialog__body {
    padding: 20px;
  }
}

.withdraw-tips {
  background: #f0f9ff;
  border: 1px solid #d9ecff;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 20px;
  color: #606266;
  font-size: 13px;

  i {
    color: #409eff;
    margin-right: 8px;
    font-size: 14px;
  }

  .tips-amount {
    color: #409eff;
    font-size: 18px;
    font-weight: 700;
    margin-left: 4px;
  }
}

.withdraw-form {
  ::v-deep .el-input-number {
    width: 100%;
  }
}

@media screen and (max-width: 768px) {
  .summary-card {
    margin-bottom: 12px;
  }
}

::v-deep .el-card__body{
  padding: 0px !important;
}
</style>