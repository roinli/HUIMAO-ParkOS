<script>
export default {
  data() {
    return {
      showPrivacy: false
    };
  },

  onLaunch: function () {
    // 启动时检查登录
    //this.checkLogin();
  },
  onShow: function () {
    //this.checkLogin();
  },
  onHide: function () {},

  methods: {
    // 登录拦截核心方法
    checkLogin() {
      const token = uni.getStorageSync('token');
	  console.log("token", token);

      // 获取当前页面路由
      const pages = getCurrentPages();
      const currentPage = pages[pages.length - 1];
      const url = currentPage ? currentPage.route : '';
	  console.log("url", url);

      // 不需要登录的白名单（登录页、注册页）
      const whiteList = ['pages/login/login'];

      // 如果不在白名单 且 没有token → 跳登录
      if (!whiteList.includes(url) && !token) {
        uni.reLaunch({
          url: '/pages/index/login/login'
        });
      }
	  
    }
  }
};
</script>

<style lang="scss">
@import "uview-ui/index.scss";

image {
  height: auto;
  width: auto;
}
/*  #ifdef MP-WEIXIN  */
[v-cloak] {
  display: none;
}
/*  #endif  */
</style>