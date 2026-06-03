<template>
  <view class="home-page">
	  
	<u-navbar
	    back-text=""
	    title="首页"
	    :border="false"
	    bgColor="transparent"
	    placeholder
	    :leftIcon="false" 
	>
	   <view class="u-nav-slot" slot="left"></view>
	</u-navbar>
	  
    <!-- 顶部 Banner -->
	<swiper class="banner-swiper" :indicator-dots="false" 
	:autoplay="true" :interval="3000" :duration="500" circular>
		<swiper-item v-for="(item, index) in bannerList" :key="index">
			<image class="banner-img" :src="IMG_URL + item"></image>
		</swiper-item>
	</swiper>
		
    <!-- 功能入口区 -->
   <!-- <view class="func-wrap">
       <view class="func-item" @tap="goToRecord">
			<image :src="IMG_URL + '/index-left.png'" class="left-img"></image>
			<text class="func-text">停车记录</text>
       </view>
	  
	    <view class="func-item">
			<button
			  open-type="contact" 
			  show-message-card="true"
			>
			<image :src="IMG_URL + '/index-right.png'" class="right-img"></image>
			  <text class="func-text">联系客服</text>
			</button>
		</view>
    </view> -->
	
	<car-license-input  v-if="status === 'unlogin' || status === 'noCar'"
	  v-model="plateNumber" 
	  ref="licenseInput"
	  @addCar="addCar"
	></car-license-input>


    <!-- 核心状态区（4种状态） -->
    <view class="status-card">
      <!-- 状态1：未登录 -->
    <!--  <view v-if="status === 'unlogin'" class="unlogin-wrap">
        <view class="login-btn" @tap="handleLogin">登录</view>
        <text class="tip-text">点击登录，查看车辆/订单</text>
      </view> -->

      <!-- 状态2：已登录，未绑定车辆 -->
     <!-- <view v-else-if="status === 'noCar'" class="no-car-wrap">
       <image :src="IMG_URL + '/car.png'" class="car-img"></image>
        <view class="add-car-btn" @tap="goToAddCar">添加车辆</view>
        <text class="tip-text">您还未绑定车辆，请先添加车辆</text>
      </view> -->

      <!-- 状态3：已登录，已绑定车辆，无订单 -->
      <view v-if="status === 'noOrder'" class="car-info-wrap">
        <view class="car-info-left">
          <text class="car-info-title">我的车辆</text>
          <text class="plate-number">{{ carInfo.plateNo }}</text>
          <text class="empty-tip">未查询到停车订单</text>
        </view>
      </view>

      <!-- 状态4：有订单待支付 -->
      <view v-else-if="status === 'hasOrder'" class="order-pay-wrap">
        <view class="order-info">
          <text class="plate-number">{{ orderInfo.plateNo }}</text>
          <text class="park-name">{{ orderInfo.lotName }}</text>
          <text class="entry-time">{{ orderInfo.entryTime }}入场</text>
        </view>
        <view class="bill-wrap">
          <view class="bill-title">账单概览</view>
          <view class="bill-item">
            <text class="label">停车时长</text>
            <text class="value">{{ formatTime(orderInfo.parkingMinutes) }}</text>
          </view>
          <view class="bill-item" v-if="orderInfo.paidAmount > 0">
            <text class="label">已支付费用</text>
            <text class="price">¥{{ orderInfo.paidAmount.toFixed(2) }}</text>
          </view>
		  <view class="bill-item">
		    <text class="label">待支付费用</text>
		    <text class="price">¥{{ orderInfo.payableAmount.toFixed(2) }}</text>
		  </view>
          <view class="total-wrap">
            <text class="total-label">合计 ¥{{ orderInfo.payableAmount.toFixed(2) }}</text>
          </view>
		  <view class="pay-btn-box">
			  <view class="pay-btn" @tap="goToPay">立即支付</view>
		  </view>
        </view>
      </view>
    </view>
	
	<view class="function-section">
	  <view class="section-box">
		  <view class="section-line"></view>
		  <view class="section-title">快捷导航</view>
	  </view>
	  
	  <view class="function-grid">
		<view class="function-item">
		  <image :src="IMG_URL + '/index_icon1.png'" class="function-icon" mode="heightFix"></image>
		  <view class="function-name">月卡</view>
		</view>
		<view class="function-item">
		  <image :src="IMG_URL + '/index_icon2.png'" class="function-icon" mode="heightFix"></image>
		  <view class="function-name">电子发票</view>
		</view>
		<view class="function-item" @click="goToRecord">
		  <image :src="IMG_URL + '/index_icon3.png'" class="function-icon" mode="heightFix"></image>
		  <view class="function-name">查订单</view>
		</view>
		<view class="function-item">
		  <image :src="IMG_URL + '/index_icon4.png'" class="function-icon" mode="heightFix"></image>
		  <view class="function-name">找车位</view>
		</view>
	  </view>
	</view>
	
	
	<view class="hack"></view>
	
	<Tabbar :currentPage="1" />

  </view>
</template>

<script>
import { IMG_URL } from '@/env.js'
import Tabbar from "../../components/tabbar/tabbar.vue";
import { getWaitpayOrder, getOrder } from "@/config/api.js";
import CarLicenseInput from '@/components/car-license-input.vue'

export default {
  data() {
    return {
	  IMG_URL,
      // 当前状态：unlogin/noCar/noOrder/hasOrder,
	  
	  bannerList:[
		   '/banner.png',
	  ],
	  
      status: 'unlogin',

      // 车辆信息（已绑定车辆时用）
      carInfo: {
        plateNo: '',
      },

      // 订单信息
      orderInfo: {},
	  plateNumber: '', // 车牌号码
	  scrollTop:0,
	  
    }
  },
  components: {
  	  Tabbar,
	  CarLicenseInput
  },
   onPageScroll(e) {
      this.scrollTop = e.scrollTop;
   },
  onLoad(options) {
      if (options && options.lotCode) {
          let lotCode = options.lotCode
          console.log('检测到停车场参数：', lotCode)
          
          uni.navigateTo({
            url: '/pages/sub-packages/scan?lotCode=' + lotCode
          })
        }
  },
  onShow(){
	  let token = uni.getStorageSync('token');
	  if(!token){
	  	this.status = 'unlogin';
	  }else{
		let plateNo = uni.getStorageSync('plateNo')
		if(plateNo){
			this.carInfo.plateNo = plateNo;
			this.getWaitpayOrder();
		}else{
			this.status = 'noCar';
		}
	  }
  },
  methods: {
	addCar(plateNo){
		let token = uni.getStorageSync('token');
		if(!token){
			uni.navigateTo({
				url:'/pages/index/login/login',
			})
			
			return false;
		}
		
		if(plateNo){
			this.carInfo.plateNo = plateNo;
			uni.setStorageSync('plateNo', plateNo);
			this.getWaitpayOrder();
		}
	},
	getWaitpayOrder(){
		let request = {
			plateNo: this.carInfo.plateNo,
		};
		
		getOrder({params: request}).then((res) => {
			if (res.code == 200) {
				//PAID, UNPAID
				if(res.data && res.data.orderStatus == 'UNPAID'){
					this.status = 'hasOrder';
					this.orderInfo = res.data;
				}else{
					this.status = 'noOrder';
				}
				
			} else{
				this.status = 'noOrder';
			}
		})
		.catch(err => {
			this.status = 'noOrder';
		});
	},  
    // 模拟登录
    handleLogin() {
		uni.redirectTo({
			url:'/pages/index/login/login',
		})
    },

    // 跳转到停车记录
    goToRecord() {
		let token = uni.getStorageSync('token');
		if(token){
			uni.navigateTo({ url: '/pages/sub-packages/parkingRecord' })
		}else{
			uni.navigateTo({ url: '/pages/index/login/login' })
		}
    },
	
    // 跳转到添加车辆
    goToAddCar() {
      uni.navigateTo({ url: '/pages/sub-packages/carBind' })
    },

    // 跳转到支付页
    goToPay() {
        uni.navigateTo({ 
		   url: '/pages/sub-packages/pay?lotId='+this.orderInfo.lotId + '&plateNo='+ this.orderInfo.plateNo 
		})
    },
	// 转换分钟为 小时:分钟
	formatTime(minutes) {
	  if (!minutes) return '0分钟'
	  
	  const h = Math.floor(minutes / 60)
	  const m = minutes % 60
	  
	  if (h === 0) {
	    return `${m}分钟`
	  }
	  if (m === 0) {
	    return `${h}小时`
	  }
	  return `${h}小时${m}分钟`
	},
	
	
	
  }
}
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
}
	
.home-page {
    width: 100%;
    min-height: 100vh;
    padding-bottom: 40rpx;
    background-color: #F8F8F8;
    background-image: linear-gradient(-24deg, #F0F4FF, #F3FDFC);
    background-size: 100% 521rpx; 
    background-repeat: no-repeat;
    background-position: top;
}

/* 顶部 Banner */
.banner-swiper {
  position: relative;
  margin: 60rpx 20rpx 32rpx 20rpx;
  overflow: hidden;
  height: 272rpx;
}
.banner-img {
  width: 100%;
  height: 272rpx;
}

.main-title {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  color: #fff;
  margin-bottom: 12rpx;
}
.sub-title {
  font-size: 28rpx;
  color: #fff;
  opacity: 0.9;
}

/* 功能入口 */
.func-wrap {
  display: flex;
  margin: 0 20rpx 20rpx;
  gap: 20rpx;
}

.func-item {
  flex: 1;
  height: 97rpx;
  background: #FFFFFF;
  border-radius: 14rpx;
  display: flex;
  align-items: center;
  padding-left: 36rpx;
}

.left-img{
	width: 45rpx;
	height: 61rpx;
	margin-right: 34rpx;
}

.right-img{
	width: 50rpx;
	height: 61rpx;
	margin-right: 34rpx;
}

.func-text {
  font-weight: 400;
  font-size: 28rpx;
  color: #1A1A1A;
}

/* 状态卡片 */
.status-card {
  margin: 0 20rpx;
  background-color: #fff;
  border-radius: 14rpx;
}

/* 1. 未登录状态 */
.unlogin-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 324rpx;
  justify-content: center;
}

.login-btn {
  width: 429rpx;
  height: 82rpx;
  background: #405EF4;
  border-radius: 14rpx;
  font-weight: 400;
  font-size: 28rpx;
  color: #FFFFFF;
  margin-bottom: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 2. 未绑定车辆 */
.no-car-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 483rpx;
  justify-content: center;
}

.car-img{
	width: 258rpx;
	height: 117rpx;
	margin-bottom: 40rpx;
}

.add-car-btn {
  width: 429rpx;
  height: 82rpx;
  background: #405EF4;
  border-radius: 14rpx;
  font-weight: 400;
  font-size: 28rpx;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 58rpx;
}

.tip-text {
  font-weight: 400;
  font-size: 25rpx;
  color: #666666;
}

/* 3. 已绑定车辆，无订单 */
.car-info-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 278rpx;
  padding-left: 50rpx;
}
.car-info-left{
	display: flex;
	flex-direction: column;
}

.car-info-left .car-info-title {
  font-weight: 400;
  font-size: 25rpx;
  color: #272727;
  margin-bottom: 39rpx;
  line-height: 1;
}
.car-info-left .plate-number {
  font-weight: bold;
  font-size: 35rpx;
  color: #405EF4;
  line-height: 1;
  margin-bottom: 40rpx;
}
.empty-tip {
  font-weight: 400;
  font-size: 28rpx;
  color: #666666;
  line-height: 1;
}

.noOrder-car-img{
	width: 258rpx;
	height: 117rpx;
}


/* 4. 有订单待支付 */
.order-info{
	height: 235rpx;
	background: #FFFFFF;
	border-radius: 14rpx 14rpx 0rpx 0rpx;
	padding-left: 47rpx;
	display: flex;
	justify-content: center;
	flex-direction: column;
}
.order-info .plate-number {
  font-weight: bold;
  font-size: 35rpx;
  color: #272727;
  line-height: 1;
  margin-bottom: 27rpx;
}
.order-info .park-name {
  font-weight: 400;
  font-size: 25rpx;
  color: #444444;
  line-height: 1;
  margin-bottom: 27rpx;
}
.order-info .entry-time {
  font-weight: 400;
  font-size: 25rpx;
  color: #999999;
  line-height: 1;
}
.bill-wrap {
  background: linear-gradient(0deg, #FFFFFF 0%, #F3F7FF 100%);
  border-radius: 14rpx;
  padding: 33rpx 46rpx;
  box-sizing: border-box;
}
.bill-title {
  font-weight: 600;
  font-size: 25rpx;
  color: #000;
  line-height: 1;
  margin-bottom: 35rpx;
}
.bill-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 38rpx;
}
.bill-item .label {
  font-weight: 400;
  font-size: 25rpx;
  color: #666666;
  line-height: 1;
}
.bill-item .value {
 font-weight: 400;
 font-size: 25rpx;
 color: #666666;
 line-height: 1;
}
.bill-item .price {
  font-weight: 600;
  font-size: 25rpx;
  color: #1D1D1D;
  line-height: 1;
}
.total-wrap {
  display: flex;
  justify-content: space-between;
  margin-top: 30rpx;
}
.total-label {
  font-weight: 600;
  font-size: 25rpx;
  color: #1D1D1D;
  line-height: 1;
}
.total-price {
  font-weight: 400;
  font-size: 25rpx;
  color: #1D1D1D;
  line-height: 1;
}
.pay-btn-box{
	width: 100%;
	display: flex;
	align-items: center;
	justify-content: center;
}
.pay-btn {
	width: 429rpx;
	height: 82rpx;
	background: #405EF4;
	border-radius: 14rpx;
	font-weight: 400;
	font-size: 28rpx;
	color: #FFFFFF;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-top: 48rpx;
}

/* 常用功能区 */
.function-section {
  margin: 20rpx 20rpx 20rpx 20rpx;
  background-color: #ffffff;
  padding: 38rpx 0rpx 64rpx 0rpx;
  box-sizing: border-box;
  border-radius: 14rpx;
  
  .section-box{
	  display: flex;
	  align-items: center;
	  margin-bottom: 63rpx;
	  box-sizing: border-box;
	  padding-left: 34rpx;
  }
  
  .section-line{
	  width: 7rpx;
	  height: 29rpx;
	  background: #405EF4;
	  border-radius: 3rpx;
	  margin-right: 14rpx;
  }

  .section-title {
    font-weight: 600;
    font-size: 40rpx;
    color: #333333;
    line-height: 1;
  }

  .function-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 56rpx 0;
    padding: 0rpx 0rpx 0rpx 0rpx;
	border-radius: 14rpx;

    .function-item {
      display: flex;
      flex-direction: column;
      align-items: center;

      .function-icon {
        width: 53rpx;
        height: 46rpx;
        margin-bottom: 21rpx;
      }

      .function-name {
		font-weight: 400;
		font-size: 32rpx;
		color: #666666;
      }
    }

  }
}

.hack{
	height: 200rpx;
}
</style>