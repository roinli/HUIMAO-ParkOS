<template>
  <view class="carModel">
  
	<view class="box">
	  <view class="carModel">
	    <view class="carModel_top">
	      <text style="color: #FF0404;">*</text>
	      车牌号码
	    </view>
	    <view class="box_top_tip">添加车辆缴费免排队、免找零</view>
	
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
	
	  <view class="agree-box">
	    <checkbox-group @change="onDefaultChange">
	      <label class="check-item">
	        <checkbox
	          value="1"
	          color="#405EF4"
	          :checked="isDefault"
	        />
	        <text class="check-item-tlte">同时设置为默认车辆</text>
	      </label>
	    </checkbox-group>
	  </view>
	
	  <view class="bttom">
	    <view @click="addBut" class="addButx">
	      <text style="margin-right: 5px;">+</text>
	      添加车辆
	    </view>
	  </view>
	</view>
	
	<u-keyboard :overlay='false' :show="show" mode="car" :tooltip="true" mask="true" @change="onChange" @cancel="onCancel"
	  @confirm="onConfirm" @backspace="onBackspace"></u-keyboard>
  
  </view>
</template>



<script>
import { IMG_URL } from '@/env.js'

export default {
  data() {
    return {
      IMG_URL,
      show: false,
      currentLicensePlateIndex: 0,
      licensePlateValue: ['', '', '', '', '', '', '', ''],
      timer: null,
      isDefault: false,
    }
  },
  onUnload() {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  },
  methods: {
    // 点击车牌区域 → 自动打开键盘并聚焦第一个
    openKeyboardAutoFirst() {
      let firstEmpty = this.licensePlateValue.findIndex(item => item === '')
      if (firstEmpty === -1) firstEmpty = 0
      this.currentLicensePlateIndex = firstEmpty
      this.show = true
    },
    addBut() {
      let platNo = this.licensePlateValue.join('')
	  
	  let token = uni.getStorageSync('token');
	  if(!token){
	  	 uni.navigateTo({
	  	 	url: '/pages/index/login/login',
	  	 })
	  }
	  
	  this.$emit('addCar', platNo);
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
	  
	  this.emitValue();
    },
    chooseLicensePlateNumber(index) {
      this.currentLicensePlateIndex = index
      this.show = true
    },
    onChange(e) {
      // 修复：第一位必须是汉字省份，不能输字母
      if (this.currentLicensePlateIndex == 0) {
        if (/[a-zA-Z]/.test(e)) {
          uni.showToast({
            title: '请输入省份简称',
            icon: 'none'
          })
          return
        }
      }
	  
      // 其他位校验
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
	  
	  this.emitValue();
    },
    onCancel() {
      this.show = false
    },
    onConfirm() {
      this.show = false
    },
    onDefaultChange(e) {
      this.isDefault = e.detail.value.length > 0
    },
	// 向父组件抛出车牌值
	emitValue() {
	  const plateNumber = this.licensePlateValue.join('');
	  this.$emit('input', plateNumber);
	  this.$emit('change', plateNumber);
	},
	// 清空车牌（外部可调用）
	clear() {
	  this.licensePlateValue = ['', '', '', '', '', '', '', ''];
	  this.currentLicensePlateIndex = 0;
	  this.emitValue();
	},
		
		
  }
}
</script>



<style scoped lang="scss">
.bttom {
  margin-top: 20px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;

  .cancel {
    font-weight: 400;
    font-size: 28rpx;
    color: #999999;
    line-height: 1;
    margin-top: 63rpx;
    text-align: center;
  }

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
  margin-top: 26rpx;
}

.box {
  width: 708rpx;
  height: 525rpx;
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
  font-size: 40rpx;
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

  /* 当前输入高亮 */
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

.agree-box {
  margin: 45rpx 0rpx 45rpx 0rpx;
  font-weight: 400;
  font-size: 25rpx;
  color: #999999;
  line-height: 1;

  checkbox {
    transform: scale(0.6);
  }
}
</style>