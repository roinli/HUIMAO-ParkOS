<template>
  <div class="my-app-container">
    <!-- 搜索区域 -->
    <search-form
      :labelWidth="90"
      v-model="queryParams"
      :forms="searchForms"
      :loading="loading"
      @search="handleQuery"
      @reset="resetQuery"
      :key="searchFormKey"
    />

    <!-- 表格区域 -->
    <table-container
      :data="gateList"
      title="通道管理"
      :columns="columns"
      :loading="loading"
      :total="total"
      :pageSize="queryParams.pageSize"
      :currentPage="queryParams.pageNum"
      @pageChange="pageChange"
    >
      <template #toolbar>
        <el-button
          type="primary"
          icon="el-icon-plus"
          v-hasPermi="['parking:gate:add']"
          @click="handleAdd"
        >新增</el-button>
      </template>

      <template #lotId="{ scope }">
        <span>{{ getLotName(scope.row.lotId) }}</span>
      </template>

      <template #gateType="{ scope }">
        <el-tag :type="scope.row.gateType === '1' ? 'success' : 'warning'">
          {{ getOptionLabel(gateTypeOptions, scope.row.gateType) }}
        </el-tag>
      </template>

      <template #status="{ scope }">
        <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
          {{ getOptionLabel(commonStatusOptions, scope.row.status) }}
        </el-tag>
      </template>

      <template #actionSlot="{ scope }">
        <el-button
          type="text"
          icon="el-icon-edit"
          v-hasPermi="['parking:gate:edit']"
          @click="handleUpdate(scope.row)"
        >修改</el-button>
        <el-button
          type="text"
          icon="el-icon-delete"
          v-hasPermi="['parking:gate:remove']"
          @click="handleDelete(scope.row)"
        >删除</el-button>
      </template>
    </table-container>

    <!-- 新增/修改 弹窗 -->
    <el-dialog
      :title="title"
      :visible.sync="open"
      width="55%"
      :close-on-click-modal="false"
    >
      <submit-form
        ref="form"
        v-model="form"
        :forms="submitForms"
        :rules="rules"
        :labelWidth="100"
      />

      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listGate, getGate, addGate, updateGate, delGate } from '@/api/parking/gate'
import { listLot } from '@/api/parking/lot'
import { gateTypeOptions, commonStatusOptions, getOptionLabel } from '@/views/parking/constants'

export default {
  name: 'ParkingGate',
  data() {
    return {
      loading: true,
      total: 0,
      gateList: [],
      lotOptions: [],
      ids: [],
      title: '',
      open: false,
      gateTypeOptions,
      commonStatusOptions,

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lotId: undefined,
        gateName: undefined,
        gateType: undefined
      },

      form: {
        gateId: undefined,
        lotId: undefined,
        gateCode: undefined,
        gateName: undefined,
        gateType: '1',
        sortNum: 1,
        status: '0',
        remark: undefined
      },

      // 搜索表单
      searchForms: [
        {
          label: '所属停车场',
          modelValue: 'lotId',
          itemType: 'select',
          placeholder: '请选择停车场',
          clearable: true,
          colSpan: 8
        },
        {
          label: '通道名称',
          modelValue: 'gateName',
          itemType: 'input',
          placeholder: '请输入通道名称',
          clearable: true,
          colSpan: 8
        },
        {
          label: '通道类型',
          modelValue: 'gateType',
          itemType: 'select',
          placeholder: '请选择通道类型',
          clearable: true,
          colSpan: 8
        }
      ],

      columns: [
        { prop: 'lotId', label: '所属停车场', minWidth: 180, slotName: 'lotId' },
        { prop: 'gateName', label: '通道名称', minWidth: 160 },
        { prop: 'gateCode', label: '通道编码', minWidth: 140 },
        { prop: 'gateType', label: '通道类型', width: 110, slotName: 'gateType' },
        { prop: 'sortNum', label: '排序号', width: 90 },
        { prop: 'status', label: '状态', width: 100, slotName: 'status' },
        { prop: 'createTime', label: '创建时间', width: 180 },
        { prop: 'action', label: '操作', fixed: 'right', width: 160, slotName: 'actionSlot' }
      ],

      // 提交表单
      submitForms: [
        {
          label: '所属停车场',
          modelValue: 'lotId',
          itemType: 'select',
          placeholder: '请选择停车场',
          colSpan: 12
        },
        {
          label: '通道名称',
          modelValue: 'gateName',
          itemType: 'input',
          placeholder: '请输入通道名称',
          maxlength: 100,
          colSpan: 12
        },
        {
          label: '通道编码',
          modelValue: 'gateCode',
          itemType: 'input',
          placeholder: '请输入通道编码',
          maxlength: 64,
          colSpan: 12
        },
        {
          label: '通道类型',
          modelValue: 'gateType',
          itemType: 'select',
          colSpan: 12
        },
        {
          label: '排序',
          modelValue: 'sortNum',
          itemType: 'input',
          min: 0,
          colSpan: 12
        },
        {
          label: '状态',
          modelValue: 'status',
          itemType: 'select',
          colSpan: 12
        },
        {
          label: '备注',
          modelValue: 'remark',
          itemType: 'textarea',
          rows: 3,
          placeholder: '请输入备注',
          colSpan: 24
        }
      ],

      rules: {
        lotId: [{ required: true, message: '请选择所属停车场', trigger: 'change' }],
        gateName: [{ required: true, message: '通道名称不能为空', trigger: 'blur' }],
        gateCode: [{ required: true, message: '通道编码不能为空', trigger: 'blur' }],
        gateType: [{ required: true, message: '请选择通道类型', trigger: 'change' }],
        sortNum: [{ required: true, message: '请填写排序号', trigger: 'blur' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      },
      searchFormKey: 0

    }
  },

  created() {
    this.getList()
    this.loadLotOptions()
  },

  methods: {
    getOptionLabel,

    getList() {
      this.loading = true
      listGate(this.queryParams).then(res => {
        this.gateList = res.data
        this.total = res.total
        this.loading = false
      })
    },

    //加载停车场，并动态赋值给表单
    loadLotOptions() {
      listLot({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.lotOptions = res.data || []

        //给form表单赋值
        const lotItem = this.submitForms.find(i => i.modelValue === 'lotId')
        lotItem.options = this.lotOptions
        lotItem.valueKey = 'lotId'
        lotItem.labelKey = 'lotName'

        // 给通道类型赋值
        const typeItem = this.submitForms.find(i => i.modelValue === 'gateType')
        typeItem.options = this.gateTypeOptions

        // 给状态赋值
        const statusItem = this.submitForms.find(i => i.modelValue === 'status')
        statusItem.options = this.commonStatusOptions


        //给搜索框赋值
        const searchLotItem = this.searchForms.find(i => i.modelValue === 'lotId')
        searchLotItem.options = this.lotOptions
        searchLotItem.valueKey = 'lotId'
        searchLotItem.labelKey = 'lotName'

        // 给通道类型赋值
        const searchTypeItem = this.searchForms.find(i => i.modelValue === 'gateType')
        searchTypeItem.options = this.gateTypeOptions

        this.searchFormKey++

      })
    },

    getLotName(lotId) {
      const find = this.lotOptions.find(i => String(i.lotId) === String(lotId))
      return find ? find.lotName : lotId || '-'
    },

    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    resetQuery() {
      this.queryParams = { ...this.$options.data().queryParams }
      this.getList()
    },

    pageChange({ size, page }) {
      this.queryParams.pageSize = size
      this.queryParams.pageNum = page
      this.getList()
    },

    handleSelectionChange(val) {
      this.ids = val.map(i => i.gateId)
    },

    handleAdd() {
      this.form = { ...this.$options.data().form }
      this.open = true
      this.title = '新增通道'
    },

    async handleUpdate(row) {
      const data = await getGate(row.gateId)
      this.form = { ...data.data }
      this.open = true
      this.title = '修改通道'
    },

    submitForm() {
      this.$refs.form.validate().then(() => {
        const api = this.form.gateId ? updateGate : addGate
        api(this.form).then(() => {
          this.$modal.msgSuccess('操作成功')
          this.open = false
          this.getList()
        })
      })
    },

    handleDelete(row) {
      const id = row.gateId || this.ids
      this.$modal.confirm('确认删除所选通道？').then(() => {
        return delGate(id)
      }).then(() => {
        this.$modal.msgSuccess('删除成功')
        this.getList()
      })
    },

    cancel() {
      this.open = false
    }
  }
}
</script>