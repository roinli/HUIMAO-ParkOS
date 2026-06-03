<template>
  <view class="login-container bg-white">
    <!-- 登录页 -->
    <image :src="IMG_URL + '/login-bg.png'" class="logo-bg-img"></image>

    <view class="logo-wrapper">
		<view class="logo-image-box">
			<image :src="IMG_URL + '/logo.png'" class="logo-user-img"></image>
		</view>
		
		<view class="logo-title-big">欢迎使用慧猫开源停车平台</view>
		
		<view class="form-wrapper">
		  <view class="form-wrapper-title"></view>
			
		  <!-- 账号输入框 -->
		  <view class="input-item">
		   <!-- <text class="input-label">账号</text> -->
		    <u-input
		      v-model="formData.phone"
		      placeholder="请输入账号"
		      type="text"
		      border="false"
		      class="custom-input"
		    />
		  </view>
		
		  <!-- 密码输入框 -->
		  <view class="input-item mt-37">
		  <!--  <text class="input-label">密码</text> -->
		    <u-input
		      v-model="formData.password"
		      placeholder="请输入密码"
		      type="password"
		      border="false"
		      class="custom-input"
		      :password-icon="showPwd"
		      @click-password-icon="showPwd = !showPwd"
		    />
		  </view>
		
		  <view class="login-btn-box">
			  <button class="btn-light" @click="submitLogin()">登录</button>
		  </view>
		  
		</view>
    </view>
	
	<view class="bottom-tips" v-show="!hideTips">
		登录即表示同意<text class="blue">《用户协议》</text>和 <text class="blue">《隐私政策》</text>
	</view>
	
  </view>
</template>

<script>
import { IMG_URL } from '@/env.js'
	
export default {
  name: "LoginPage",
  data() {
    return {
      IMG_URL,
      formData: { 
		  phone: "", 
		  password: "" ,
	  },
      showPwd: false,
	  hideTips: false,
	  originalHeight: 0 // 记录页面原始高度
    };
   },
   onLoad() {
      // #ifdef H5
      this.originalHeight = window.innerHeight;
      window.addEventListener('resize', this.onKeyboardChange);
      // #endif
    },
    onUnload() {
      // #ifdef H5
      window.removeEventListener('resize', this.onKeyboardChange);
      // #endif
    },
  methods:{
	// 监听键盘弹出/收起
	onKeyboardChange() {
		const nowHeight = window.innerHeight;
		// 键盘弹出：页面高度变小 → 隐藏提示
		if (nowHeight < this.originalHeight - 50) {
		  this.hideTips = true;
		} else {
		  // 键盘收起 → 显示提示
		  this.hideTips = false;
		}
	},
	submitLogin() {
		
		if (!this.formData.phone) {
				uni.showToast({
				  title: "请输入账号",
				  icon: "none"
				});
				return;
		}
			
		if (this.formData.password.length < 6) {
			uni.showToast({
			  title: "密码长度不能少于6位",
			  icon: "none"
			});
			return;
		}
			  
		uni.setStorageSync('token', '1234567890');
		//uni.setStorageSync('plateNo', '川A234121');
		
		uni.switchTab({
			url: '/pages/index/index',
		})
		
		return false;
		
	  // 表单校验
	 //  if (!/^1[3-9]\d{9}$/.test(this.formData.phone)) {
		// uni.showToast({
		//   title: "请输入正确的手机号",
		//   icon: "none"
		// });
		// return;
	 //  }
	 
		
	    customerLogin({
			'username': this.formData.phone,
			'password': this.formData.password,
	  	})
	  	.then((res) => {
	  		if (res.code == 200 && res.token) {
	  			uni.setStorageSync('token', res.token);
				uni.switchTab({
					url: '/pages/index/index',
				})
	  		} 
	  	})
	  	.catch((err) => {});
		
	},
		
  },
  
};
</script>

<style scoped lang="scss">
button::after {
	border: none
}
button {
  background-color: #fff;
  border-radius:0;
  margin-left: 0rpx !important;
  margin-right: 0rpx !important;
  padding-left: 0rpx !important;
  padding-right: 0rpx !important;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-direction: column;
}
		
	
.login-container {
  min-height: 100vh;
  padding-bottom: env(safe-area-inset-bottom);
  box-sizing: border-box;
  position: relative;
  position: relative;
}

.logo-bg-img {
  width: 100%;
  height: 559rpx;
}

.logo-wrapper {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  position: fixed;
  top: 240rpx;
  width: 100%;
  flex-direction: column;
  padding: 0rpx 54rpx; 
  box-sizing: border-box;
	  
   .logo-image-box{
	   width: 100%;
   }
		
  .logo-user-img {
    width: 108rpx;
    height: 62rpx;
  }
  
  .logo-title-big{
	font-weight: 700;
	font-size: 44rpx;
	color: #191717;
	margin-top: 50rpx;
	line-height: 1;
  }
  
}

/* 自定义输入框外框 带背景+圆角 */
.input-item {
  display: flex;
  align-items: center;
  width: 100%;
  height: 94rpx;
  padding: 0 27rpx;
  border-radius: 14rpx;
  box-sizing: border-box;
  background: #FFFFFF;
  border: 1rpx solid #E1E2E5;
}

/* 左侧标签 */
.input-label {
  font-weight: bold;
  font-size: 29rpx;
  color: #333333;
  line-height: 32rpx;
}

/* 输入框内容 */
.custom-input {
  width: 100%;
  font-weight: 400;
  font-size: 29rpx;
  color: #656565;
  line-height: 29rpx;
  padding: 0rpx 0rpx !important;
}

.form-wrapper {
  margin-top: 98rpx;
  width: 100%;
  box-sizing: border-box;
  position: relative;
  
  .form-wrapper-title{
  	font-weight: 600;
  	font-size: 29rpx;
  	color: #333333;
	position: relative;
	margin-bottom: 104rpx;
    line-height: 1;
  }
  
  .form-wrapper-title:before{
  	content: "";
  	position: absolute;
  	width: 60rpx;
  	height: 14rpx;
  	background: linear-gradient(90deg, #6688F5, #ffffff 92%);
  	border-radius: 7rpx;
  	bottom: -40rpx;
  	left: 32rpx;
  	margin: 0 auto;
  }
  
  .form-wrapper-title:after{
  	content: "账号登录";
  	position: absolute;
  	left: 0;
  	width: 100%;
  }
  
  
  .mt-37{
	  margin-top: 37rpx;
  }
}

.forget-pwd {
  font-weight: 400;
  font-size: 25rpx;
  color: #91897E;
  margin-top: 70rpx;
  text-align: center;
}

.login-btn-box{
	margin-top: 72rpx;
	position: relative;
	height: 94rpx;
	width: 100%;
	background: #405EF4;
	box-shadow: 0rpx 2rpx 19rpx 0rpx rgba(110,144,241,0.57);
	border-radius: 14rpx;
	
	.btn-grey, .btn-light{
		width: 100%;
		height: 94rpx;
		font-weight: 400;
		font-size: 29rpx;
		color: #FFFFFF;
		display: flex;
		align-items: center;
		justify-content: center;
		background: transparent !important;
		border: none !important;
		position: relative;
		z-index: 2;
	}
}

.bottom-tips{
	font-weight: 400;
	font-size: 23rpx;
	color: #676767;
	text-align: center;
	width: 100%;
	position: absolute;
	bottom: 111rpx;
	left: 0rpx;
	
	.blue{
		color: #405EF4;
	}

	/* #ifdef H5 */
	/* 核心：H5 强制不跟随键盘！！ */
	position: fixed !important;
	bottom: 0 !important;
	padding-bottom: 20rpx !important;
	background: #fff !important;
	z-index: 1;
	/* #endif */
}
</style>