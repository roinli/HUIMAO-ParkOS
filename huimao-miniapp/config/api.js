const http = uni.$u.http

// 按停车场和车牌查询当前待支付订单
export const getWaitpayOrder = (params) => http.get('/parking/open/h5/lot/'+params.params.lotId+'/order', params)

// 按车牌查询当前待支付订单
export const getOrder = (params) => http.get('/parking/open/h5/order', params)

// 发起模拟支付
export const pay = (params) => http.post('/parking/open/h5/order/'+params.params.orderNo+'/pay', params)

// 按车牌查询缴费记录
export const getPaymentRecords = (params) => http.get('/parking/open/h5/payment-records', params)

// 按车牌查询订单记录
export const getSessions = (params) => http.get('/parking/open/h5/sessions', params)

// 按停车场编码查询停车场公开信息 
export const getLotInfo = (params) => http.get('/parking/open/h5/lot/code/'+params.params.lotCode, params)