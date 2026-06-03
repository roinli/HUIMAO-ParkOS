import { API_URL } from '@/env'

export default {
	
	//图片处理-上传图片
	uploadImageQuiet(api, url) {
		let token = uni.getStorageSync('token');
		
		let config_url = API_URL;
	
		return new Promise((resolve, reject) => {
			uni.uploadFile({
				url: config_url + api,
				filePath: url,
				name: 'file',
				header: {
					"Authorization": "Bearer " + token
				},
				success: res => {
					res = JSON.parse(res.data);
					resolve(res.data)
				}
			});
		}).catch(e => {
			reject(e)
		})
	},
	jump(path){
		uni.navigateTo({
			url:path
		})
	},
	maskedName (val) {
	  if (!val || val === '') return ''
	  let name = ''
	  if (val.length === 2) {
		name = val.substring(0, 1) + '*' // 截取name 字符串截取第一个字符，
	  } else if (val.length === 3) {
		name = val.substring(0, 1) + '*' + val.substring(2, 3) // 截取第一个和第三个字符
	  } else if (val.length === 4) {
		name = val.substring(0, 2) + '*' + '*' // 4个字隐藏后面两个
	  } else if (val.length > 4) {
		name = val.substring(0, 1) // 5个字只显示第一个字
		for (let i = 0; i < val.length - 1; i++) {
		  name = name + '*'
		}
	  }
	  return name
	},
	maskedPhoneNumber(phone) {
	      if (phone.length === 11) { // 确保是11位手机号
	        return phone.substr(0, 3) + '****' + phone.substr(7); 
	      } else {
	        return phone; // 如果不是标准长度，则不进行任何处理
	      }
	},
	maskedCardNumber (val) {
	  if (!val) return ''
	  let reg = /^(.{3}).*(.{2})$/
	  return val.replace(reg, '$1*************$2')
	},
	/**提示框
	 *title(标题)
	 *icon(图标):  success，loading，none
	 *duration(延时): 0为不关闭, 毫秒数
	 *options(其它参数)
	 */
	toast(title, icon = 'none', options) {
		uni.showToast({
			title: title || '',
			icon: icon,
			duration: (options && options.duration) || 1500,
			image: (options && options.image) || '',
			mask: (options && options.mask) || true,
		});
	},
	// 打电话
	callPhone(phoneNumber = '') {
		let num = phoneNumber.toString()
		uni.makePhoneCall({
			phoneNumber: num,
			fail(err) {
				console.log('makePhoneCall出错', err)
			},
		});
	},
	// 图片处理-预览图片
	previewImage(urls = [], current = 0) {
		uni.previewImage({
			urls: urls,
			current: current,
			indicator: 'default',
			loop: true,
			fail(err) {
				console.log('previewImage出错', urls, err)
			},
		})
	},
	//浮点数乘法
	NumberMul(arg1, arg2){
		var m = 0;
		var s1 = arg1.toString();
		var s2 = arg2.toString();
		try {
			m += s1.split(".")[1].length;
		} catch (e) {}
		try {
			m += s2.split(".")[1].length;
		} catch (e) {}
	 
		return Number(s1.replace(".", "")) * Number(s2.replace(".", "")) / Math.pow(10, m);
	},
	//除数，被除数， 保留的小数点后的位数
	NumberDiv (arg1, arg2,digit){
		var t1=0,t2=0,r1,r2;
		try{t1=arg1.toString().split(".")[1].length}catch(e){}
		try{t2=arg2.toString().split(".")[1].length}catch(e){}
		
		r1 = Number(arg1.toString().replace(".",""))
		r2 = Number(arg2.toString().replace(".",""))
		
		//获取小数点后的计算值
		var result = ((r1/r2)*Math.pow(10,t2-t1)).toString()
		var result2 = result.split(".")[1];
		result2 = result2.substring(0,digit>result2.length?result2.length:digit);
	 
		return Number(result.split(".")[0]+"."+result2);
	},
	//浮点型加法
	NumberAdd(arg1, arg2){
		var r1, r2, m, n;
		 try {
			 r1 = arg1.toString().split(".")[1].length
		 } catch (e) {
			 r1 = 0
		 }
		 
		 try {
			 r2 = arg2.toString().split(".")[1].length
		 } catch (e) { 
			 r2 = 0 
		 } 
		 
		m = Math.pow(10, Math.max(r1, r2))
		n = (r1 >= r2) ? r1 : r2;
		 
		return ((arg1 * m + arg2 * m) / m).toFixed(n);
	 },
	 //浮点型减法
	 NumberSub(arg1, arg2){
		var re1, re2, m, n;
		try {
			re1 = arg1.toString().split(".")[1].length;
		} catch (e) {
		re1 = 0;
		}
		try {
			re2 = arg2.toString().split(".")[1].length;
		} catch (e) {
			re2 = 0;
		}
		m = Math.pow(10, Math.max(re1, re2)); 
		n = (re1 >= re2) ? re1 : re2;
		
		return ((arg1 * m - arg2 * m) / m).toFixed(n); 
	},
	hidePhone(phone) {
	   return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
	},
	getUrlParams(url) {
		let params = {};
		if (url.indexOf('?') !== -1) {
			let query = url.split('?')[1];
			let arr = query.split('&');
			arr.forEach(item => {
				let key = item.split('=')[0];
				let val = item.split('=')[1];
				params[key] = decodeURIComponent(val);
			});
		}
		
		return params;
	}
		 
}
