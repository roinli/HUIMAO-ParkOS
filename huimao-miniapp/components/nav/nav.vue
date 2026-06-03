<template>
	<view class="nav-box" :style="{'height':height+'px','background':bgColor}">
		<!-- 自定义导航栏 -->
		<view class="status_bar" :style="{'height':statusBarHeight+'px'}">
			<!-- uni-ui这里是状态栏 -->
		</view>
		<!-- 胶囊位置信息 -->
		<!-- justify-center -->
		<view class="nav-main flex align-center "
			:style="{height: navBarHeight+'px',lineHeight:  navBarHeight+'px'}">
			<view class="nav-main-back" @click="back" v-if="backIcon">
				<u-icon name="arrow-left" size="22" :color="backIconColor"></u-icon>
			</view>
			<text class="nav-main-title" :style="{color:titleColor}">{{title}}</text>
		</view>
	</view>
</template>

<script>
	export default {
		props: {
			bgColor: {
				type: String,
				default: "linear-gradient(to bottom, #FEF3EB, #FBF3ED)"
			},
			backIcon: {
				type: Boolean,
				default: true
			},
			backIconColor: {
				type: String,
				default: '#111111'
			},
			titleColor: {
				type: String,
				default: '#fff'
			},
			title: {
				type: String,
				default: "充电桩"
			}
		},
		data() {
			return {
				//总高度
				height: 0,
				//胶囊位置信息
				menuButtonRect: {},
				//状态栏的高度
				statusBarHeight: 0,
				//导航栏的高度
				navBarHeight: 0
			}
		},
		created() {
			// this.height = wx.getStorageSync('navBarHeight')
			this.getHeight();
		},
		methods: {
			//获取屏幕导航栏高度
			getHeight() {
				if (wx.canIUse('getMenuButtonBoundingClientRect')) {
					let sysInfo = wx.getSystemInfoSync(); //状态栏的高度
					this.statusBarHeight = sysInfo.statusBarHeight;
					// 胶囊位置信息
					let rect = wx.getMenuButtonBoundingClientRect();
					this.menuButtonRect = JSON.parse(JSON.stringify(rect));
					// 导航栏高度
					let navBarHeight = (rect.top - sysInfo.statusBarHeight) * 2 + rect.height;
					this.navBarHeight = navBarHeight;
					// 自定义导航栏的高度
					this.height = sysInfo.statusBarHeight + navBarHeight;
				} else {
					wx.showToast({
						title: '您的微信版本过低，界面可能会显示不正常',
						icon: 'none',
						duration: 4000
					});
				}
			},
			//返回
			back() {
				let pages = getCurrentPages(); // 当前页面
				let beforePage = pages.length
				if (beforePage > 1) {
					uni.navigateBack({
						delta: 1
					})
				} else {
					uni.reLaunch({
						url: '/pages/index/index'
					})
				}
			},
		}
	}
</script>

<style lang="scss" scoped>
	.nav-box {
		position: sticky;
		z-index: 9999;
		top: 0;
		left: 0;
		bottom: 0;
		padding: 0 28rpx;
	}

	.status_bar {
		width: 100%;
	}

	.nav-main {
		position: relative;
		display: flex;
		align-items: center;

		.nav-main-back {
			margin-right: 8rpx;
		}

		.nav-main-title {
			font-size: 16px;
		}
	}
</style>