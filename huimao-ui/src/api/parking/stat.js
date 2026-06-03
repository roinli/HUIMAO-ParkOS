import request from '@/utils/request'

export function listStat(query) {
  return request({
    url: '/parking/admin/stat/daily',
    method: 'get',
    params: query
  })
}

export function getStatOverview(query) {
  return request({
    url: '/parking/admin/stat/overview',
    method: 'get',
    params: query
  })
}

export function getDashboard(query) {
  return request({
    url: '/parking/admin/stat/dashboard',
    method: 'get',
    params: query
  })
}

export function refreshStat(query) {
  return request({
    url: '/parking/admin/stat/refresh',
    method: 'get',
    params: query
  })
}
