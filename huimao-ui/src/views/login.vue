<template>
  <div class="container-page">
    <div class="container-des">
      <img :src="platformLogo" alt="" class="huige"/>
      <span>{{ title }}</span>

      <ul class="menus">
        <li class="menu">
          <a href="http://huimao.doc.huizhidata.com/" target="_blank">文档</a>
        </li>
 <!--        
        <li class="menu f-pr" v-on:mouseenter="showsub = true" v-on:mouseleave="showsub = false">
          <a href="javascript:" class="f-ib">演示</a>
          <img src="@/assets/images/login/up.png" class="up f-vat f-ib" v-if="showsub" />
          <img src="@/assets/images/login/up.png" class="up1 f-vat f-ib" v-else />
          <div class="submenus" v-show="showsub">
            <div class="submenus-wrapper">
              <section class="submenu">
                <a href="http://182.40.195.136:8180/server/" target="_blank">本地停车</a>
              </section>
              <section class="submenu">
                <a href="http://182.40.195.136:8180/cloud_web/" target="_blank">云端停车</a>
              </section>
            </div>
          </div>
        </li> -->

        <li class="menu">
          <img :src="platformLogo" class="f-ib f-vat hui" />
          <a href="http://wenhui.huizhidata.com" class="f-ib f-vat" target="_blank">官方网站</a>
        </li>
      </ul>
    </div>

    <div class="page-left">
      <div class="content-form">

        <div class="wrap-form" :style="{ width: width }" v-show="operatortype == 'login'">
          <div class="form-title"> {{ title }}</div>

          <div class="form-tabs">账号密码</div>
          <el-form ref="loginForm" :model="loginForm" :rules="loginRules" auto-complete="on" label-position="left">
            <el-form-item prop="username" class="login-form-item">
              <el-input ref="username" v-model="loginForm.username" placeholder="用户名" name="username" type="text"
                tabindex="1" auto-complete="on">
                <img slot="prefix" src="@/assets/images/login/user.png" style="width: 17px;height: 20px; margin-top: 14px" />
              </el-input>
            </el-form-item>

            <el-form-item prop="password" class="login-form-item">
              <el-input :key="passwordType" ref="password" v-model="loginForm.password" :type="passwordType"
                placeholder="密码" name="password" tabindex="2" auto-complete="on" @keyup.enter.native="handleLogin">
                <img slot="prefix" src="@/assets/images/login/pass.png" style="width: 16px;height: 18px; margin-top: 14px;" />
              </el-input>
            </el-form-item>


            <el-form-item prop="code" v-if="captchaEnabled" style="display: flex;align-items: center">
              <div class="w100 flexSb">
                <el-input
                  v-model="loginForm.code"
                  auto-complete="off"
                  placeholder="验证码"
                  style="flex:1; margin-right: 20px;"
                  @keyup.enter.native="handleLogin"
                >
                <img slot="prefix" src="@/assets/images/login/code.png" style="width: 16px;height: 18px; margin-top: 14px;" />
                </el-input>
                <div class="login-code">
                  <img :src="codeUrl" @click="getCode" class="login-code-img"/>
                </div>
              </div>
            </el-form-item>

            <el-checkbox v-model="loginForm.rememberMe" class="remember-label">记住密码</el-checkbox>

            <div class="btn-box">
              <el-button :loading="loading" type="primary" class="loginBtn" @click.native.prevent="handleLogin">登录
              </el-button>
            </div>

            <div class="contact-box">
              <div class="contact-info">
                <span class="phone">
                  <img src="@/assets/images/login/phone.png" alt="电话图标" class="icon"/>18601938676
                </span>
                    <span class="wechat" style="margin-left: 40px">
                  <img src="@/assets/images/login/wechat.png" alt="微信图标" class="icon"/>18601938676
                </span>
              </div>
            </div>

          </el-form>
        </div>
      </div>
   
      <div class="c-btn-img">
        <img src="@/assets/images/login/1.svg" class="btm-img" alt="" />
      </div>
      <div class="btm-desc">
        <!-- <el-select v-model="value" placeholder="请选择">
          <img slot="prefix" class="iconCss" src="@/assets/images/login/inter.png" />
          <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value">
          </el-option>
        </el-select> -->
        <div class="btm-desc-right">
          <div> Copyright：2026 慧猫开源停车平台</div>
          <div class="ml20"> 苏ICP备2024116970号</div>
        </div>
      </div>
    </div>

    <div class="page-right" v-if="!hiddenRight">
      <div class="container-font">
        <div class="font">
          {{ topText }}
        </div>
      </div>

      <div class="wechats">
        <div>
          <img src="@/assets/images/login/wecode3-1.png" class="wechat-image">
          <img src="@/assets/images/login/wecode3-2.png" class="wechat-image">
        </div>
      </div>
      <img src="@/assets/images/login/newlogo.png" class="right-img3" alt="" />
    </div>
  </div>
</template>

<script>
import { validUsername } from "@/utils/validate";
import Cookies from "js-cookie";
import { getCodeImg } from "@/api/login";
import { encrypt, decrypt } from '@/utils/jsencrypt'

export default {
  name: "Login",
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!validUsername(value)) {
        callback(new Error("请输入正确的手机号"));
      } else {
        callback();
      }
    };
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error("密码最少为6位"));
      } else {
        callback();
      }
    };
    const validatephone = (rule, value, callback) => {
      if (value) {
        if (/^1[3456789]\d{9}$/.test(value)) {
          callback()
        } else {
          callback(new Error("请输入正确格式的手机号"))
        }
      } else {
        callback(new Error("请输入手机号"))
      }
    }
    const validatePass = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else if (value.length < 6) {
        callback(new Error('密码长度不能小于6位'))
      } else {
        callback()
      }
    }
    const validatePass1 = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.registerform.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }
    return {
      platformIco: "@/assets/images/login/logo3.png",
      options: [{
        value: '1',
        label: '简体中文'
      }, {
        value: '2',
        label: '英文'
      }],
      value: '简体中文',
      operatortype: 'login',
      countdown: 0,
      showsub: false,
      showreg: '',
      isPassing: false,
      loginForm: {
        username: "",
        password: "",
      },
      loginRules: {
        // username: [
        //   { required: true, trigger: "blur", validator: validateUsername },
        // ],
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" }
        ],
        password: [
          { required: true, trigger: "blur", validator: validatePassword },
        ],
      },
      registerform: {
        password: '',
        passwordrepeat: ''
      },
      registerrules: {
        mobile: [
          {
            required: true,
            validator: validatephone
          }
        ],
        mobileCode: [
          {
            required: true,
            message: '请输入验证码',
            trigger: 'blur'
          }
        ],
        password: [
          {
            required: true,
            validator: validatePass
          }
        ],
        passwordrepeat: [
          {
            required: true,
            validator: validatePass1
          }
        ]
      },
      loading: false,
      passwordType: "password",
      timer: null,
      redirect: undefined,
      isShowQR: false,
      title: "慧猫开源停车平台",
      qrUrl: "",
      width: "410px",
      hiddenRight: false,
      topText: '慧猫开源停车平台 （V1.0.0）支持全业务场景，平台目前服务企业1000+，采用Spring Boot、Spring Cloud Alibaba、Spring Security、MyBatis-Plus、Swagger、Redis、MySQL等技术栈',
      platformLogo: require('@/assets/images/logo.png'),
      forgetpwdForm: {
        code:'',
        mobile:'',
        newPassword: '',
      },
      forgetpwdRules: {
        mobile: [
          {
            required: true,
            validator: validatephone
          }
        ],
        code: [
          {
            required: true,
            message: '请输入验证码',
            trigger: 'blur'
          }
        ],
        newPassword: [
          {
            required: true,
            validator: validatePass
          }
        ],
      },
      // 验证码开关
      captchaEnabled: false,
      codeUrl:'',
      
    };
  },
  watch: {
    $route: {
      handler: function (route) {
        this.redirect = route.query && route.query.redirect;
      },
      immediate: true,
    },
  },

  created() {
    this.getCode();
    this.getCookie();
  },
  mounted() {
    this.getScreenSize();
    window.onresize = () => {
      this.getScreenSize();
    };
  },
  methods: {
    getCookie() {
      const username = Cookies.get("username");
      const password = Cookies.get("password");
      const rememberMe = Cookies.get('rememberMe')
      this.loginForm = {
        username: username === undefined ? this.loginForm.username : username,
        password: password === undefined ? this.loginForm.password : decrypt(password),
        rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
      };
    },
    getCode() {
      getCodeImg().then(res => {
        this.captchaEnabled = res.captchaEnabled === undefined ? true : res.captchaEnabled;
        if (this.captchaEnabled) {
          this.codeUrl = "data:image/gif;base64," + res.img;
          this.loginForm.uuid = res.uuid;
        }
      });
    },
    getScreenSize() {
      this.screenWidth = window.innerWidth;
      this.screenHeight = window.innerHeight;
      if (this.screenWidth < 900) {
        this.hiddenRight = true;
      } else {
        this.hiddenRight = false;
      }

      if (this.screenWidth < 550) {
        this.width = "100%";
      } else {
        this.width = "455px";
      }
    },
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true;

          if (this.loginForm.rememberMe) {
            Cookies.set("username", this.loginForm.username, { expires: 30 });
            Cookies.set("password", encrypt(this.loginForm.password), { expires: 30 });
            Cookies.set('rememberMe', this.loginForm.rememberMe, { expires: 30 });
          } else {
            Cookies.remove("username");
            Cookies.remove("password");
            Cookies.remove('rememberMe');
          }

          this.$store.dispatch("Login", this.loginForm).then(() => {
              let path = this.redirect || '/';
            this.$router.push({ path: path }).catch(()=>{});
          }).catch(() => {
            this.loading = false;
            if (this.captchaEnabled) {
              this.getCode();
            }
          });
        }

      });
    },
    changeOperatortype(type){
        this.operatortype = type;
        clearInterval(this.timer);
        this.timer = null;
        this.countdown = 0;
    },
  },
};
</script>
<style lang="scss">
.icon-style {
  font-size: 18px;
}

.relitave {
  position: relative;
}

.iconCss {
  width: 18px;
  height: 18px;
  margin-top: 14px;
  margin-left: -10px;
  position: absolute;
}

.btm-desc .el-select {
  width: 150px;
  height: 36px;
}

input:-internal-autofill-previewed,
input:-internal-autofill-selected {
  -webkit-text-fill-color: #919191 !important;
  transition: background-color 5000s ease-in-out 10s !important;
}
</style>
<style lang="scss" scoped>
.el-input--prefix ::v-deep .el-input__inner {
  padding-left: 50px;
}

.btm-desc ::v-deep .el-input__inner {
  height: 40px;
  line-height: 40px !important;
  vertical-align: middle !important;
  padding-top: 2px !important;
}

::v-deep .el-input__prefix {
  padding-left: 12px !important;
}

.container-page {
  display: flex;
  justify-content: flex-start;
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: relative;

  .container-des {
    position: absolute;
    left: 1.7vw;
    top: 1.7vw;
    z-index: 100;
    display: flex;
    align-items: center;
    font-size: 20px;
    color: #333333;
    width: calc(100% - 500px);
  }

  .page-left {
    flex-grow: 1;
    background-color: #f3f6ff;
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;

    .content-form {
      width: 100%;
      z-index: 999;
    }

    .wrap-form {
      margin: 0 auto;
      background: #ffffff;
      box-shadow: 0px 5px 13px 0px rgba(183, 195, 219, 0.53);
      border-radius: 10px;
      padding: 39px 33px 30px 33px;
      box-sizing: border-box;
      width: 410px !important;
      position: relative;

      .form-tabs {
        width: 58px;
        border-bottom: 1px solid #5365ea;
        padding-bottom: 9px;
        margin-bottom: 18px;
        font-size: 14px;
        color: #5365EA;
      }

      .form-title {
        font-weight: 400;
        font-size: 24px;
        color: #3F3E3E;
        margin-bottom: 35px;
        position: relative;
      }

      .free-jieru{
        width: 62px;
        height: 20px;
        background: #F83535;
        border-radius: 6px;
        font-family: Microsoft YaHei;
        font-weight: bold;
        font-size: 12px;
        color: #FFFFFF;
        display: flex;
        align-items: center;
        justify-content: center;
        position: absolute;
        top: -14px;
        right: 36px;

      }

      .view-newversion{
          border-radius: 4px;
          font-family: Microsoft YaHei;
          font-size: 12px;
          color: #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          position: absolute;
          top: 0px;
          right: 0px;
          border: 1px solid #5365ea;
          padding: 7px 12px;
          font-size: 13px;
          color: #5365ea;
          cursor: pointer;
      }

      ::v-deep .el-form {
        .el-form-item__label {
          font-size: 16px;
          color: #000000;
        }

        .el-input__inner {
          background-color: #fff;
          height: 51px;
          line-height: 46px;
          font-size: 14px;
          color: #919191;
        }

        .el-form-item__content {
          // border-bottom: 1px solid #f5f5f5;
          padding-bottom: 0px;
        }
      }

      .loginBtn {
        width: 100%;
        height: 50px;
        background: #3E64F3;
        border-radius: 3px;
        font-size: 18px;
        color: #ffffff;
      }
    }

    .info-email-phone {
      margin: 24px 20px;
      color: #7c7fa5;
      display: flex;
      justify-content: space-between;
      position: relative;
      z-index: 1;

      .info-email,
      .info-phone {
        display: flex;
        align-items: center;
      }

      img {
        margin-right: 4px;
        width: 20px;
        height: 20px;
      }
    }

    .c-btn-img {
      position: absolute;
      bottom: 0;
      width: 100%;

      .btm-img {
        width: 100% !important;
      }
    }

    .btm-desc {
      width: 100%;
      position: absolute;
      bottom: 15px;
      display: -webkit-box;
      display: -ms-flexbox;
      display: flex;
      padding-left: 20px;
      padding-right: 20px;
      font-size: 14px;
      color: #666;
      height: 40px;
      line-height: 40px;
      text-align: center;
    }

    .btm-desc-right {
      display: flex;
    }
  }

  .ml20 {
    margin-left: 20px;
  }

  .page-right {
    width: 500px;
    position: relative;
    overflow: hidden;

    .wechats {
      margin-top: calc(20% + 140px);
      text-align: center;

      .free-title-image{
        width: 340px;
        margin-bottom: 40px;
      }

      .wechat-image {
        width: 140px;
        margin-left: 10px;
        margin-right: 10px;
      }
    }

    .container-font {
      padding: 10px 16px;
      background-color: #fff;
      z-index: 999;
      width: 400px;
      height: 165px;
      background-image: url("~@/assets/images/login/2.svg");
      background-repeat: no-repeat;
      background-size: cover;
      position: absolute;
      // top: 11%;
      top: 60px;
      left: 50%;
      transform: translate(-50%, 0);
      color: #5365ea;
      font-size: 14px;
      line-height: 24px;
    }

    .right-img2 {
      position: absolute;
      bottom: 3.9%;
      left: 50%;
      transform: translate(-50%, 0);
      width: 280px;
    }

    .right-img3 {
      position: absolute;
      bottom: 3%;
      left: 50%;
      transform: translate(-50%, 0);
      width: 300px;
      opacity: 0.8;
      // height: 373px;
    }
  }
}

.huige {
  width: auto;
  height: 26px;
  margin-right: 10px;
}

.menus {
  flex: 1;
  text-align: right;
  padding-right: 40px;
  font-size: 16px !important;
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: flex-end;


  .menu {
    display: inline-block;
    margin-left: 20px;
    margin-right: 20px;
  }
}

.f-ib {
  display: inline-block;
}

.f-vat {
  vertical-align: top;
}

.hui {
  position: relative;
  top: 1px;
  right: 4px;
  width: 22px;
  height: 21px;
}

.f-pr {
  position: relative;
}

.submenus {
  position: absolute;
  font-size: 14px;
  text-align: center;
  line-height: 36px;
  left: -42px;
}

.submenus-wrapper {
  background-color: white;
  width: 130px;
  border-radius: 5px;
  padding: 10px;
  margin-top: 10px;
}

.submenu {
  display: block;
}

.submenu a:hover {
  color: #5365ea;
}

.up1 {
  transform: rotate(180deg);
}

.up,
.up1 {
  position: relative;
  top: 8px;
  left: 4px;
}

.register {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  text-align: center;
  font-size: 16px;
  color: #959595;
}

.register-link {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.25s ease, color 0.25s ease;
}

.register-link-primary {
  padding: 4px 0 8px;
}

.register-link-primary:hover {
  transform: translateY(-1px);
}

.register-link-primary .register-link-text {
  position: relative;
  display: inline-block;
  color: #1f4fff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
}

.register-link-primary .register-link-text::after {
  content: "";
  position: absolute;
  left: 0;
  bottom: -6px;
  width: 100%;
  height: 2px;
  border-radius: 999px;
  background: #254fe9;
  transform-origin: left center;
  transform: scaleX(0.4);
  opacity: 0.7;
  transition: transform 0.25s ease, opacity 0.25s ease;
}

.register-link-primary:hover .register-link-text::after {
  transform: scaleX(1);
  opacity: 1;
}

.register-link-secondary {
  color: #7d8fd0;
  font-size: 15px;
}

.register-link-secondary:hover {
  color: #254FE9;
  transform: translateY(-1px);
}

.reg {
  font-size: 16px;
  color: #3E64F3;
}

.contact-box{
    margin: 30px 0px 0px 0px;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
}

.bottom-line{
  width: 344px;
  height: 1px;
  background: #F3F6FF;
  margin-bottom: 20px;
}

.contact-info {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #8c939d;
}

.contact-info .icon {
  width: 16px;
  height: 16px;
  margin-right: 5px;
  vertical-align: middle;
}

.contact-info span {
  display: inline-flex;
  align-items: center;
}

.main-title{
  font-weight: 400;
  font-size: 22px;
  color: #f7891d;
  margin-bottom: 40px;
  text-align: center;
}

.free-title{
    display: flex;
    justify-content: center;
    align-items: center;
    margin-bottom: 40px;
}

.free-title-image{
  width: 380px;
}


.my-free-box{
  position: absolute;
  top: -50px;
  right: 0px;
  z-index: -1;

  .my-free-title{
    font-family: Microsoft YaHei;
    font-weight: bold;
    font-size: 14px;
    line-height: 1.2;
    color: #fff;
    padding: 7px 14px;
    min-height: 32px;
    background: #F83535;
    border-radius: 10px;
    position: absolute;
    top: -18px;
    right: -94px;
    box-shadow: 0 8px 18px rgba(248, 53, 53, 0.25);
  }

  .my-free-btn{
    width: 150px;
    height: 86px;
    background: linear-gradient(174deg, #FFECC8, #FDC678, #FFBF37);
    border-radius: 16px;
    font-family: Microsoft YaHei;
    font-weight: bold;
    font-size: 18px;
    line-height: 1;
    color: #C57C15;
    display: flex;
    padding-top: 18px;
    justify-content: center;
    box-shadow: 0 14px 28px rgba(255, 191, 55, 0.28);
  }
}


.login-code {
  width: 100px;
  height: 51px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.login-code-img {
  height: 51px;
  width: 100px;
}


.remember-label{
  margin-bottom: 18px;
}

.flexSb{
  display: flex;
  align-items: center;
  justify-content: space-between;
}

</style>
