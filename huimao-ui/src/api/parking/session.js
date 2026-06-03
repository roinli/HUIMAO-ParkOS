import request from '@/utils/request'

export function listSession(query) {
  return request({
    url: '/parking/admin/session/list',
    method: 'get',
    params: query
  })
}

export function getSession(sessionId) {
  return request({
    url: '/parking/admin/session/' + sessionId,
    method: 'get'
  })
}
