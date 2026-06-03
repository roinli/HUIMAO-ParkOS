/**
 * @param {String} type  wgs84/gcj02
 */

export const getLocation = (type = 'wgs84') => {
    // #ifdef H5
    return new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(pos => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }), reject, { timeout: 5000 })
    })
    // #endif
    // #ifndef H5
    return new Promise((resolve, reject) => {
        uni.getLocation({
            type,
            success: resolve,
            fail: reject
        })
    })
    // #endif
}

/**
 * @param {Object} params
 * @param {String} params.latitude  纬度
 * @param {String} params.longitude  经度
 * @param {String} params.name  位置名
 * @param {String} params.address  详细地址
 * @param {Number} params.scale  缩放级别
 * @param {String} params.infoUrl  在查看位置界面底部显示的超链接,可点击跳转
 */
export const openLocation = (params = {}) => {
    return new Promise((resolve, reject) => {
        uni.openLocation({
            ...params,
            success: resolve,
            fail: reject
        })
    })
}

/**
 * @param {Object} options
 * @param {Array} options.scanType 默认为['qrCode', 'barCode']
 */

export const scanCode = ({ }) => {
    return new Promise((resolve, reject) => {
        uni.scanCode({
            autoZoom: false,
            success: resolve,
            fail: reject
        });
    })
}