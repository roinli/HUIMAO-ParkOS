<template>
  <view class="record-page">
	  
	<z-paging ref="paging"
		autowire-list-name="orderList" 
		autowire-query-name="zQuery"
		:show-scrollbar="false"
		:auto-clean-list-when-reload="false"
		:auto-scroll-to-top-when-reload="false"
		:safe-area-inset-bottom="true"
		:auto="true"
		:use-page-scroll="true"
		>
	  
    <!-- 订单列表 -->
    <view class="order-list">
      <view class="order-item" v-for="(item, index) in orderList" :key="index">
        <!-- 订单头部 -->
        <view class="order-header">
          <view class="park-info">
			<image :src="IMG_URL + '/location.png'" class="location-icon"></image>
            <text class="park-name">{{ item.lotName }}</text>
          </view>
          <text class="fee">¥{{ item.totalPayableAmount.toFixed(2) }}</text>
        </view>

        <view class="divider"></view>

        <!-- 订单详情 -->
        <view class="order-details">
          <view class="detail-row space-between">
			<view class="detail-left">
				  <text class="label">停车车辆：</text>
				  <text class="value">{{ item.plateNo }}</text>
			</view>
			<!--WAIT_PAY， COMPLETED， IN_PARK， PAID_PENDING_EXIT-->
            <text class="status paid"  v-if="['PAID_PENDING_EXIT', 'COMPLETED'].includes(item.status)">已支付</text>
			<text class="status pending"  v-else>待支付</text>
          </view>
          <view class="detail-row">
            <text class="label">入场时间：</text>
            <text class="value">{{ item.entryTime }}</text>
          </view>
          <view class="detail-row">
            <text class="label">出场时间：</text>
            <text class="value">{{ item.leaveDeadlineTime || '待出场' }}</text>
          </view>
          <view class="detail-row">
            <text class="label">停车时长：</text>
            <text class="value">{{ formatTime(item.parkingMinutes) || '--' }}</text>
          </view>
        </view>

        <!-- 待支付按钮 -->
		<view class="bottom-btn-box">
			<view
			  v-if="['WAIT_PAY', 'IN_PARK'].includes(item.status)"
			  class="pay-btn" 
			  @tap="handlePay(item)"
			>
			  立即支付
			</view>
		</view>
		
      </view>
    </view>
	
	</z-paging>
	
  </view>
</template>

<script>
import { IMG_URL } from '@/env.js'
import { getPaymentRecords, pay, getSessions } from "@/config/api.js";

export default {
  data() {
    return {
	  IMG_URL,
      orderList: []
    }
  },
  methods: {
    handlePay(order) {
	  console.log("order", order);
	  
	  pay({params: {
	  	orderNo: order.currentOrderNo
	  }}).then((res) => {
	  	if (res.code == 200) {
	  		uni.redirectTo({
	  			url: '/pages/sub-packages/paySuccess?orderNo='+order.currentOrderNo,
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
	zQuery(pageNo, pageSize) {
		let _this = this;
		
		let request =  {
			pageNum: pageNo,
			pageSize: pageSize,
			plateNo: uni.getStorageSync('plateNo'),
		}
		
		getSessions({params: request}).then((res) => {
			if (res.code == 200) {
				this.$refs.paging.complete(res.data);
			} 
		})
		.catch(err => {
			this.$refs.paging.complete(false);
		});
	},
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
	
	
	
  },
  
  
}
</script>

<style scoped>
.record-page {
  background-color: #F8F8F8;
  min-height: 100vh;
  padding: 20rpx;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.order-item {
  padding: 30rpx 30rpx 40rpx 30rpx;
  background: #FFFFFF;
  border-radius: 14rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 29rpx;
  line-height: 1;
}

.park-info {
  display: flex;
  align-items: center;
  gap: 25rpx;
}

.location-icon {
  width: 22rpx;
  height: 29rpx;
}

.park-name {
 font-weight: 600;
 font-size: 31rpx;
 color: #333333;
}

.fee {
	font-weight: 600;
	font-size: 31rpx;
	color: #353535;
	line-height: 1;
}

.divider {
  width: 100%;
  height: 1rpx;
  background-color: #ECECEC;
  margin-bottom: 30rpx;
}

.order-details {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.detail-row {
  display: flex;
  align-items: center;
}

.detail-left{
	display: flex;
	align-items: center;
}

.space-between{
	justify-content: space-between;
}

.label {
  font-weight: 400;
  font-size: 28rpx;
  color: #999999;
}

.value {
  font-weight: 400;
  font-size: 28rpx;
  color: #333333;
}

.status {
	font-weight: 400;
	font-size: 28rpx;
}

.status.pending {
  color: #F39844;
}

.status.paid {
  color: #2CC542;
}

.bottom-btn-box{
	display: flex;
	justify-content: flex-end;
}

.pay-btn {
	width: 185rpx;
	height: 69rpx;
	background: #405EF4;
	border-radius: 10rpx;
	font-weight: 400;
	font-size: 28rpx;
	color: #FFFFFF;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-top: 16rpx;
}
</style>