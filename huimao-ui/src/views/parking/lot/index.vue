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
      :data="lotList"
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

      <!-- 状态 -->
      <template #status="{ scope }">
        <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
          {{ getOptionLabel(lotStatusOptions, scope.row.status) }}
        </el-tag>
      </template>

      <!-- 所属租户 -->
      <template #tenantId="{ scope }">
        <span>{{ getTenantLabel(scope.row.tenantId) }}</span>
      </template>

      <!-- 区域 -->
      <template #area="{ scope }">
        <span>{{ getAreaText(scope.row) }}</span>
      </template>

      <!-- 车位数 -->
      <template #space="{ scope }">
        <span>{{ getSpaceText(scope.row) }}</span>
      </template>

      <!-- 联系人 -->
      <template #contact="{ scope }">
        <span>
          {{ scope.row.contactName || '-' }}
          <span v-if="scope.row.contactPhone"> / {{ scope.row.contactPhone }}</span>
        </span>
      </template>

      <!-- 收费规则 -->
      <template #chargeRuleId="{ scope }">
        <span>{{ getRuleLabel(scope.row.chargeRuleId) }}</span>
      </template>

      <!-- 二维码 -->
      <template #payQr="{ scope }">
        <el-link
          v-if="scope.row.payQrUrl"
          :href="scope.row.payQrUrl"
          type="primary"
          target="_blank"
        >查看二维码</el-link>
        <span v-else>{{ scope.row.payQrContent || '-' }}</span>
      </template>

      <!-- 操作 -->
      <template #actionSlot="{ scope }">
        <el-button type="text" icon="el-icon-setting" @click="ruleEditHandler(scope.row)">收费规则</el-button>
        <el-button type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
        <el-button type="text" icon="el-icon-delete" @click="handleDelete(scope.row.lotId)">删除</el-button>
      </template>
    </table-container>

    <!-- 新增&编辑弹窗 -->
    <ParkingLotDialog ref="ParkingLotDialog" @success="getList" />

    <!-- 编辑规则 -->
    <PileRuleDialog ref="ruleDialog" @success="getList" />

  </div>
</template>

<script>
import { listLot, getLot, addLot, updateLot, delLot } from '@/api/parking/lot'
import { listRule } from '@/api/parking/rule'
import { listTenant } from '@/api/system/tenant'
import { checkPermi } from '@/utils/permission'
import { getTenant } from '@/utils/auth'
import { lotStatusOptions, getOptionLabel } from '@/views/parking/constants'
import ParkingLotDialog from '.././components/ParkingLotDialog.vue'
import PileRuleDialog from '.././components/PileRuleDialog';

export default {
  name: 'ParkingLot',
  components: {
    ParkingLotDialog,
    PileRuleDialog
  },
  data() {
    return {
      loading: true,
      total: 0,
      lotList: [],

      // 租户/规则下拉
      tenantOptions: [],
      ruleOptions: [],
      hasTenantPermission: false,
      lotStatusOptions,

      // 查询条件
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        tenantId: undefined,
        lotCode: undefined,
        lotName: undefined,
        status: undefined
      },

      // 搜索表单配置
      searchModels: [
        {
          label: '所属租户',
          modelValue: 'tenantId',
          itemType: 'select',
          placeholder: '请选择租户',
          clearable: true,
          options: this.tenantOptions,
          valueKey: 'id',
          labelKey: 'tenantName',
          disabled: true
        },
        {
          label: '停车场名称',
          modelValue: 'lotName',
          itemType: 'input',
          placeholder: '停车场名称',
          clearable: true,
        },
        {
          label: '停车场编码',
          modelValue: 'lotCode',
          itemType: 'input',
          placeholder: '停车场编码',
          clearable: true
        },
        {
          label: '运营状态',
          modelValue: 'status',
          itemType: 'select',
          placeholder: '状态',
          clearable: true,
          options: [
            { label: '全部', value: undefined },
            ...lotStatusOptions
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
          prop: 'lotName',
          label: '停车场名称',
          minWidth: 160
        },
        {
          prop: 'lotCode',
          label: '停车场编码',
          minWidth: 200
        },
        {
          prop: 'area',
          label: '所属区域',
          minWidth: 180,
          slotName: 'area'
        },
        {
          prop: 'address',
          label: '详细地址',
          minWidth: 220
        },
        {
          prop: 'space',
          label: '车位数',
          minWidth: 160,
          slotName: 'space'
        },
        // {
        //   prop: 'contact',
        //   label: '联系人',
        //   minWidth: 160,
        //   slotName: 'contact'
        // },
        // {
        //   prop: 'chargeRuleId',
        //   label: '默认收费规则',
        //   minWidth: 160,
        //   slotName: 'chargeRuleId'
        // },
        // {
        //   prop: 'payQr',
        //   label: '二维码',
        //   minWidth: 140,
        //   slotName: 'payQr'
        // },
        {
          prop: 'status',
          label: '运营状态',
          minWidth: 100,
          slotName: 'status'
        },
        {
          prop: 'createTime',
          label: '创建时间',
          minWidth: 180
        },
        {
          prop: 'action',
          label: '操作',
          slotName: 'actionSlot',
          fixed: 'right',
          width: 200
        }
      ]
    }
  },
  created() {
    this.hasTenantPermission = checkPermi(['system:tenant:list'])
    this.initTenantQuery()
    this.getList()
    this.loadRuleOptions()
    this.loadTenantOptions()
  },
  activated() {
    // 页面激活时刷新租户/规则
    this.loadTenantOptions()
    this.loadRuleOptions()
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

    syncTenantSearchModel() {
      this.searchModels[0].options = this.tenantOptions
      this.searchModels[0].disabled = !this.hasTenantPermission
      this.searchModels[0].clearable = this.hasTenantPermission
    },

    initTenantQuery() {
      if (!this.hasTenantPermission) {
        this.queryParams.tenantId = this.getCurrentTenantId()
        this.tenantOptions = this.getCurrentTenantOption()
      }
      this.syncTenantSearchModel()
    },

    // 获取列表
    getList() {
      this.loading = true
      listLot(this.queryParams).then(response => {
        this.lotList = response.data
        this.total = response.total
        this.loading = false
      })
    },

    // 加载租户
    loadTenantOptions() {
      if (!this.hasTenantPermission) {
        this.tenantOptions = this.getCurrentTenantOption()
        if (!this.queryParams.tenantId) {
          this.queryParams.tenantId = this.getCurrentTenantId()
        }
        this.syncTenantSearchModel()
        return
      }
      listTenant({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.tenantOptions = res.data || []
        this.syncTenantSearchModel()
      })
    },

    // 加载收费规则
    loadRuleOptions(tenantId) {
      const params = { pageNum: 1, pageSize: 1000 }
      if (tenantId) params.tenantId = tenantId
      listRule(params).then(res => {
        this.ruleOptions = res.data || []
      })
    },

    // 租户名称
    getTenantLabel(tenantId) {
      const find = this.tenantOptions.find(t => t.id == tenantId)
      return find ? find.tenantName : tenantId || '-'
    },

    // 规则名称
    getRuleLabel(ruleId) {
      const find = this.ruleOptions.find(r => r.ruleId == ruleId)
      return find ? find.ruleName : ruleId || '-'
    },

    // 区域拼接
    getAreaText(row) {
      const arr = [row.provinceName, row.cityName, row.areaName].filter(Boolean)
      return arr.length ? arr.join(' / ') : '-'
    },

    // 车位信息
    getSpaceText(row) {
      return `总 ${row.totalSpace || 0} / 临 ${row.tempSpace || 0} / 月 ${row.monthlySpace || 0}`
    },

    // 搜索
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    // 重置
    resetQuery() {
      this.queryParams = {
        ...this.$options.data().queryParams
      }
      if (!this.hasTenantPermission) {
        this.queryParams.tenantId = this.getCurrentTenantId()
      }
      this.getList()
    },

    // 分页
    pageChange({ size, page }) {
      this.queryParams.pageSize = size
      this.queryParams.pageNum = page
      this.getList()
    },

    // 新增
    handleAdd() {
      this.$refs.ParkingLotDialog.openDialog()
    },

    // 修改
    handleUpdate(row) {
      this.$refs.ParkingLotDialog.openDialog(row)
    },

    // 删除
    handleDelete(id) {
      this.$confirm('确定删除该停车场吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delLot(id).then(() => {
          this.$message.success('删除成功')
          this.getList()
        })
      })
    },
    ruleEditHandler(row) {
      this.$refs.ruleDialog.open(row);
    },

  }
}
</script>
