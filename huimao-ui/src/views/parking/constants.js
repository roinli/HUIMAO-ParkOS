// 车位类型选项
export const lotTypeOptions = [
  { label: '地面', value: '1' },
  { label: '地下', value: '2' },
  { label: '占道', value: '3' },
  { label: '地上/地下', value: '4' }
]

export const lotStatusOptions = [
  { label: '运营中', value: '0' },
  { label: '停用', value: '1' }
]

export const gateTypeOptions = [
  { label: '入口', value: '1' },
  { label: '出口', value: '2' }
]

export const commonStatusOptions = [
  { label: '启用', value: '0' },
  { label: '停用', value: '1' }
]

export const deviceTypeOptions = [
  { label: '相机', value: 'camera' },
  { label: '道闸', value: 'barrier' },
  { label: '一体机', value: 'allinone' }
]

export const deviceStatusOptions = [
  { label: '在线', value: '0' },
  { label: '离线', value: '1' },
  { label: '停用', value: '2' }
]

export const deviceVendorOptions = [
  { label: '模拟设备', value: 'SIM' }
]

export const deviceConnectModeOptions = [
  { label: '模拟接入', value: 'SIMULATED' },
  { label: 'HTTP 接入', value: 'HTTP' },
  { label: 'SDK 接入', value: 'SDK' }
]

export const overtimeRecalcOptions = [
  { label: '否', value: '0' },
  { label: '是', value: '1' }
]

export const sessionStatusOptions = [
  { label: '在场中', value: 'IN_PARK' },
  { label: '待支付', value: 'WAIT_PAY' },
  { label: '已支付待离场', value: 'PAID_PENDING_EXIT' },
  { label: '已完成', value: 'COMPLETED' }
]

export const orderTypeOptions = [
  { label: '正常订单', value: 'NORMAL' },
  { label: '补费订单', value: 'OVERTIME' }
]

export const orderStatusOptions = [
  { label: '待支付', value: 'UNPAID' },
  { label: '已支付', value: 'PAID' },
  { label: '已关闭', value: 'CLOSED' }
]

export const payStatusOptions = [
  { label: '未支付', value: 'UNPAID' },
  { label: '支付成功', value: 'SUCCESS' },
  { label: '支付失败', value: 'FAIL' }
]

export const withdrawStatusOptions = [
  { label: '待审核', value: 'WAIT_AUDIT' },
  { label: '已打款', value: 'PAID' },
  { label: '已驳回', value: 'REJECTED' }
]

export const accountStatusOptions = [
  { label: '正常', value: '0' },
  { label: '冻结', value: '1' }
]

export const yesNoOptions = [
  { label: '是', value: '1' },
  { label: '否', value: '0' }
]

export const simulatorStatusOptions = [
  { label: '运行中', value: 'RUNNING' },
  { label: '已停止', value: 'STOPPED' }
]

export const onlineStatusOptions = [
  { label: '在线', value: 'ONLINE' },
  { label: '离线', value: 'OFFLINE' }
]

export function getOptionLabel(options, value, fallback = '-') {
  const target = value === undefined || value === null ? '' : String(value)
  const item = options.find(option => String(option.value) === target)
  return item ? item.label : fallback
}
