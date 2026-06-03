import request from '@/utils/request'

export function listWithdraw(query) {
  return request({
    url: '/parking/admin/withdraw/list',
    method: 'get',
    params: query
  })
}

export function reviewWithdraw(data) {
  return request({
    url: '/parking/admin/withdraw/review',
    method: 'post',
    data: data
  })
}
