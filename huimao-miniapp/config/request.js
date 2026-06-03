import md5Utils from '@/config/md5Utils.js'
import {API_URL} from '@/env'

// 此vm参数为页面的实例，可以通过它引用vuex中的变量
module.exports = (vm) => {
	// 初始化请求配置
	uni.$u.http.setConfig((config) => {
		/* config 为默认全局配置*/
		config.baseURL = API_URL; /* 根域名 */
		return config
	})

	// 请求拦截
	uni.$u.http.interceptors.request.use((config) => {
		// 初始化请求拦截器时，会执行此方法，此时data为undefined，赋予默认{}
		if(config.custom && config.custom.hideLoading){
			//不展示loading
		}else{
			// uni.showLoading({
			// 	title: '加载中'
			// })
		}

		// #ifdef  APP-PLUS
		config.header.platform = 'APP'
		// #endif
		// #ifdef  MP-WEIXIN
		config.header.platform = 'MP-WEIXIN'
		// #endif
		const timestamp = new Date().getTime();
		config.header.Authorization = "Bearer " + uni.getStorageSync('token')
		config.params.timestamp = timestamp
		const sign = md5Utils.getSign(config.url, {...config.params, ...config.data})
		config.header.sign = sign;
		config.header.timestamp = timestamp;
		config.data = config.data || {}
		// 根据custom参数中配置的是否需要token，添加对应的请求头
		if (config?.custom?.auth) {
			// 可以在此通过vm引用vuex中的变量，具体值在vm.$store.state中
			config.header.token = vm.$store.state.userInfo.token
		}
		// console.log(config)
		return config
	}, config => {
		return Promise.reject(config)
	})

	// 响应拦截
	uni.$u.http.interceptors.response.use((response) => {
		console.log("response", response);
		const data = response.data
		//未登录跳转登录页面
		if(data && data.code && data.code == 401){
			uni.reLaunch({
				url: '/pages/index/login/login',
			})
		}
		
		uni.hideLoading()
		return data.result === undefined ? data : data
	}, (response) => {
		return Promise.reject(response)
	})
}
