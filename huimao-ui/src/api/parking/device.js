import request from '@/utils/request'

export function listDevice(query) {
  return request({
    url: '/parking/admin/device/list',
    method: 'get',
    params: query
  })
}

export function getDevice(deviceId) {
  return request({
    url: '/parking/admin/device/' + deviceId,
    method: 'get'
  })
}

export function addDevice(data) {
  return request({
    url: '/parking/admin/device',
    method: 'post',
    data: data
  })
}

export function updateDevice(data) {
  return request({
    url: '/parking/admin/device',
    method: 'put',
    data: data
  })
}

export function delDevice(deviceId) {
  return request({
    url: '/parking/admin/device/' + deviceId,
    method: 'delete'
  })
}
