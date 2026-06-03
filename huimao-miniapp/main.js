import App from './App'

// #ifndef VUE3
import Vue from 'vue'
Vue.config.productionTip = false
App.mpType = 'app'
import uView from "uview-ui";
import { mixin } from "./utils/mixin.js";
import tools from '@/utils/tools'

Vue.prototype.$tools = tools;

import './common.scss'

Vue.mixin(mixin);
Vue.use(uView);
// 如此配置即可
const app = new Vue({
    ...App
})
require('config/request.js')(app)
console.log("request已注册")
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
export function createApp() {
  const app = createSSRApp(App)
  return {
    app
  }
}
// #endif