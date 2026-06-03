import request from '@/utils/request'

export function listLot(query) {
  return request({
    url: '/parking/admin/lot/list',
    method: 'get',
    params: query
  })
}

export function getLot(lotId) {
  return request({
    url: '/parking/admin/lot/' + lotId,
    method: 'get'
  })
}

export function addLot(data) {
  return request({
    url: '/parking/admin/lot',
    method: 'post',
    data: data
  })
}

export function updateLot(data) {
  return request({
    url: '/parking/admin/lot',
    method: 'put',
    data: data
  })
}

export function delLot(lotId) {
  return request({
    url: '/parking/admin/lot/' + lotId,
    method: 'delete'
  })
}
