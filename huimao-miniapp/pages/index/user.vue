<template>
  <view class="mine-page">
	<u-navbar
	    back-text=""
	    title="我的"
	    :border="false"
	    bg-color="transparent"
	    placeholder
	    :leftIcon="false" 
	>
	   <view class="u-nav-slot" slot="left"></view>
	</u-navbar>
	  
	  
    <!-- 顶部用户信息区 -->
    <view class="user-header">
	   <image :src="IMG_URL + '/default-user.png'" class="default-user-img"></image>
      <view class="user-info">
        <text class="nickname">云舟</text>
        <text class="phone">13500009456</text>
      </view>
    </view>

    <!-- 功能菜单列表 -->
    <view class="menu-list">
      <view class="menu-item" @tap="goToMyCar">
        <view class="item-left">
          <view class="icon-wrap">
            <image :src="IMG_URL + '/my-icon1.png'" class="menu-icon1"></image>
          </view>
          <text class="menu-text">我的车辆</text>
        </view>
        <view class="item-right">
          <text class="badge-text">3辆</text>
          <image :src="IMG_URL + '/right.png'" class="arrow"></image>
        </view>
      </view>

      <view class="menu-item" @tap="goToRecord">
        <view class="item-left">
          <view class="icon-wrap">
            <image :src="IMG_URL + '/my-icon2.png'" class="menu-icon2"></image>
          </view>
          <text class="menu-text">停车记录</text>
        </view>
        <view class="item-right">
          <view class="badge-wrap" v-if="pendingCount > 0">
            <text class="badge-text">待支付</text>
            <view class="badge-num">{{ pendingCount }}</view>
          </view>
          <image :src="IMG_URL + '/right.png'" class="arrow"></image>
        </view>
      </view>

      <view class="menu-item" @tap="goToService">
        <view class="item-left">
          <view class="icon-wrap">
             <image :src="IMG_URL + '/my-icon3.png'" class="menu-icon3"></image>
          </view>
          <text class="menu-text">客服中心</text>
        </view>
        <view class="item-right">
			<image :src="IMG_URL + '/right.png'" class="arrow"></image>
        </view>
      </view>

      <view class="menu-item" @tap="goToAbout">
        <view class="item-left">
          <view class="icon-wrap">
            <image :src="IMG_URL + '/my-icon4.png'" class="menu-icon4"></image>
          </view>
          <text class="menu-text">关于我们</text>
        </view>
        <view class="item-right">
          <image :src="IMG_URL + '/right.png'" class="arrow"></image>
        </view>
      </view>

      <view class="menu-item logout" @tap="handleLogout">
        <view class="item-left">
          <view class="icon-wrap">
             <image :src="IMG_URL + '/my-icon5.png'" class="menu-icon5"></image>
          </view>
          <text class="menu-text">退出登录</text>
        </view>
      </view>
    </view>
	
	
	<Tabbar :currentPage="3" />
	
  </view>
</template>


<script>
import Tabbar from "../../components/tabbar/tabbar.vue";
import { IMG_URL } from '@/env.js'
import { getOrder } from "@/config/api.js";
	
export default {
	data() {
		return {
		  IMG_URL,
		  userInfo:{},
		  pendingCount: 0,
		}
	},	
	components: {
		Tabbar,
	},
	onLoad() {
		
	},
	onShow(){
	  let token = uni.getStorageSync('token');
	  if(token){
		this.getOrder();
	  }
	},
	methods: {
		getOrder(){
			let request = {
				plateNo: uni.getStorageSync('plateNo'),
			};
			
			getOrder({params: request}).then((res) => {
				if (res.code == 200) {
					if(res.data && res.data.orderStatus == 'UNPAID'){
						this.pendingCount = 1
					}
				} 
			})
			.catch(err => {
				
			});
		},
		goToMyCar(){
			uni.navigateTo({
				url:'/pages/sub-packages/carList',
			})
		},
		handleLogout(){
			uni.clearStorageSync();
			uni.navigateTo({
				url: '/pages/index/login/login'
			})
		},
		goToRecord(){
			uni.navigateTo({
				url:'/pages/sub-packages/parkingRecord',
			})
		},
	}
}
</script>

<style scoped lang="scss">
.mine-page {
  width: 100%;
  min-height: 100vh;
  background-color: #F8F8F8;
  // display: flex;
  // flex-direction: column;
  // background-image: url('/static/images/user-bg.png');
  // background-size: cover;
  // background-position: center;
  // background-repeat: no-repeat;
  // background-attachment: fixed;
}

.user-bg-img{
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
}

/* 用户信息头部 */
.user-header {
  display: flex;
  align-items: center;
  padding: 40rpx 30rpx;
  z-index: 2;
  position: relative;

  .default-user-img {
    width: 121rpx;
    height: 121rpx;
    border-radius: 50%;
    margin-right: 36rpx;
  }

  .user-info {
    display: flex;
    flex-direction: column;
    gap: 29rpx;

    .nickname {
      font-weight: 600;
      font-size: 31rpx;
      color: #212121;
	  line-height: 1;
    }

    .phone {
      font-weight: 400;
      font-size: 28rpx;
      color: #666666;
	  line-height: 1;
    }
  }
}

/* 菜单列表 */
.menu-list {
  background: linear-gradient(180deg, #ffffff, #f8f8f8) 60%;
  border-radius: 42rpx 42rpx 0rpx 0rpx;
  overflow: hidden;
  z-index: 2;
  position: relative;

  .menu-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 42rpx 59rpx;

    &:last-child {
      border-bottom: none;
    }

    .item-left {
      display: flex;
      align-items: center;

      .menu-icon1 {
        width: 31rpx;
        height: 27rpx;
      }
	  
	  .menu-icon2 {
	    width: 27rpx;
	    height: 27rpx;
	  }
	  
	  .menu-icon3 {
	    width: 28rpx;
	    height: 27rpx;
	  }
	  
	  .menu-icon4 {
	    width: 29rpx;
	    height: 27rpx;
	  }
	  
	  .menu-icon5 {
	    width: 25rpx;
	    height: 27rpx;
	  }

      .menu-text {
        font-weight: 400;
        font-size: 28rpx;
        color: #393939;
		margin-left: 19rpx;
      }
    }

    .item-right {
      display: flex;
      align-items: center;
      gap: 12rpx;

      .badge-text {
		font-weight: 400;
		font-size: 25rpx;
		color: #999999;
      }

      .badge-wrap {
        display: flex;
        align-items: center;
        gap: 8rpx;

        .badge-num {
		  font-weight: 400;
		  font-size: 24rpx;
		  color: #FFFFFF;
          width: 36rpx;
          height: 36rpx;
		  line-height: 36rpx;
          background: #F47426;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
        }
      }

      .arrow {
        width: 12rpx;
        height: 23rpx;
      }
    }

    &.logout {
      .menu-text {
        color: #333;
      }
    }
  }
}
</style>