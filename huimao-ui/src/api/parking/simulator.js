import request from '@/utils/request'

export function listSimulatorDevice(query) {
  return request({
    url: '/parking-simulator/admin/device/list',
    method: 'get',
    params: query
  })
}

export function getSimulatorDevice(deviceId) {
  return request({
    url: '/parking-simulator/admin/device/' + deviceId,
    method: 'get'
  })
}

export function startSimulator(deviceId) {
  return request({
    url: '/parking-simulator/admin/device/' + deviceId + '/start',
    method: 'post'
  })
}

export function stopSimulator(deviceId) {
  return request({
    url: '/parking-simulator/admin/device/' + deviceId + '/stop',
    method: 'post'
  })
}

export function onlineSimulator(deviceId) {
  return request({
    url: '/parking-simulator/admin/device/' + deviceId + '/online',
    method: 'post'
  })
}

export function offlineSimulator(deviceId) {
  return request({
    url: '/parking-simulator/admin/device/' + deviceId + '/offline',
    method: 'post'
  })
}

export function heartbeatSimulator(deviceId) {
  return request({
    url: '/parking-simulator/admin/device/' + deviceId + '/heartbeat',
    method: 'post'
  })
}

export function updateSimulatorConfig(data) {
  return request({
    url: '/parking-simulator/admin/device/config',
    method: 'put',
    data: data
  })
}

export function simulateEntry(deviceId, data) {
  return request({
    url: '/parking-simulator/admin/device/' + deviceId + '/entry',
    method: 'post',
    data: data
  })
}

export function simulateExit(deviceId, data) {
  return request({
    url: '/parking-simulator/admin/device/' + deviceId + '/exit',
    method: 'post',
    data: data
  })
}
