<template>
	<view>
		<view id='tabbar-container' class="tabbar-container" :class="isIpx ? 'IpxBot' : ''">
			<view class="tabbar-item" v-for="(item, index) in tabList" :class="[item.centerItem ? 'center-item' : '']"
				@click="changeItem(item)" :key="index">
				<view class="item-top" :style="{ padding: item.id == 2 ? 0 : '8rpx' }">
					<image :style="{
						width: item.imgWidth,
						height: item.imgHeight
					}" :src="tabId == item.id ? item.selectIcon : item.icon" v-if="!item.centerItem" mode="heightFix">
					</image>
					<view class="item-top_center" v-else>
						<image :src="require('./menu/scan.png')" mode=""></image>
					</view>
				</view>
				<view class="item-bottom" :class="[tabId == item.id ? 'item-active' : '']">
					<text>{{ item.text }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getLotInfo } from "@/config/api.js";
import { scanCode } from '@/utils/plat-api'
	
export default {
	props: {
		currentPage: {
			type: Number,
			default: 0,
		},
	},
	data() {
		return {
			isIpx: false,
			tabId: 0,
			tabList: [{
					id: 1,
					path: "/pages/index/index",
					icon: require("./menu/tab1-2.png"),
					selectIcon: require("./menu/tab1-1.png"),
					text: "首页",
					imgWidth: '42rpx',
					imgHeight: '38rpx'
				}, {
					id: 2,
					text: "",
					imgWidth: '38rpx',
					imgHeight: '38rpx',
					centerItem: true,
				},
				{
					id: 3,
					path: "/pages/index/user",
					icon: require("./menu/tab4-2.png"),
					selectIcon: require("./menu/tab4-1.png"),
					text: "我的",
					imgWidth: '34rpx',
					imgHeight: '38rpx'
				},
			],
		};
	},
	mounted() {
		this.tabId = this.currentPage;
		uni.hideTabBar();
	},
	created() {
		let that = this;
		uni.getSystemInfo({
			success: function(res) {
				if (res.model.indexOf("iPhone") !== -1) {
					that.isIpx = true;
				}
			},
		});
	},
	methods: {
		changeItem(item) {
			if (item.id == 2) {
				
				// #ifndef H5
				uni.scanCode({
					success: (res) => {
						console.log("scanCode", res.result);
						if (res.result.indexOf('lotCode') != -1) {
							var lotCode = this.getQueryString(res.result, 'lotCode');
							lotCode = lotCode.trim();
							
							getLotInfo({params: {
								lotCode: lotCode
							}}).then((res) => {
								if (res.code == 200) {
									uni.navigateTo({
										url:'/pages/sub-packages/scan?lotCode='+ lotCode,
									})
								} else{
									uni.showToast({
										title: res.msg,
										icon:'none',
									})
								}
							})
							.catch(err => {
								
							});
							
							
						} else {
							uni.showToast({
								title: '二维码错误！',
								icon: 'error',
							})
						}
					}
				});
				// #endif
				
				// #ifdef H5
				scanCode().then(res => {
					console.log("scanCode", res.result);
					if (res.result.indexOf('lotCode') != -1) {
						var lotCode = this.getQueryString(res.result, 'lotCode');
						lotCode = lotCode.trim();
						
						getLotInfo({params: {
							lotCode: lotCode
						}}).then((res) => {
							if (res.code == 200) {
								uni.navigateTo({
									url:'/pages/sub-packages/scan?lotCode='+ lotCode,
								})
							} else{
								uni.showToast({
									title: res.msg,
									icon:'none',
								})
							}
						})
						.catch(err => {
							
						});
						
					} else {
						uni.showToast({
							title: '二维码错误！',
							icon: 'error',
						})
					}
					
				}).catch(err => {
				 
				})
				// #endif
				
			}else{
				uni.switchTab({
					url: item.path,
				});
			}
		},
		getQueryString(url, name) {
			name = name.replace(/[\\[\\]]/g, '\\\\$&');
			var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)');
			var results = regex.exec(url);
			if (!results) return '';
			if (!results[2]) return '';
			return decodeURIComponent(results[2].replace(/\\+/g, ' '));
		}
		
	},
};
</script>

<style scoped lang="scss">
view {
	padding: 0;
	margin: 0;
	box-sizing: border-box;
}

.tabbar-container {
	position: fixed;
	bottom: 0;
	left: 0;
	width: 100%;
	box-shadow: 0px 0px 3px 0px rgba(232, 232, 232, 0.65);
	display: flex;
	align-items: center;
	font-size: 26rpx;
	color: #666666;
	background-color: #ffffff;
	z-index: 999;
}

.tabbar-container .tabbar-item {
	flex: 1;
	height: 112rpx;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	text-align: center;
}

.tabbar-container .item-active {
	color: #5A3907;
}

.tabbar-container .center-item .item-top .item-top_center {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 111rpx;
	height: 111rpx;
	background: linear-gradient(-50deg, #3F5CF2, #2B47F0);
	border-radius: 50%;
	margin-bottom: 48px;

	image {
		width: 56rpx;
		height: 56rpx;
	}
}

.tabbar-container .tabbar-item .item-top {
	padding: 8rpx;
}

.tabbar-container .tabbar-item .item-bottom {
	font-size: 21rpx;
}

/* 适配iPhone X */
.IpxBot {
	padding-bottom: 20rpx !important;
}
</style>