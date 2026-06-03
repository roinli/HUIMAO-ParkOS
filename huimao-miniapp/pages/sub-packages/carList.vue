<template>
  <view class="vehicle-page">
 
    <!-- 车辆列表 -->
    <view class="vehicle-list">
      <view class="vehicle-item" v-for="(item, index) in vehicleList" :key="index">
        <!-- 车辆信息区 -->
        <view class="info-wrap">
          <view class="info-left">
            <text class="plate">{{ item.plateNo }}</text>
            <text class="type">{{ item.type }}</text>
            <text class="time">时间：{{ item.createTime }}</text>
          </view>
        </view>

        <!-- 操作区 -->
        <view class="action-wrap">
          <view class="default-wrap" @tap="toggleDefault(index)">
			<image :src="IMG_URL + '/checked.png'" class="check-img" v-if="item.isDefault"></image>
			<image :src="IMG_URL + '/no-check.png'" class="check-img" v-else="item.isDefault"></image>
            <text class="default-text">设为默认车辆</text>
          </view>
          <view class="delete-wrap" @tap="handleDelete(index)">
           <image :src="IMG_URL + '/delete.png'" class="delete-img"></image>
            <text class="delete-text">删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 添加车辆按钮 -->
    <view class="add-btn-wrap">
      <button class="add-btn" @tap="goToAdd">+ 添加车辆</button>
    </view>
	
  </view>
</template>

<script>
import { IMG_URL } from '@/env.js'
	
export default {
  data() {
    return {
	  IMG_URL,
      vehicleList: [
        {
          plateNo: '川A234121',
          type: '新能源',
          createTime: '2026-04-18 12:12:12',
          isDefault: true
        },
        {
          plateNo: '川A134345',
          type: '新能源',
          createTime: '2026-04-16 13:12:12',
          isDefault: false
        },
		{
		  plateNo: '川A678456',
		  type: '新能源',
		  createTime: '2026-04-21 16:12:00',
		  isDefault: false
		}
      ]
    }
  },
  methods: {
    goBack() {
      uni.navigateBack()
    },
    // 切换默认车辆
    toggleDefault(index) {
      // 所有车辆取消默认
      this.vehicleList.forEach((item, i) => {
        this.$set(this.vehicleList[i], 'isDefault', false)
      })
      // 当前设为默认
      this.$set(this.vehicleList[index], 'isDefault', true)
      uni.showToast({ title: '已设为默认车辆', icon: 'none' })
    },
    // 删除车辆
    handleDelete(index) {
      uni.showModal({
        title: '提示',
        content: '确定要删除该车辆吗？',
        success: (res) => {
          if (res.confirm) {
            this.vehicleList.splice(index, 1)
            uni.showToast({ title: '删除成功', icon: 'none' })
          }
        }
      })
    },
    goToAdd() {
      uni.navigateTo({ url: '/pages/sub-packages/carBind' })
    }
  }
}
</script>

<style scoped lang="scss">
.vehicle-page {
  min-height: 100vh;
  background-color: #F8F8F8;
  padding-bottom: 40rpx;
}

.vehicle-list {
  padding: 20rpx;
}

.vehicle-item {
  background-color: #FFFFFF;
  border-radius: 14rpx;
  margin-bottom: 20rpx;
  overflow: hidden;
}

.info-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 50rpx 0rpx 60rpx 50rpx;
  box-sizing: border-box;
}

.info-left {
  display: flex;
  flex-direction: column;
  gap: 25rpx;
}

.plate {
  font-weight: 600;
  font-size: 35rpx;
  color: #333333;
  line-height: 1;
}

.type {
  font-weight: 400;
  font-size: 28rpx;
  color: #999999;
  line-height: 1;
}

.time {
 font-weight: 400;
 font-size: 28rpx;
 color: #999999;
 line-height: 1;
}

.car-img {
  width: 237rpx;
  height: 126rpx;
}

.action-wrap {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 68rpx;
  padding: 31rpx 42rpx 36rpx 0rpx;
  border-top: 1rpx solid #DDDDDD;
}

.default-wrap {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.check-img {
  width: 32rpx;
  height: 32rpx;
}

.default-text {
  font-weight: 400;
  font-size: 25rpx;
  color: #999999;
}

.delete-wrap {
  display: flex;
  align-items: center;
  gap: 15rpx;
}

.delete-img {
  width: 31rpx;
  height: 31rpx;
}

.delete-text {
  font-weight: 400;
  font-size: 25rpx;
  color: #999999;
}

.add-btn-wrap {
   position: fixed;
   bottom: 113rpx;
   left: 0rpx;
   display: flex;
   align-items: center;
   justify-content: center;
   width: 100%;
}

.add-btn {
  width: 640rpx;
  height: 95rpx;
  background: #405EF4;
  border-radius: 48rpx;
  font-weight: 400;
  font-size: 28rpx;
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>