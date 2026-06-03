export const mixin = {
  data() {
    return {
      share: {
        title: "割胶机",
        path: "",
        imageUrl: "",
      },
    };
  },
  created() {
	  // 在组件创建后，动态设置分享标题
	  const platformTitle = uni.getStorageSync("platformTitle");
	  if (platformTitle) {
		this.share.title = platformTitle;
	  }
	},
  // 分享到微信
  onShareAppMessage: function () {
    // 获取加载的页面
    let pages = getCurrentPages(),
      view = pages[pages.length - 1];
    //分享的页面路径
    if (!this.share.path) {
      this.share.path = view.$page.fullPath;
    }
    //转发参数
    return this.share;
  },
  // 分享到朋友圈
  onShareTimeline() {
    // 获取加载的页面
    let pages = getCurrentPages(),
      view = pages[pages.length - 1];
    //分享的页面路径
    if (!this.share.path) {
      this.share.path = view.$page.fullPath;
    }
    //转发参数
    return this.share;
  },
};
