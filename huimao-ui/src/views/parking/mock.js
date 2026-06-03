function pad(value) {
  return value < 10 ? `0${value}` : `${value}`
}

function formatDate(date) {
  const year = date.getFullYear()
  const month = pad(date.getMonth() + 1)
  const day = pad(date.getDate())
  return `${year}-${month}-${day}`
}

export function buildDailyStats() {
  const base = new Date()
  return Array.from({ length: 7 }).map((_, index) => {
    const current = new Date(base)
    current.setDate(base.getDate() - index)
    const entryCount = 120 + index * 8
    const exitCount = 110 + index * 7
    const orderCount = 96 + index * 5
    const incomeAmount = 2680 + index * 186
    return {
      statId: index + 1,
      lotId: index % 2 === 0 ? 1001 : 1002,
      statDate: formatDate(current),
      entryCount,
      exitCount,
      inParkCount: 45 + index * 3,
      orderCount,
      paidOrderCount: orderCount - 4,
      incomeAmount: incomeAmount.toFixed(2),
      avgOrderAmount: (incomeAmount / orderCount).toFixed(2),
      turnoverRate: (2.4 + index * 0.16).toFixed(2)
    }
  })
}

export function buildAccountSummary() {
  return {
    accountId: 9001,
    tenantId: 1,
    totalIncomeAmount: '286530.50',
    balanceAmount: '84210.16',
    availableAmount: '76880.16',
    frozenAmount: '7330.00',
    status: '0',
    feeRate: '0.0060',
    minWithdrawAmount: '100.00',
    settleMerchantName: '维途停车运营账户',
    settleBankName: '招商银行深圳科技园支行',
    settleAccountNo: '6225888888888888',
    updateTime: '2026-04-27 10:20:00'
  }
}

export function buildAccountFlows() {
  return [
    {
      flowId: 1,
      bizType: 'PARKING_INCOME',
      bizNo: 'PO202604270001',
      changeType: 'IN',
      changeAmount: '36.00',
      beforeBalance: '84174.16',
      afterBalance: '84210.16',
      remark: '停车订单入账',
      createTime: '2026-04-27 09:30:00'
    },
    {
      flowId: 2,
      bizType: 'WITHDRAW_FREEZE',
      bizNo: 'WD202604260003',
      changeType: 'OUT',
      changeAmount: '5000.00',
      beforeBalance: '89174.16',
      afterBalance: '84174.16',
      remark: '提现申请冻结',
      createTime: '2026-04-26 16:10:00'
    },
    {
      flowId: 3,
      bizType: 'PARKING_INCOME',
      bizNo: 'PO202604260188',
      changeType: 'IN',
      changeAmount: '58.00',
      beforeBalance: '89116.16',
      afterBalance: '89174.16',
      remark: '停车订单入账',
      createTime: '2026-04-26 14:22:00'
    }
  ]
}

export function buildWithdrawRecords() {
  return [
    {
      withdrawId: 1,
      withdrawNo: 'WD202604270001',
      applyAmount: '3000.00',
      feeAmount: '18.00',
      realAmount: '2982.00',
      feeRate: '0.0060',
      status: 'WAIT_AUDIT',
      auditBy: '',
      auditTime: '',
      auditRemark: '',
      payTime: '',
      createTime: '2026-04-27 09:00:00'
    },
    {
      withdrawId: 2,
      withdrawNo: 'WD202604260003',
      applyAmount: '5000.00',
      feeAmount: '30.00',
      realAmount: '4970.00',
      feeRate: '0.0060',
      status: 'PAID',
      auditBy: 'admin',
      auditTime: '2026-04-26 17:10:00',
      auditRemark: '审核通过',
      payTime: '2026-04-26 18:20:00',
      createTime: '2026-04-26 16:10:00'
    },
    {
      withdrawId: 3,
      withdrawNo: 'WD202604230002',
      applyAmount: '8000.00',
      feeAmount: '48.00',
      realAmount: '7952.00',
      feeRate: '0.0060',
      status: 'REJECTED',
      auditBy: 'admin',
      auditTime: '2026-04-23 15:30:00',
      auditRemark: '账户资料待完善',
      payTime: '',
      createTime: '2026-04-23 11:50:00'
    }
  ]
}
