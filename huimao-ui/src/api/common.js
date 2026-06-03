
import request from '@/utils/request'


export function getProvinceList(data) {
  return request({
    url: '/parking/city/getProvinceList',
    method: 'get',
    params: data
  });
}

export function getCityList(data) {
  return request({
    url: '/parking/city/getCityList',
    method: 'get',
    params: data
  });
}

export function getCities(provinceId) {
  return request({
    url: '/parking/city/getCities',
    method: 'get',
    params: { provinceId }
  });
}

export function getAreas(cityId) {
  return request({
    url: '/parking/city/getAreas',
    method: 'get',
    params: { cityId }
  });
}

export function getMapGeocoder(params) {
  return request({
    url: '/parking/qq/map/geocoder',
    method: 'get',
    params: params
  });
}

export function getMapSuggestion(params) {
  return request({
    url: '/parking/qq/map/suggestion',
    method: 'get',
    params: params
  });
}

// 地理编码：地址查询经纬度
export function geocoderByAddress(params) {
  return request({
    url: '/parking/qq/map/geocoder/address',
    method: 'get',
    params: params
  });
}
