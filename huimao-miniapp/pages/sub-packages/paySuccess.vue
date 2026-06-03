<template>
  <view class="payment-success">
 
	<image :src="IMG_URL + '/pay-success.png'" class="pay-success-img"></image>
	
    <!-- 文字信息 -->
    <view class="title">支付成功</view>
    <view class="bill">账单号：{{ orderNo }}</view>

    <!-- 按钮 -->
    <button class="btn" @tap="handleViewBill">查看账单</button>
	
  </view>
</template>

<script>
import { IMG_URL } from '@/env.js'
	
export default {
  data() {
    return {
		IMG_URL,
        orderNo: ''
    }
  },
  onLoad(options){
	  this.orderNo = options.orderNo;
  },
  methods: {
    handleViewBill() {
      uni.reLaunch({
        url: '/pages/sub-packages/parkingRecord'
      })
    }
  }
}
</script>

<style scoped>
.payment-success {
  min-height: 100vh;
  background-color: #F8F8F8;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 154rpx;
  box-sizing: border-box;
}

.pay-success-img{
	width: 153rpx;
	height: 131rpx;
}

.title {
  font-weight: 600;
  font-size: 35rpx;
  color: #212121;
  margin-bottom: 43rpx;
  margin-top: 54rpx;
}

.bill {
  font-weight: 400;
  font-size: 28rpx;
  color: #666666;
  margin-bottom: 125rpx;
}

.btn {
  width: 247rpx;
  height: 60rpx;
  background: #405EF4;
  border-radius: 30rpx;
  font-weight: 400;
  font-size: 28rpx;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 60rpx;
}
</style>