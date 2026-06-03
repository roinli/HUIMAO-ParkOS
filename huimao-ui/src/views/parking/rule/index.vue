<template>
  <div class="my-app-container">

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
      :data="ruleList"
      :columns="columns"
      :loading="loading"
      :total="total"
      :pageSize="queryParams.pageSize"
      :currentPage="queryParams.pageNum"
      @pageChange="pageChange"
    >
      <template #toolbar>
        <el-button @click="handleAdd()" type="primary" icon="el-icon-plus">新增</el-button>
      </template>

      <!-- 所属租户 -->
      <template #tenantId="{ scope }">
        <span>{{ getTenantLabel(scope.row.tenantId) }}</span>
      </template>

      <!-- 规则类型 -->
      <template #defaultFlag="{ scope }">
        <el-tag :type="scope.row.defaultFlag === '1' ? 'warning' : 'info'">
          {{ scope.row.defaultFlag === '1' ? '默认规则' : '自定义规则' }}
        </el-tag>
      </template>

      <!-- 免费时长 -->
      <template #freeMinutes="{ scope }">
        <span>{{ scope.row.freeMinutes }} 分钟</span>
      </template>

      <!-- 计费单位 -->
      <template #unitMinutes="{ scope }">
        <span>{{ scope.row.unitMinutes }} 分钟</span>
      </template>

      <!-- 超时补费 -->
      <template #overtimeRecalc="{ scope }">
        <span>{{ getOptionLabel(overtimeRecalcOptions, scope.row.overtimeRecalc) }}</span>
      </template>

      <!-- 状态 -->
      <template #status="{ scope }">
        <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
          {{ getOptionLabel(commonStatusOptions, scope.row.status) }}
        </el-tag>
      </template>

      <!-- 生效时间 -->
      <template #effectiveTime="{ scope }">
        <span>{{ parseTime(scope.row.effectiveTime) }}</span>
      </template>

      <!-- 失效时间 -->
      <template #expireTime="{ scope }">
        <span>{{ parseTime(scope.row.expireTime) }}</span>
      </template>

      <!-- 操作 -->
      <template #actionSlot="{ scope }">
        <el-button type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
        <el-button type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
      </template>
    </table-container>

    <el-dialog :title="title" :visible.sync="open" width="760px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="130px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="所属租户" prop="tenantId">
              <el-select
                v-model="form.tenantId"
                :clearable="hasTenantPermission"
                filterable
                placeholder="请选择租户"
                style="width: 100%"
                :disabled="!hasTenantPermission || Boolean(form.ruleId)"
              >
                <el-option
                  v-for="item in tenantOptions"
                  :key="item.id"
                  :label="item.tenantName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="规则名称" prop="ruleName">
              <el-input v-model="form.ruleName" placeholder="请输入规则名称" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="免费时长" prop="freeMinutes">
              <el-input-number v-model="form.freeMinutes" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="计费单位" prop="unitMinutes">
              <el-input-number v-model="form.unitMinutes" :min="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="离场宽限期" prop="payGraceMinutes">
              <el-input-number v-model="form.payGraceMinutes" :min="0" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="首小时单价" prop="unitPrice">
              <el-input-number v-model="form.unitPrice" :min="0" :precision="2" :step="0.5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="后续单价" prop="followUnitPrice">
              <el-input-number v-model="form.followUnitPrice" :min="0" :precision="2" :step="0.5" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="24小时封顶" prop="dayCapAmount">
              <el-input-number v-model="form.dayCapAmount" :min="0" :precision="2" :step="1" controls-position="right" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="超时是否补费" prop="overtimeRecalc">
              <el-radio-group v-model="form.overtimeRecalc">
                <el-radio
                  v-for="item in overtimeRecalcOptions"
                  :key="item.value"
                  :label="item.value"
                >{{ item.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio
                  v-for="item in commonStatusOptions"
                  :key="item.value"
                  :label="item.value"
                >{{ item.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生效时间" prop="effectiveTime">
              <el-date-picker
                v-model="form.effectiveTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择生效时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="失效时间" prop="expireTime">
              <el-date-picker
                v-model="form.expireTime"
                type="datetime"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择失效时间"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listRule, getRule, addRule, updateRule, delRule } from '@/api/parking/rule'
import { listTenant } from '@/api/system/tenant'
import { checkPermi } from '@/utils/permission'
import { getTenant } from '@/utils/auth'
import { parseTime } from '@/utils/huimao'
import {
  commonStatusOptions,
  overtimeRecalcOptions,
  getOptionLabel
} from '@/views/parking/constants'

export default {
  name: 'ParkingRule',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      showSearch: true,
      total: 0,
      ruleList: [],
      tenantOptions: [],
      hasTenantPermission: false,
      title: '',
      open: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        tenantId: undefined,
        ruleName: undefined,
        status: undefined
      },
      form: {},
      rules: {
        ruleName: [
          { required: true, message: '规则名称不能为空', trigger: 'blur' }
        ],
        freeMinutes: [
          { required: true, message: '请填写免费时长', trigger: 'blur' }
        ],
        unitMinutes: [
          { required: true, message: '请填写计费单位', trigger: 'blur' }
        ],
        unitPrice: [
          { required: true, message: '请填写首小时单价', trigger: 'blur' }
        ],
        followUnitPrice: [
          { required: true, message: '请填写后续单价', trigger: 'blur' }
        ],
        dayCapAmount: [
          { required: true, message: '请填写24小时封顶金额', trigger: 'blur' }
        ],
        payGraceMinutes: [
          { required: true, message: '请填写离场宽限期', trigger: 'blur' }
        ],
        overtimeRecalc: [
          { required: true, message: '请选择超时是否补费', trigger: 'change' }
        ],
        status: [
          { required: true, message: '请选择规则状态', trigger: 'change' }
        ]
      },

       // 搜索表单配置
      searchModels: [
        {
          label: '所属租户',
          modelValue: 'tenantId',
          itemType: 'select',
          placeholder: '请选择租户',
          clearable: true,
          options: [],
          valueKey: 'id',
          labelKey: 'tenantName',
          disabled: true
        },
        {
          label: '规则名称',
          modelValue: 'ruleName',
          itemType: 'input',
          placeholder: '请输入规则名称',
          clearable: true,
        },
        {
          label: '状态',
          modelValue: 'status',
          itemType: 'select',
          placeholder: '请选择状态',
          clearable: true,
          options: [
            { label: '全部', value: undefined },
            ...commonStatusOptions
          ]
        }
      ],

      // 表格列配置
      columns: [
        {
          prop: 'tenantId',
          label: '所属租户',
          minWidth: 160,
          slotName: 'tenantId'
        },
        {
          prop: 'ruleName',
          label: '规则名称',
          minWidth: 180
        },
        {
          prop: 'defaultFlag',
          label: '规则类型',
          align: 'center',
          width: 100,
          slotName: 'defaultFlag'
        },
        {
          prop: 'freeMinutes',
          label: '免费时长',
          align: 'center',
          width: 80,
          slotName: 'freeMinutes'
        },
        {
          prop: 'unitMinutes',
          label: '计费单位',
          align: 'center',
          width: 80,
          slotName: 'unitMinutes'
        },
        {
          prop: 'unitPrice',
          label: '首小时价',
          align: 'center',
          width: 90
        },
        {
          prop: 'followUnitPrice',
          label: '后续单价',
          align: 'center',
          width: 90
        },
        {
          prop: 'dayCapAmount',
          label: '24h封顶',
          align: 'center',
          width: 90
        },
        {
          prop: 'payGraceMinutes',
          label: '离场宽限',
          align: 'center',
          width: 100,
        },
        {
          prop: 'overtimeRecalc',
          label: '超时补费',
          align: 'center',
          width: 100,
          slotName: 'overtimeRecalc'
        },
        {
          prop: 'status',
          label: '状态',
          align: 'center',
          width: 90,
          slotName: 'status'
        },
        {
          prop: 'effectiveTime',
          label: '生效时间',
          align: 'center',
          width: 180,
          slotName: 'effectiveTime'
        },
        {
          prop: 'expireTime',
          label: '失效时间',
          align: 'center',
          width: 180,
          slotName: 'expireTime'
        },
        {
          prop: 'action',
          label: '操作',
          slotName: 'actionSlot',
          fixed: 'right',
          width: 160
        }
      ],

      commonStatusOptions,
      overtimeRecalcOptions,
    }
  },
  created() {
    this.hasTenantPermission = checkPermi(['system:tenant:list'])
    this.initTenantQuery()
    this.getList()
    this.loadTenantOptions()
  },
  methods: {
    parseTime,
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
    getList() {
      this.loading = true
      listRule(this.queryParams).then(response => {
        this.ruleList = response.data
        this.total = response.total
        this.loading = false
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
        this.syncTenantSearchModel();
      })
    },
    syncTenantSearchModel() {
      this.searchModels[0].options = this.tenantOptions
      this.searchModels[0].disabled = !this.hasTenantPermission
      this.searchModels[0].clearable = this.hasTenantPermission
    },
    getTenantLabel(tenantId) {
      const match = this.tenantOptions.find(item => String(item.id) === String(tenantId))
      return match ? match.tenantName : tenantId || '-'
    },
    cancel() {
      this.open = false
      this.reset()
    },
    reset() {
      this.form = {
        ruleId: undefined,
        tenantId: undefined,
        defaultFlag: '0',
        ruleName: undefined,
        freeMinutes: 15,
        unitMinutes: 60,
        unitPrice: 5,
        followUnitPrice: 2,
        dayCapAmount: 30,
        payGraceMinutes: 15,
        overtimeRecalc: '1',
        status: '0',
        effectiveTime: undefined,
        expireTime: undefined,
        remark: undefined
      }
      if (!this.hasTenantPermission) {
        this.form.tenantId = this.getCurrentTenantId()
      }
      this.resetForm('form')
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      if (!this.hasTenantPermission) {
        this.queryParams.tenantId = this.getCurrentTenantId()
      }
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.ruleId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },
    handleAdd() {
      this.reset()
      if (this.hasTenantPermission) {
        this.form.tenantId = this.queryParams.tenantId
      } else {
        this.form.tenantId = this.getCurrentTenantId()
      }
      this.open = true
      this.title = '新增收费规则'
    },
    handleUpdate(row) {
      this.reset()
      const ruleId = row.ruleId || this.ids[0]
      getRule(ruleId).then(response => {
        this.form = Object.assign({}, this.form, response.data)
        this.open = true
        this.title = '修改收费规则'
      })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) {
          return
        }
        if (!this.form.ruleId && !this.form.tenantId) {
          this.$modal.msgError('请选择所属租户')
          return
        }
        const request = this.form.ruleId ? updateRule(this.form) : addRule(this.form)
        request.then(() => {
          this.$modal.msgSuccess(this.form.ruleId ? '修改成功' : '新增成功')
          this.open = false
          this.getList()
        })
      })
    },
    handleDelete(row) {
      const ruleIds = row.ruleId || this.ids
      this.$modal.confirm('是否确认删除所选收费规则？').then(() => {
        return delRule(ruleIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    },
    // 分页
    pageChange({ size, page }) {
      this.queryParams.pageSize = size
      this.queryParams.pageNum = page
      this.getList()
    },


  }
}
</script>
