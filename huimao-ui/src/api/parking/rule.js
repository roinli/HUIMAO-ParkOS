import request from '@/utils/request'

export function listRule(query) {
  return request({
    url: '/parking/admin/rule/list',
    method: 'get',
    params: query
  })
}

export function getRule(ruleId) {
  return request({
    url: '/parking/admin/rule/' + ruleId,
    method: 'get'
  })
}

export function addRule(data) {
  return request({
    url: '/parking/admin/rule',
    method: 'post',
    data: data
  })
}

export function updateRule(data) {
  return request({
    url: '/parking/admin/rule',
    method: 'put',
    data: data
  })
}

export function delRule(ruleId) {
  return request({
    url: '/parking/admin/rule/' + ruleId,
    method: 'delete'
  })
}
