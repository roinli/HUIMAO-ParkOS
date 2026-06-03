<template>
  <view class="container">
    <view class="parking-top">
      <view class="parking-status">
        <image :src="IMG_URL + '/map-grep.png'" class="map-img"></image>
        <view class="parking-label">当前位置</view>
      </view>
      <view class="parking-tips">{{lotInfo.lotName || ''}}</view>
    </view>

    <view class="box">
      <view class="carModel">
        <view class="box_top_tip">输入车牌号查询停车订单</view>
        <view class="carModel_bottom">
          <view class="car-value">
            <block v-for="(item, index) in 8" :key="index">
              <view 
                class="car-input" 
                :class="{
                  'filled': licensePlateValue[index] !== '',
                  'active': currentLicensePlateIndex === index 
                }"
                @tap="chooseLicensePlateNumber(index)" 
              >
                <text v-if="index === 7" class="car-text">
                  {{ licensePlateValue[index] || '新能源' }}
                </text>
                <text v-else class="car-text">
                  {{ licensePlateValue[index] }}
                </text>
              </view>
            </block>
          </view>
        </view>
      </view>

      <view class="bttom">
        <view @click="addBut" class="addButx">查询</view>
      </view>
    </view>

    <!-- 新增：我的车辆列表 -->
    <view class="car-list-section">
      <view class="section-title">我的车辆（已绑定{{ carList.length }}辆）</view>
      <view class="car-list">
        <view 
          class="car-item" 
          :class="{ selected: selectedCarIndex === index }"
          @tap="selectCar(index)"
          v-for="(car, index) in carList" 
          :key="index"
        >
          <text class="plate-text">{{ car.plateNo }}</text>
          <view class="radio-wrap">
			  <image :src="IMG_URL + '/checked.png'" class="check-img" v-if="selectedCarIndex === index"></image>
			  <image :src="IMG_URL + '/no-check.png'" class="check-img" v-else></image>
          </view>
        </view>
      </view>
    </view>

    <u-keyboard 
      :overlay='false' 
      :show="show" 
      mode="car" 
      :tooltip="true" 
      mask="true" 
      @change="onChange" 
      @cancel="onCancel"
      @confirm="onConfirm" 
      @backspace="onBackspace"
    ></u-keyboard>
  </view>
</template>

<script>
import { IMG_URL } from '@/env.js'
import { getLotInfo, getWaitpayOrder } from "@/config/api.js";

export default {
  data() {
    return {
      IMG_URL,
      show: false,
      currentLicensePlateIndex: 0,
      licensePlateValue: ['', '', '', '', '', '', '', ''],
      timer: null,
      isDefault: false,
      // 车辆列表和选中状态
      carList: [
        { plateNo: '川A234121' },
        { plateNo: '川A134345' },
        { plateNo: '川A678456' }
      ],
      selectedCarIndex: -1,
	  lotCode:'',
	  lotInfo:{},
    }
  },
  onLoad(options){
	  this.lotCode = options.lotCode;
	  this.getLotInfo();
  },
  onUnload() {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  },
  methods: {
	getLotInfo(){
		getLotInfo({params: {
			lotCode: this.lotCode
		}}).then((res) => {
			if (res.code == 200) {
				this.lotInfo = res.data
			} else{
				uni.showToast({
					title: res.msg,
					icon:'none',
				})
			}
		})
		.catch(err => {
			
		});
	},
    // 点击下方车辆，同步到上方输入框
    selectCar(index) {
      // 单选逻辑：再次点击取消选中
      if (this.selectedCarIndex === index) {
        this.selectedCarIndex = -1
        this.licensePlateValue = ['', '', '', '', '', '', '', '']
        return
      }

      this.selectedCarIndex = index
      const plateNo = this.carList[index].plateNo
      // 把车牌拆分成8个字符，同步到输入框
      const chars = plateNo.split('')
      this.licensePlateValue = []
      for (let i = 0; i < 8; i++) {
        this.licensePlateValue.push(chars[i] || '')
      }
      // 自动聚焦到最后一个空位置
      this.currentLicensePlateIndex = this.licensePlateValue.findIndex(c => c === '')
      if (this.currentLicensePlateIndex === -1) {
        this.currentLicensePlateIndex = 7
      }
    },

    addBut() {
        let plateNo = this.licensePlateValue.join('')
		if(!plateNo){
			uni.showToast({
				title: '请输入车牌',
				icon:'none',
			})
			return false;
		}
		
		if(!this.lotInfo.lotId){
			uni.showToast({
				title: '停车场不存在',
				icon:'none',
			})
			return false;
		}
		
		getWaitpayOrder({params: {
			plateNo: plateNo,
			lotId: this.lotInfo.lotId,
		}}).then((res) => {
			if (res.code == 200) {
				//PAID, UNPAID
				if(res.data && res.data.orderStatus == 'UNPAID'){
					uni.navigateTo({
						url:'/pages/sub-packages/scanOrder?lotId='+ this.lotInfo.lotId + '&plateNo=' + plateNo+'&lotCode='+this.lotCode,
					})
				}else if(res.data && res.data.orderStatus == 'PAID'){
					uni.showToast({
						title: '支付成功，请及时驶离',
						icon:'none',
					})
				}
				
			} else{
				uni.showToast({
					title: res.msg,
					icon:'none',
				})
			}
			
		  })
		  .catch(err => {
			
		  });
    },
    goback() {
      uni.navigateBack()
    },
    onBackspace() {
      let licensePlateIndex = this.currentLicensePlateIndex > 7 ? 7 : this.currentLicensePlateIndex
      if (this.licensePlateValue[licensePlateIndex] !== '') {
        this.$set(this.licensePlateValue, licensePlateIndex, '')
      } else {
        licensePlateIndex = licensePlateIndex < 1 ? 0 : --licensePlateIndex
        this.$set(this.licensePlateValue, licensePlateIndex, '')
      }
      this.currentLicensePlateIndex = licensePlateIndex
    },
    chooseLicensePlateNumber(index) {
      this.currentLicensePlateIndex = index
      this.show = true
    },
    onChange(e) {
      if (this.currentLicensePlateIndex == 0) {
        if (/[a-zA-Z]/.test(e)) {
          uni.showToast({
            title: '请输入省份简称',
            icon: 'none'
          })
          return
        }
      }
      if (this.currentLicensePlateIndex != 0) {
        if (!/[0-9a-zA-Z]/.test(e)) {
          uni.showToast({
            title: '请输入正确的车牌号码',
            icon: 'none'
          })
          return
        }
      }

      this.$set(this.licensePlateValue, this.currentLicensePlateIndex, e)
      this.currentLicensePlateIndex++
      if (this.currentLicensePlateIndex === 8) {
        this.show = false
      }
    },
    onCancel() {
      this.show = false
    },
    onConfirm() {
      this.show = false
    },
    goBack() {
      uni.navigateBack();
    },
    onDefaultChange(e) {
      this.isDefault = e.detail.value.length > 0
    }
  }
}
</script>

<style scoped lang="scss">
.container {
  width: 100%;
  min-height: 100vh;
  background-color: #F8F8F8;
  padding-bottom: 40rpx;
}

.parking-top{
  padding-top: 52rpx;
  padding-left: 31rpx;
  margin-bottom: 51rpx;
  
  .parking-status{
    display: flex;
    align-content: center;
	padding-left: 4rpx;
  }
  
  .map-img{
    width: 21rpx;
    height: 31rpx;
  }
  
  .parking-label{
    font-weight: 400;
    font-size: 28rpx;
    color: #999999;
    line-height: 1;
    margin-left: 20rpx;
  }
  
  .parking-tips{
    font-weight: 600;
    font-size: 38rpx;
    color: #333333;
    line-height: 1;
    margin-top: 34rpx;
  }
}

.bttom {
  margin-top: 73rpx;
  display: flex;
  align-items: center;
  justify-content: center;
 
  .addButx {
    width: 340rpx;
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
}

.box_top_tip {
  font-weight: 400;
  font-size: 25rpx;
  color: #999999;
  line-height: 1.8;
}

.box {
  width: 708rpx;
  height: 409rpx;
  background: #FFFFFF;
  border-radius: 14rpx;
  margin: 0rpx 20rpx;
  overflow: hidden;
  z-index: 999;
  position: relative;
  box-sizing: border-box;
  padding: 34rpx 32rpx;
}

.carModel_bottom {
  margin-top: 38rpx;
}

.carModel_top {
  font-weight: 600;
  font-size: 31rpx;
  color: #333333;
  line-height: 1;
}

.car-value {
  display: flex;
  margin: 0 auto;

  .car-input {
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 64rpx;
    height: 79rpx;
    border-radius: 4rpx;
    text-align: center;
    font-size: 38rpx;
    line-height: 1;
    margin-left: 18rpx;
    background-color: #F8F9FB;
    border: 4rpx solid transparent;
    transition: all 0.2s;
    box-sizing: border-box;
  }

  .car-input.active {
    border-color: #405EF4 !important;
  }

  .car-input:last-child {
    background: #EFFDFB;
    border: 1rpx solid #16E2A1;
  }

  .car-input:last-child .car-text {
    font-weight: 400;
    font-size: 17rpx;
    color: #141E34;
  }

  .car-input:last-child.filled .car-text {
    font-size: 38rpx !important;
  }
}

/* 新增：车辆列表样式 */
.car-list-section {
  margin-top: 54rpx;
  padding: 0 20rpx;

  .section-title {
    font-weight: 600;
    font-size: 28rpx;
    color: #333333;
    margin-bottom: 23rpx;
	padding-left: 12rpx;
  }

  .car-list {
    .car-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0rpx 36rpx;
      background: #FFFFFF;
      border-radius: 14rpx;
      margin-bottom: 24rpx;
      border: 1rpx solid transparent;
      transition: all 0.2s;
	  box-sizing: border-box;
	  height: 150rpx;

      &.selected {
        background: #E5ECF9;
        border: 1px solid #405EF4;
      }

      .plate-text {
        font-weight: 600;
        font-size: 32rpx;
        color: #333333;
      }
	  
	  .check-img{
		  width: 29rpx;
		  height: 29rpx;
	  }
      
    }
  }
}
</style>