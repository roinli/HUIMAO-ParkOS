import * as wx from 'weixin-js-sdk'


let _wxconfig = {}
let _apiFunc = null

const ua = window.navigator.userAgent.toLowerCase();

/**
 * @param {Promise} api promise对象，返回微信配置信息
 */
const $init = () => {
    return new Promise(async (resolve, reject) => {
        if (!isWechat) {
            reject(new Error('不是微信环境'))
        }
        if (typeof _apiFunc !== 'function') {
            reject(new Error('请先初始化微信sdk'))
        }
        try {
            const config = await _apiFunc()

            const { appId, timestamp, nonceStr, signature, debug = false } = _wxconfig = config
            if (!appId || !timestamp || !nonceStr || !signature) {
                throw new Error('微信配置信息不完整,字段名称必须为 appId, timestamp, nonceStr, signature')
            }
            wx.config({
                debug, //是否开启调试模式
                appId, //后端返回的公众号/服务号appid
                timestamp, //后端返回的时间戳
                nonceStr,
                signature,
                jsApiList: [ //这个是SDK要进行初始化的功能配置,具体可参照下面给出的
                    'checkJsApi',
                    'onMenuShareTimeline',
                    'onMenuShareAppMessage',
                    'updateAppMessageShareData',
                    'updateTimelineShareData',
                    'showAllNonBaseMenuItem',
                    'chooseWXPay',
                    'scanQRCode',
                    'getLocation',
                    'openLocation'
                ]
            });
            wx.ready((res) => {
                console.log('%c wx-sdk init success', 'background: green; color: #fff;');
                resolve(config)
            })
            wx.error((err) => {
                console.log(err);
                reject(err)
            })
        } catch (err) {
            console.log(err);
            reject(err)
        }
    })
}

let globalInstance = null
const $$createWxInstance = async () => {
    if (!globalInstance) {
        globalInstance = $init()
        return globalInstance
    }
    try {
        return await globalInstance
    } catch (error) {
        globalInstance = $init()
    }
    return globalInstance
}
const checkInit = async () => {
    if (!isWechat) {
        throw new Error('不是微信环境')
    }
    return $$createWxInstance()
}


export const getUrl = () => window.location.href.split('#')[0]
// 获取code
export const getUrlCode = (name) => decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ''])[1].replace(/\+/g, '%20')) || null

// 判断是否在微信中 
export const isWechat = ua.match(/micromessenger/i) == 'micromessenger'
// 获取微信网页授权函数
export const getWeixinAuth = (appId) => {
    //检测是否是微信浏览器
    if (isWechat) {
        const redirect_url = window.location.href //本地地址的赋值
        const local = encodeURIComponent(redirect_url) //对地址进行url转义,以防出错
        const code = getUrlCode('code') //获取你的当前地址栏的code
        //如果没有code的情况下进行微信授权重定向
        if (code == null || code == '') {
            window.location.href = 'https://open.weixin.qq.com/connect/oauth2/authorize?appid=' + appId || _wxconfig.appId + '&redirect_uri=' + local + '&response_type=code&scope=snsapi_userinfo&forcePopup=true&state=abcdefghigklmnopqrstuvwxyz#wechat_redirect'
        }
    } else {
        uni.showToast({
            title: '请使用微信浏览器登录!',
            icon: 'error',
            duration: 2000
        })
    }
}

export const injectSignFuncWrap = async (api, autoLoad = true) => {
    _apiFunc = api
    // return autoLoad && $$createWxInstance
}
export const pay = async (api) => {
    await checkInit()
    const { appId, timestamp, nonceStr, package: pkg, signType, paySign } = await api
    if (!appId || !timestamp || !nonceStr || !pkg || !signType || !paySign) {
        throw new Error('微信配置信息不完整,字段名称必须为 appId, timestamp, nonceStr, signature, package, signType, paySign')
    }
    return new Promise((resolve, reject) => {
        wx.chooseWXPay({
            appId,
            timestamp, // 支付签名时间戳，注意微信jssdk中的所有使用timestamp字段均为小写。但最新版的支付后台生成签名使用的timeStamp字段名需大写其中的S字符  
            nonceStr, // 支付签名随机串，不长于 32 位  
            package: pkg, // 统一支付接口返回的prepay_id参数值，提交格式如：prepay_id=\*\*\*）  
            signType, // 签名方式，默认为'SHA1'，使用新版支付需传入'MD5'  
            paySign, // 支付签名  
            success: (res) => {
                resolve(res)
            },
            fail: (err) => {
                reject(err)
            },
        })
    })

}

//初始化code传输给后端
export const getKeyFun = async () => {
    if (!isWechat) return
    const code = getUrlCode('code')
    const res = await getKey({
        code: code
    })
    console.log('返回的openid', res)
    if (res.data.code == 0) {
        uni.setStorageSync('openid1', res.data.data.openid)
    }
}

/**
 * @param {String} type  wgs84/gcj02
 */

export const getLocation = async (type = 'gcj02') => {
    await checkInit()
    return new Promise((resolve, reject) => {
        wx.getLocation({
            type,
            isHighAccuracy: true, // 开启高精度定位
            success: (res) => {
                res.lat = res.latitude
                res.lng = res.longitude

                resolve(res)
            },
            fail: reject
        })
    })
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

export const openLocation = async (params = {}) => {
    await checkInit()
    return new Promise((resolve, reject) => {
        wx.openLocation({
            ...params,
            success: resolve,
            fail: reject
        })
    })
}

/**
 * @param {Object} options
 * @param {Number} options.needResult 默认为1
 * @param {Array} options.scanType 默认为['qrCode', 'barCode']
 */

export const scanCode = async ({ needResult = 1, scanType = ["qrCode", "barCode"] } = {}) => {
    await checkInit()
    return new Promise((resolve, reject) => {
        wx.scanQRCode({
            needResult, // 默认为0，扫描结果由微信处理，1则直接返回扫描结果，
            scanType, // 可以指定扫二维码还是一维码，默认二者都有
            success: (res) => {
                res.result = res.resultStr
                resolve(res)
            },
            fail: (err) => {
							console.log(err)
							reject(err)
						}
        });
    })
}