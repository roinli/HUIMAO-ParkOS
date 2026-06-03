import request from '@/utils/request'

export function getAccountSummary(query) {
  return request({
    url: '/parking/account/summary',
    method: 'get',
    params: query
  })
}

export function applyWithdraw(data) {
  return request({
    url: '/parking/account/withdraw/apply',
    method: 'post',
    data: data
  })
}

//查询账户信息
export function accountInfo(data) {
  return request({
    url: '/parking/account/info',
    method: 'get',
    params: data
  })
}


//分页查询账户提现记录
export function withdrawList(data) {
  return request({
    url: '/parking/account/withdraw/list',
    method: 'get',
    params: data
  })
}

//分页查询账户流水
export function flowList(data) {
  return request({
    url: '/parking/account/flow/list',
    method: 'get',
    params: data
  })
}

