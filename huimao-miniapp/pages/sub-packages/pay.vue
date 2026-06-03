<template>
  <view class="parking-payment-page">
	  
   <image :src="IMG_URL + '/pay-bg.png'" class="pay-bg-img"></image>
  
	<u-navbar
	  back-text=""
	  title="停车缴费"
	  :border="false"
	  bgColor="transparent"
	  placeholder
	  @leftClick="goBack"
	></u-navbar>
	
  
    <!-- 头部宣传区 -->
    <view class="banner">
      <view class="banner-text">
        <text class="banner-title">智享停车新生活</text>
        <text class="banner-subtitle">一键快捷缴费 出行自在无忧</text>
      </view>
      <image :src="IMG_URL + '/car.png'" class="car-img"></image>
    </view>

    <!-- 车辆信息卡片 -->
    <view class="info-card">
      <view class="card-item">
        <view class="label">待缴费车辆</view>
        <view class="plate-row">
          <text class="plate-number">{{ orderInfo.plateNo }}</text>
          <view class="status-tag">
             <image :src="IMG_URL + '/pay-wait.png'" class="status-icon"></image>
            <text class="status-text">待付款</text>
          </view>
        </view>
      </view>
      <view class="card-item">
        <view class="label">当前停车场</view>
        <view class="value">{{ orderInfo.lotName }}</view>
      </view>
      <view class="card-item">
        <view class="label">入场时间</view>
        <view class="value">{{ orderInfo.entryTime }}入场</view>
      </view>
    </view>

    <!-- 账单概览卡片 -->
    <view class="bill-card">
      <view class="bill-header">
        <view class="mark"></view>
        <text class="bill-title">账单概览</text>
      </view>
      <view class="bill-item">
        <text class="bill-label">停车时长</text>
        <text class="bill-value">{{ formatTime(orderInfo.parkingMinutes) }}</text>
      </view>
      <view class="bill-item">
        <text class="bill-label">停车费用</text>
        <text class="bill-price">¥{{ orderInfo.payableAmount.toFixed(2) }}</text>
      </view>
    </view>

    <!-- 底部支付栏 -->
    <view class="footer">
      <view class="total">
        <text class="total-label">合计 ¥</text>
        <text class="total-price">{{ orderInfo.payableAmount.toFixed(2) }}</text>
      </view>
      <view class="pay-btn" @click="handlePay">立即支付</view>
    </view>
  </view>
</template>

<script>
import { IMG_URL } from '@/env.js'
import { getWaitpayOrder, getOrder, pay } from "@/config/api.js";
	
export default {
  data() {
    return {
	  IMG_URL,
      orderInfo: {},
	  lotId:0,
	  plateNo:'',
    };
  },
  onLoad(options) {
	console.log("options", options);
	
	this.lotId =  options.lotId;
	this.plateNo =  options.plateNo;
	  
    let token = uni.getStorageSync('token');
  	if(!token){
		uni.navigateTo({
			url:'/pages/index/login/login',
		})
  	}else{
  		this.getWaitpayOrder();
  	}
  },
  methods: {
    goBack() {
      uni.navigateBack();
    },
	getWaitpayOrder(){
		let request = {
			lotId: this.lotId,
			plateNo: this.plateNo,
		};
		
		getWaitpayOrder({params: request}).then((res) => {
			if (res.code == 200) {
				this.orderInfo = res.data;
			} else{
				uni.showToast({
					title: res.msg,
				})
			}
		})
		.catch(err => {
		});
	},  
    handlePay() {
		pay({params: {
			orderNo: this.orderInfo.orderNo
		}}).then((res) => {
			if (res.code == 200) {
				
				uni.redirectTo({
					url: '/pages/sub-packages/paySuccess?orderNo='+this.orderInfo.orderNo,
				})
				
			} else{
				uni.showToast({
					title: res.msg,
				})
			}
		})
		.catch(err => {
			
		});
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
	}
	
  }
};
</script>

<style scoped>
.parking-payment-page {
  width: 100%;
  min-height: 100vh;
  background-color: #F8F8F8;
/*  display: flex;
  flex-direction: column;
  background-image: url('/static/images/pay-bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed; */
}

.pay-bg-img{
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
}

/* 头部宣传区 */
.banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 70rpx 47rpx 72rpx 47rpx;
  overflow: hidden;
  z-index: 2;
  position: relative;
}

.banner-text {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.banner-title {
  font-weight: bold;
  font-size: 40rpx;
  color: #212121;
  line-height: 1;
}

.banner-subtitle {
  font-weight: 400;
  font-size: 28rpx;
  color: #666666;
  line-height: 1;
  margin-top: 34rpx;
}

.car-img {
  width: 258rpx;
  height: 117rpx;
}

/* 卡片通用样式 */
.info-card, .bill-card {
  margin: 0rpx 27rpx 20rpx 27rpx;
  padding: 40rpx;
  background-color: #ffffff;
  border-radius: 14rpx;
  z-index: 2;
  position: relative;
}

.card-item {
  margin-bottom: 46rpx;
}

.card-item:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: 400;
  font-size: 25rpx;
  color: #999999;
  line-height: 1;
  margin-bottom: 31rpx;
}

.plate-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  line-height: 1;
}

.plate-number {
  font-weight: 600;
  font-size: 38rpx;
  color: #272727;
}

.status-tag {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.status-icon {
  width: 27rpx;
  height: 27rpx;
}

.status-text {
  font-weight: 400;
  font-size: 25rpx;
  color: #EF8E1A;
  line-height: 1;
}

.value {
  font-weight: 400;
  font-size: 28rpx;
  color: #444444;
  line-height: 1;
}

/* 账单概览 */
.bill-header {
  display: flex;
  align-items: center;
  gap: 15rpx;
  margin-bottom: 47rpx;
}

.mark {
  width: 6rpx;
  height: 26rpx;
  background: #F3921E;
  border-radius: 3rpx;
}

.bill-title {
  font-weight: 600;
  font-size: 28rpx;
  color: #444444;
}

.bill-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 47rpx;
}

.bill-item:last-child {
  margin-bottom: 0;
}


.bill-label{
	font-weight: 400;
	font-size: 28rpx;
	color: #999999;
}

.bill-value{
	font-weight: 400;
	font-size: 28rpx;
	color: #333333;
}

.bill-price {
  font-weight: 600;
  font-size: 28rpx;
  color: #1D1D1D;
}

/* 底部支付栏 */
.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0rpx 40rpx;
  height: 145rpx;
  background: #FFFFFF;
}

.total {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}

.total-label {
  font-weight: 400;
  font-size: 28rpx;
  color: #666666;
}

.total-price {
  font-weight: 600;
  font-size: 40rpx;
  color: #F26315;
}

.pay-btn {
  width: 353rpx;
  height: 89rpx;
  background: #405EF4;
  border-radius: 14rpx;
  font-weight: 400;
  font-size: 28rpx;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>