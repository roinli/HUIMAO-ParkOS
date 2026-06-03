<template>
  <div class="my-app-container">

    <!-- 搜索 -->
    <search-form
      v-model="queryParams"
      :forms="searchForms"
      @search="handleQuery"
      @reset="resetQuery"
      :labelWidth="90"
    />

    <table-container
      :data="deviceList"
      :loading="loading"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      :columns="columns"
      @pagination="getList"
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

      <template #gateId="{ scope }">
        <span>{{ getGateName(scope.row.gateId) }}</span>
      </template>

      <template #deviceType="{ scope }">
         <span>{{ getOptionLabel(deviceTypeOptions, scope.row.deviceType) }}</span>
      </template>

      <template #vendorCode="{ scope }">
         <span>{{ getOptionLabel(deviceVendorOptions, scope.row.vendorCode) }}</span>
      </template>

      <template #connectMode="{ scope }">
         <span>{{ getOptionLabel(deviceConnectModeOptions, scope.row.connectMode) }}</span>
      </template>

      <template #status="{ scope }">
         <span>{{ getOptionLabel(deviceStatusOptions, scope.row.status) }}</span>
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

    <!-- 弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="720px" append-to-body>
      <submit-form
        ref="form"
        v-model="form"
        :rules="rules"
        :forms="submitForms"
        :labelWidth="100"
      />
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listDevice, getDevice, addDevice, updateDevice, delDevice } from '@/api/parking/device'
import { listLot } from '@/api/parking/lot'
import { listGate } from '@/api/parking/gate'
import { parseTime } from '@/utils/huimao'
import {
  deviceTypeOptions,
  deviceStatusOptions,
  deviceVendorOptions,
  deviceConnectModeOptions,
  getOptionLabel
} from '@/views/parking/constants'

export default {
  name: 'ParkingDevice',
  data() {
    return {
      loading: true,
      ids: [],
      single: true,
      multiple: true,
      total: 0,
      deviceList: [],
      lotOptions: [],
      searchGateOptions: [],
      formGateOptions: [],
      title: '',
      open: false,

      columns: [
        { label: '所属停车场', prop: 'lotId', minWidth: 170, custom: true, slotName: 'lotId' },
        { label: '所属通道', prop: 'gateId', minWidth: 150, custom: true, slotName: 'gateId' },
        { label: '设备编码', prop: 'deviceCode', minWidth: 150 },
        { label: '设备名称', prop: 'deviceName', minWidth: 160 },
        { label: '设备类型', prop: 'deviceType', width: 110, custom: true, slotName: 'deviceType'  },
        { label: '厂商编码', prop: 'vendorCode', width: 100, slotName: 'vendorCode' },
        { label: '接入方式', prop: 'connectMode', width: 120, slotName: 'connectMode' },
        { label: '设备状态', prop: 'status', width: 110, custom: true, slotName: 'status' },
        { label: '最后心跳', prop: 'lastHeartbeatTime', width: 180 },
        { prop: 'action', label: '操作', fixed: 'right', width: 160, slotName: 'actionSlot' }
      ],

      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lotId: undefined,
        gateId: undefined,
        deviceCode: undefined,
        status: undefined
      },

      form: {},

      searchForms: [
        { label: '所属停车场', modelValue: 'lotId', itemType: 'select', placeholder: '请选择停车场', clearable: true },
        { label: '所属通道', modelValue: 'gateId', itemType: 'select', placeholder: '请选择通道', clearable: true },
        { label: '设备名称', modelValue: 'deviceName', itemType: 'input', placeholder: '请输入设备名称', clearable: true },
        { label: '设备编码', modelValue: 'deviceCode', itemType: 'input', placeholder: '请输入设备编码', clearable: true },
        { label: '设备状态', modelValue: 'status', itemType: 'select', placeholder: '请选择设备状态', clearable: true, options: deviceStatusOptions }
      ],

      submitForms: [
        { label: '所属停车场', modelValue: 'lotId', itemType: 'select', col: 12, required: true, colSpan: 12 },
        { label: '所属通道', modelValue: 'gateId', itemType: 'select', col: 12, required: true, colSpan: 12 },
        { label: '设备编码', modelValue: 'deviceCode', itemType: 'input', col: 12, required: true, colSpan: 12 },
        { label: '设备名称', modelValue: 'deviceName', itemType: 'input', col: 12, required: true, colSpan: 12 },
        { label: '设备类型', modelValue: 'deviceType', itemType: 'select', col: 12, required: true, options: deviceTypeOptions, colSpan: 12 },
        { label: '设备状态', modelValue: 'status', itemType: 'select', col: 12, required: true, options: deviceStatusOptions, colSpan: 12 },
        { label: '厂商编码', modelValue: 'vendorCode', itemType: 'select', col: 12, required: true, options: deviceVendorOptions, colSpan: 12 },
        { label: '接入方式', modelValue: 'connectMode', itemType: 'select', col: 12, required: true, options: deviceConnectModeOptions, colSpan: 12 },
        { label: '设备密钥', modelValue: 'deviceSecret', itemType: 'input', col: 24, showPassword: true },
        { label: '设备配置', modelValue: 'configJson', itemType: 'textarea', col: 24, rows: 5 },
        { label: '备注', modelValue: 'remark', itemType: 'textarea', col: 24, rows: 3 }
      ],

      rules: {
        lotId: [{ required: true, message: '请选择所属停车场', trigger: 'change' }],
        gateId: [{ required: true, message: '请选择所属通道', trigger: 'change' }],
        deviceCode: [{ required: true, message: '设备编码不能为空', trigger: 'blur' }],
        deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
        deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
        vendorCode: [{ required: true, message: '请选择或输入厂商编码', trigger: 'change' }],
        connectMode: [{ required: true, message: '请选择接入方式', trigger: 'change' }],
        status: [{ required: true, message: '请选择设备状态', trigger: 'change' }]
      },

      deviceTypeOptions,
      deviceStatusOptions,
      deviceVendorOptions,
      deviceConnectModeOptions,
      allGate:[],
    }
  },

  watch: {
    'queryParams.lotId'(val) {
      this.queryParams.gateId = undefined
      this.loadSearchGateOptions(val)
    },

    'form.lotId'(val) {
      this.form.gateId = undefined
      this.loadFormGateOptions(val)
    }
  },

  created() {
    this.getList()
    this.loadLotOptions()
    this.loadSearchGateOptions() // 搜索框初始通道
    this.loadFormGateOptions()   // 表单初始通道
  },

  methods: {
    parseTime,
    getOptionLabel,

    getList() {
      this.loading = true
      listDevice(this.queryParams).then(res => {
        this.deviceList = res.data
        this.total = res.total
        this.loading = false
      })
    },

    loadLotOptions() {
      listLot({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.lotOptions = res.data || []

        // 表单停车场
        this.submitForms = this.submitForms.map(item => {
          if (item.modelValue === 'lotId') {
            return { ...item, options: this.lotOptions, valueKey: 'lotId', labelKey: 'lotName' }
          }
          return item
        })

        // 搜索框停车场
        this.searchForms = this.searchForms.map(item => {
          if (item.modelValue === 'lotId') {
            return { ...item, options: this.lotOptions, valueKey: 'lotId', labelKey: 'lotName' }
          }
          return item
        })


      })
    },

    loadSearchGateOptions(lotId = '') {
      const params = { pageNum: 1, pageSize: 1000 }
      if (lotId) params.lotId = lotId

      listGate(params).then(res => {
        this.searchGateOptions = res.data || []

        // 只刷新搜索框通道
        this.searchForms = this.searchForms.map(item => {
          if (item.modelValue === 'gateId') {
            return {
              ...item,
              options: this.searchGateOptions,
              valueKey: 'gateId',
              labelKey: 'gateName'
            }
          }
          return item
        })
      })
    },

    loadFormGateOptions(lotId = '') {
      const params = { pageNum: 1, pageSize: 1000 }
      if (lotId) params.lotId = lotId

      listGate(params).then(res => {
        this.formGateOptions = res.data || []

        if(!lotId){
          this.allGate = res.data || []
        }

        this.submitForms = this.submitForms.map(item => {
            if (item.modelValue === 'gateId') {
              return {
                ...item,
                options: this.formGateOptions,  // 空数组也会生效
                valueKey: 'gateId',
                labelKey: 'gateName'
              }
            }
            return item
        })

      })
    },

    getLotName(lotId) {
      const item = this.lotOptions.find(i => String(i.lotId) === String(lotId))
      return item ? item.lotName : lotId || '-'
    },
    getGateName(gateId) {
      const item = this.allGate.find(i => String(i.gateId) === String(gateId))
      return item ? item.gateName : gateId || '-'
    },

    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },

    resetQuery() {
      this.queryParams = { ...this.$options.data().queryParams }
      this.loadSearchGateOptions() // 只重置搜索框通道
      this.getList()
    },

    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.deviceId)
      this.single = selection.length !== 1
      this.multiple = !selection.length
    },

    handleAdd() {
      this.reset()
      this.loadFormGateOptions() // 打开新增时，刷新表单通道
      this.open = true
      this.title = '新增设备'
    },

    handleUpdate(row) {
      this.reset()
      const id = row?.deviceId || this.ids[0]
      getDevice(id).then(res => {
        this.form = { ...this.form, ...res.data }
        //this.loadFormGateOptions(this.form.lotId) // 修改时，只加载表单通道
        this.open = true
        this.title = '修改设备'


        const params = { pageNum: 1, pageSize: 1000 }
        params.lotId = res.data.lotId

        listGate(params).then(ress => {
          this.formGateOptions = ress.data || []
          this.submitForms = this.submitForms.map(item => {
              if (item.modelValue === 'gateId') {
                return {
                  ...item,
                  options: this.formGateOptions,  // 空数组也会生效
                  valueKey: 'gateId',
                  labelKey: 'gateName'
                }
              }
              return item
          })

          this.form = { ...this.form, ...res.data }
        })

      })
    },

    submitForm() {
      this.$refs.form.validate().then(() => {
        const request = this.form.deviceId ? updateDevice(this.form) : addDevice(this.form)
        request.then(() => {
          this.$modal.msgSuccess('操作成功')
          this.open = false
          this.getList()
        })
      })
    },

    handleDelete(row) {
      const id = row?.deviceId || this.ids
      this.$modal.confirm('确认删除？').then(() => {
        return delDevice(id)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      })
    },

    cancel() {
      this.open = false
    },

    reset() {
      this.form = {
        deviceId: undefined,
        lotId: undefined,
        gateId: undefined,
        deviceCode: undefined,
        deviceName: undefined,
        deviceType: 'camera',
        vendorCode: 'SIM',
        connectMode: 'SIMULATED',
        deviceSecret: undefined,
        status: '0',
        configJson: undefined,
        remark: undefined
      }
    }
  }
}
</script>
