import request from '@/utils/request'

export function listGate(query) {
  return request({
    url: '/parking/admin/gate/list',
    method: 'get',
    params: query
  })
}

export function getGate(gateId) {
  return request({
    url: '/parking/admin/gate/' + gateId,
    method: 'get'
  })
}

export function addGate(data) {
  return request({
    url: '/parking/admin/gate',
    method: 'post',
    data: data
  })
}

export function updateGate(data) {
  return request({
    url: '/parking/admin/gate',
    method: 'put',
    data: data
  })
}

export function delGate(gateId) {
  return request({
    url: '/parking/admin/gate/' + gateId,
    method: 'delete'
  })
}
