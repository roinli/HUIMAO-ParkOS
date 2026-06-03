<template>
  <div class="app-container">
    <el-form
      v-show="showSearch"
      ref="queryForm"
      :model="queryParams"
      size="small"
      :inline="true"
      label-width="90px"
    >
      <el-form-item label="停车场" prop="lotId">
        <el-select v-model="queryParams.lotId" clearable filterable placeholder="请选择停车场">
          <el-option
            v-for="item in lotOptions"
            :key="item.lotId"
            :label="item.lotName"
            :value="item.lotId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备编码" prop="deviceCode">
        <el-input
          v-model="queryParams.deviceCode"
          placeholder="请输入设备编码"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input
          v-model="queryParams.deviceName"
          placeholder="请输入设备名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="模拟器状态" prop="simulatorStatus">
        <el-select v-model="queryParams.simulatorStatus" clearable placeholder="请选择状态">
          <el-option
            v-for="item in simulatorStatusOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="自动心跳" prop="autoHeartbeat">
        <el-select v-model="queryParams.autoHeartbeat" clearable placeholder="请选择">
          <el-option
            v-for="item in yesNoOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item style="margin-left: 20px;">
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="deviceList">
      <el-table-column label="序号" width="70" align="center">
        <template slot-scope="scope">
          <span>{{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="停车场" prop="lotName" min-width="170" show-overflow-tooltip />
      <el-table-column label="通道" min-width="180" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ scope.row.gateName || '-' }}</span>
          <el-tag size="mini" class="ml8" :type="scope.row.gateType === '1' ? 'success' : 'warning'">
            {{ getOptionLabel(gateTypeOptions, scope.row.gateType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="设备编码" prop="deviceCode" min-width="150" />
      <el-table-column label="设备名称" prop="deviceName" min-width="150" show-overflow-tooltip />
      <el-table-column label="平台设备状态" width="120" align="center">
        <template slot-scope="scope">
          <el-tag :type="getDeviceStatusTagType(scope.row.deviceStatus)">
            {{ getOptionLabel(deviceStatusOptions, scope.row.deviceStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="模拟器状态" width="110" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.simulatorStatus === 'RUNNING' ? 'success' : 'info'">
            {{ getOptionLabel(simulatorStatusOptions, scope.row.simulatorStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="在线状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag :type="scope.row.onlineStatus === 'ONLINE' ? 'success' : 'info'">
            {{ getOptionLabel(onlineStatusOptions, scope.row.onlineStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="自动心跳" width="90" align="center">
        <template slot-scope="scope">
          <span>{{ getOptionLabel(yesNoOptions, scope.row.autoHeartbeat) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="心跳间隔" width="100" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.heartbeatIntervalSec || 0 }} 秒</span>
        </template>
      </el-table-column>
      <el-table-column label="自动回调" width="90" align="center">
        <template slot-scope="scope">
          <span>{{ getOptionLabel(yesNoOptions, scope.row.autoCallback) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="默认车牌" prop="defaultPlateNo" width="110" align="center" />
      <el-table-column label="最近心跳" width="170" align="center">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastHeartbeatTime) || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最近响应" min-width="220" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ getResponseText(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最近指令/订单" min-width="220" show-overflow-tooltip>
        <template slot-scope="scope">
          <span>{{ scope.row.lastCommandNo || '-' }}</span>
          <span v-if="scope.row.lastOrderNo"> / {{ scope.row.lastOrderNo }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" fixed="right" width="360" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            v-if="scope.row.simulatorStatus !== 'RUNNING'"
            v-hasPermi="['parking:simulator:operate']"
            size="mini"
            type="text"
            icon="el-icon-video-play"
            @click="handleStart(scope.row)"
          >启动</el-button>
          <el-button
            v-if="scope.row.simulatorStatus === 'RUNNING'"
            v-hasPermi="['parking:simulator:operate']"
            size="mini"
            type="text"
            icon="el-icon-video-pause"
            @click="handleStop(scope.row)"
          >停止</el-button>
          <el-button
            v-if="scope.row.onlineStatus !== 'ONLINE'"
            v-hasPermi="['parking:simulator:operate']"
            size="mini"
            type="text"
            icon="el-icon-connection"
            @click="handleOnline(scope.row)"
          >上线</el-button>
          <el-button
            v-if="scope.row.onlineStatus === 'ONLINE'"
            v-hasPermi="['parking:simulator:operate']"
            size="mini"
            type="text"
            icon="el-icon-close"
            @click="handleOffline(scope.row)"
          >下线</el-button>
          <el-button
            v-hasPermi="['parking:simulator:operate']"
            size="mini"
            type="text"
            icon="el-icon-refresh"
            @click="handleHeartbeat(scope.row)"
          >心跳</el-button>
          <el-button
            v-hasPermi="['parking:simulator:edit']"
            size="mini"
            type="text"
            icon="el-icon-setting"
            @click="handleConfig(scope.row)"
          >配置</el-button>
          <el-button
            v-hasPermi="['parking:simulator:operate']"
            size="mini"
            type="text"
            icon="el-icon-right"
            @click="handleEvent(scope.row, 'entry')"
          >入场</el-button>
          <el-button
            v-hasPermi="['parking:simulator:operate']"
            size="mini"
            type="text"
            icon="el-icon-back"
            @click="handleEvent(scope.row, 'exit')"
          >出场</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog title="模拟器配置" :visible.sync="configOpen" width="560px" append-to-body>
      <el-form ref="configForm" :model="configForm" :rules="configRules" label-width="120px">
        <el-form-item label="设备编码">
          <span>{{ currentDevice.deviceCode || '-' }}</span>
        </el-form-item>
        <el-form-item label="自动心跳" prop="autoHeartbeat">
          <el-radio-group v-model="configForm.autoHeartbeat">
            <el-radio v-for="item in yesNoOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="心跳间隔(秒)" prop="heartbeatIntervalSec">
          <el-input-number v-model="configForm.heartbeatIntervalSec" :min="1" :step="5" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="自动回调" prop="autoCallback">
          <el-radio-group v-model="configForm.autoCallback">
            <el-radio v-for="item in yesNoOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="回调延迟(秒)" prop="callbackDelaySec">
          <el-input-number v-model="configForm.callbackDelaySec" :min="0" :step="1" controls-position="right" style="width: 100%" />
        </el-form-item>
        <el-form-item label="默认车牌号" prop="defaultPlateNo">
          <el-input v-model="configForm.defaultPlateNo" placeholder="请输入默认模拟车牌号" maxlength="20" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="configForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitConfig">确 定</el-button>
        <el-button @click="configOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :title="eventTitle" :visible.sync="eventOpen" width="560px" append-to-body>
      <el-form ref="eventForm" :model="eventForm" :rules="eventRules" label-width="120px">
        <el-form-item label="设备编码">
          <span>{{ currentDevice.deviceCode || '-' }}</span>
        </el-form-item>
        <el-form-item label="模拟车牌号" prop="plateNo">
          <el-input v-model="eventForm.plateNo" placeholder="为空时使用默认车牌号" maxlength="20" />
        </el-form-item>
        <el-form-item label="事件时间" prop="eventTime">
          <el-date-picker
            v-model="eventForm.eventTime"
            type="datetime"
            value-format="yyyy-MM-dd HH:mm:ss"
            placeholder="为空则使用当前时间"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="抓拍图片地址" prop="imageUrl">
          <el-input v-model="eventForm.imageUrl" placeholder="可选" />
        </el-form-item>
        <el-form-item label="原始报文" prop="rawContent">
          <el-input v-model="eventForm.rawContent" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitEvent">确 定</el-button>
        <el-button @click="eventOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listLot } from '@/api/parking/lot'
import {
  listSimulatorDevice,
  getSimulatorDevice,
  startSimulator,
  stopSimulator,
  onlineSimulator,
  offlineSimulator,
  heartbeatSimulator,
  updateSimulatorConfig,
  simulateEntry,
  simulateExit
} from '@/api/parking/simulator'
import { parseTime } from '@/utils/huimao'
import {
  gateTypeOptions,
  deviceStatusOptions,
  yesNoOptions,
  simulatorStatusOptions,
  onlineStatusOptions,
  getOptionLabel
} from '@/views/parking/constants'

export default {
  name: 'ParkingSimulator',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      lotOptions: [],
      deviceList: [],
      configOpen: false,
      eventOpen: false,
      eventAction: 'entry',
      currentDevice: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        lotId: undefined,
        deviceCode: undefined,
        deviceName: undefined,
        simulatorStatus: undefined,
        autoHeartbeat: undefined
      },
      configForm: {
        deviceId: undefined,
        autoHeartbeat: '1',
        heartbeatIntervalSec: 30,
        autoCallback: '1',
        callbackDelaySec: 1,
        defaultPlateNo: '',
        remark: ''
      },
      eventForm: {
        plateNo: '',
        eventTime: '',
        imageUrl: '',
        rawContent: ''
      },
      configRules: {
        autoHeartbeat: [
          { required: true, message: '请选择是否自动心跳', trigger: 'change' }
        ],
        heartbeatIntervalSec: [
          { required: true, message: '请填写心跳间隔', trigger: 'blur' }
        ],
        autoCallback: [
          { required: true, message: '请选择是否自动回调', trigger: 'change' }
        ],
        callbackDelaySec: [
          { required: true, message: '请填写回调延迟', trigger: 'blur' }
        ]
      },
      eventRules: {},
      gateTypeOptions,
      deviceStatusOptions,
      yesNoOptions,
      simulatorStatusOptions,
      onlineStatusOptions
    }
  },
  computed: {
    eventTitle() {
      return this.eventAction === 'entry' ? '模拟设备入场' : '模拟设备出场'
    }
  },
  created() {
    this.getList()
    this.loadLotOptions()
  },
  methods: {
    parseTime,
    getOptionLabel,
    loadLotOptions() {
      listLot({
        pageNum: 1,
        pageSize: 1000
      }).then(response => {
        this.lotOptions = response.data || []
      })
    },
    getList() {
      this.loading = true
      listSimulatorDevice(this.queryParams).then(response => {
        this.deviceList = response.data || []
        this.total = response.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    getDeviceStatusTagType(status) {
      if (status === '0') {
        return 'success'
      }
      if (status === '1') {
        return 'warning'
      }
      return 'info'
    },
    getResponseText(row) {
      if (!row.lastResponseCode && !row.lastResponseMsg) {
        return '-'
      }
      return `${row.lastResponseCode || '-'} / ${row.lastResponseMsg || '-'}`
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    refreshRow(row) {
      getSimulatorDevice(row.deviceId).then(response => {
        const data = response.data || {}
        const index = this.deviceList.findIndex(item => item.deviceId === row.deviceId)
        if (index > -1) {
          this.$set(this.deviceList, index, data)
        }
      })
    },
    doOperate(request, row, successText) {
      request(row.deviceId).then(response => {
        this.$modal.msgSuccess(response.msg || successText)
        this.refreshRow(row)
      })
    },
    handleStart(row) {
      this.doOperate(startSimulator, row, '启动成功')
    },
    handleStop(row) {
      this.doOperate(stopSimulator, row, '停止成功')
    },
    handleOnline(row) {
      this.doOperate(onlineSimulator, row, '上线成功')
    },
    handleOffline(row) {
      this.doOperate(offlineSimulator, row, '下线成功')
    },
    handleHeartbeat(row) {
      this.doOperate(heartbeatSimulator, row, '心跳发送成功')
    },
    handleConfig(row) {
      getSimulatorDevice(row.deviceId).then(response => {
        const data = response.data || {}
        this.currentDevice = data
        this.configForm = {
          deviceId: data.deviceId,
          autoHeartbeat: data.autoHeartbeat || '1',
          heartbeatIntervalSec: data.heartbeatIntervalSec || 30,
          autoCallback: data.autoCallback || '1',
          callbackDelaySec: data.callbackDelaySec || 1,
          defaultPlateNo: data.defaultPlateNo || '',
          remark: data.remark || ''
        }
        this.configOpen = true
        this.$nextTick(() => {
          if (this.$refs.configForm) {
            this.$refs.configForm.clearValidate()
          }
        })
      })
    },
    submitConfig() {
      this.$refs.configForm.validate(valid => {
        if (!valid) {
          return
        }
        updateSimulatorConfig(this.configForm).then(response => {
          this.$modal.msgSuccess(response.msg || '配置成功')
          this.configOpen = false
          this.refreshRow(this.currentDevice)
        })
      })
    },
    handleEvent(row, action) {
      getSimulatorDevice(row.deviceId).then(response => {
        const data = response.data || {}
        this.currentDevice = data
        this.eventAction = action
        this.eventForm = {
          plateNo: data.defaultPlateNo || '',
          eventTime: '',
          imageUrl: '',
          rawContent: ''
        }
        this.eventOpen = true
        this.$nextTick(() => {
          if (this.$refs.eventForm) {
            this.$refs.eventForm.clearValidate()
          }
        })
      })
    },
    submitEvent() {
      const request = this.eventAction === 'entry' ? simulateEntry : simulateExit
      request(this.currentDevice.deviceId, this.eventForm).then(response => {
        this.$modal.msgSuccess(response.msg || (this.eventAction === 'entry' ? '模拟入场成功' : '模拟出场成功'))
        this.eventOpen = false
        this.refreshRow(this.currentDevice)
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.mb8 {
  margin-bottom: 8px;
}

.ml8 {
  margin-left: 8px;
}
</style>
