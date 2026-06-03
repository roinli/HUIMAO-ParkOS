<template>
  <div>
    <el-dialog
      id="rulePopupModal"
      title="停车场"
      :visible.sync="dialogVisible"
      width="800px"
    >

      <submit-form
        ref="form"
        :labelWidth="120"
        :rules="formRule"
        v-model="formData"
        :forms="submitForms"
      >
        <!-- 租户 -->
        <template #tenantId="{ scope }">
          <el-select
            v-model="scope.params[scope.modelValue]"
            clearable
            filterable
            :disabled="!hasTenantPermission || tenantOptions.length <= 0"
            placeholder="请选择租户"
            style="width: 100%"
            @change="handleFormTenantChange"
          >
            <el-option
              v-for="item in tenantOptions"
              :key="item.id"
              :label="item.tenantName"
              :value="item.id"
            />
          </el-select>
        </template>

        <!-- 收费规则 -->
        <template #chargeRuleId="{ scope }">
          <el-select
            v-model="scope.params[scope.modelValue]"
            clearable
            filterable
            placeholder="请选择收费规则"
            style="width: 100%"
          >
            <el-option
              v-for="item in ruleOptions"
              :key="item.ruleId"
              :label="item.ruleName"
              :value="item.ruleId"
            />
          </el-select>
        </template>

        <!-- 位置信息 省市区三级联动 -->
        <template #location="{ scope }">
          <div style="display: flex;align-items:center;">
            <el-select style="width:200px" v-model="province" @change="selProvince" placeholder="请选择省份">
              <el-option v-for="item in provinceList" :label="item.name" :value="item.id" :key="item.id"></el-option>
            </el-select>
            <el-select @change="selCity" style="width:200px;margin-left:10px" v-model="city" placeholder="请选择市">
              <el-option v-for="item in cityList" :label="item.name" :value="item.id" :key="item.id"></el-option>
            </el-select>
            <el-select @change="getXY" style="width:200px;margin-left:10px" v-model="formData.areaCode" placeholder="请选择区">
              <el-option v-for="item in areaList" :label="item.name" :value="item.id" :key="item.id"></el-option>
            </el-select>
            <el-button @click="openSelectAddress" type="primary" icon="el-icon-plus" style="margin-left:20px;">选择位置</el-button>
          </div>
        </template>

        <!-- 停车场图片 -->
        <template #parkImg="{ scope }">
          <el-upload
            ref="upload"
            class="avatar-uploader"
            :action="uploadUrl"
            :headers="headers"
            :data="uploadData"
            :show-file-list="false"
            name="file"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
          >
            <img v-if="imageUrl" :src="imageUrl" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"></i>
          </el-upload>
        </template>
      </submit-form>


      <!-- 二维码预览区域（仅编辑模式显示） -->
      <div v-if="!isAdd" class="qr-section">
        <div class="form-group-title">车场二维码</div>

        <div class="qr-container">
          <div class="qr-left">
            <img :src="qrCodeUrl" alt="二维码" class="qr-image">
          </div>
          <div class="qr-right">
            <div class="qr-top">
              <div class="qr-title">二维码链接</div>
              <el-input v-model="qrCodeContent" placeholder="二维码链接" class="qr-input"></el-input>
            </div>

            <el-button @click="copyQrLink" type="primary" class="qr-btn">复制链接</el-button>
            <el-button @click="downloadQrCode" type="primary" class="qr-btn download-btn">下载二维码</el-button>
          </div>
        </div>
      </div>

      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogVisible = false">取 消</el-button>
        <el-button :disabled="isDisable" type="primary" @click="submitHandler" style="margin-left: 10px;">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 地图弹窗 -->
    <el-dialog title="选择位置" :visible.sync="isShowDialog" width="800px">
      <div class="search">
        <el-input placeholder="请输入搜索地址信息，例如：北京市故宫博物院" v-model="keyword" clearable>
          <el-button slot="append" icon="el-icon-search" @click="searchAddress"></el-button>
        </el-input>
      </div>
      <div id="mapContainer" style="width: 100%; height: 300px"></div>
      <div class="address">
        <span>当前选点：</span>
        <b>{{
            nowAddress
              ? getNowAddressText(nowAddress)
              : "尚未选点"
          }}</b>
        <el-button v-if="nowAddress" @click="selectAddress(nowAddress, 1)" type="text">【确认选择】</el-button>
      </div>
      <el-table highlight-current-row :data="nearPointList" height="250" style="width: 100%" class="table additem">
        <el-table-column prop="address" label="附近推荐地点">
          <template slot-scope="scope">
            {{ scope.row.address }}{{ scope.row.name }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button @click="selectAddress(scope.row, 2)" type="text">确认选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="isShowDialog = false">取 消</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { addLot, updateLot } from '@/api/parking/lot'
import { listTenant } from '@/api/system/tenant'
import { listRule } from '@/api/parking/rule'
import { lotStatusOptions, lotTypeOptions } from '@/views/parking/constants'
import { getProvinceList, getCityList, getMapGeocoder, getMapSuggestion } from '@/api/common'
import { checkPermi } from '@/utils/permission'
import { getToken, getTenant } from '@/utils/auth'
import QRCode from 'qrcode'

export default {
  name: 'ParkingLotDialog',
  emits: ['success'],
  data() {
    return {
      dialogTitle: '',
      dialogVisible: false,
      loading: false,
      isAdd: false,
      tenantOptions: [],
      ruleOptions: [],
      hasTenantPermission: false,

      // 省市区
      province: '',
      city: '',
      area: '',
      provinceList: [],
      cityList: [],
      areaList: [],

      // 上传配置
      uploadUrl: process.env.VUE_APP_BASE_API + "/file/uploadImg",
      headers: {
        Authorization: 'Bearer ' + getToken(),
        'tenant-id': getTenant()
      },
      uploadData: { business: "parking_lot" },
      imageUrl: "",

      // 二维码相关
      qrCodeUrl: '',
      qrCodeContent: '',

      formData: {
        lotId: undefined,
        tenantId: undefined,
        lotCode: undefined,
        lotName: undefined,
        status: '0',
        chargeRuleId: undefined,
        provinceCode: undefined,
        provinceName: undefined,
        cityCode: undefined,
        cityName: undefined,
        areaCode: undefined,
        areaName: undefined,
        address: undefined,
        totalSpace: '',
        tempSpace: '',
        monthlySpace: '',
        contactName: undefined,
        contactPhone: undefined,
        payQrContent: undefined,
        payQrUrl: undefined,
        remark: undefined,
        fileUrl: '',
        spaceType: undefined,
        lng: undefined,
        lat: undefined
      },

      submitForms: [
        { itemType: 'groupTitle', label: '基础信息', colSpan: 24 },
        { label: '停车场名称', modelValue: 'lotName', itemType: 'input', placeholder: '请输入名称', clearable: true, colSpan: 12 },
        { label: '停车场编码', modelValue: 'lotCode', itemType: 'input', placeholder: '请输入编码', clearable: true, colSpan: 12,disabled: true, hide: this.isAdd },
        { label: '所属租户', modelValue: 'tenantId', slotName: 'tenantId', colSpan: 12 },
        { label: '总车位数', modelValue: 'totalSpace', itemType: 'input', placeholder: '请输入总车位数', colSpan: 12 },
        { label: '临停车位数', modelValue: 'tempSpace', itemType: 'input', placeholder: '请输入临停车位数', colSpan: 12 },
        { label: '固定车位数', modelValue: 'monthlySpace', itemType: 'input', placeholder: '请输入固定车位数', colSpan: 12 },
        { label: '位置信息', modelValue: 'location', slotName: 'location', colSpan: 24 },
        { label: '详细地址', modelValue: 'address', itemType: 'input', colSpan: 24 },
        { label: '经度', modelValue: 'lng', itemType: 'input', placeholder: '请选择位置或输入经度', clearable: true, colSpan: 12 },
        { label: '纬度', modelValue: 'lat', itemType: 'input', placeholder: '请选择位置或输入纬度', clearable: true, colSpan: 12 },
        { label: '停车场图片', modelValue: 'fileUrl', slotName: 'parkImg', colSpan: 24 },

        { itemType: 'groupTitle', label: '运营信息', colSpan: 24 },
        { label: '车位类型', modelValue: 'spaceType', itemType: 'select', options: lotTypeOptions, colSpan: 12 },
        { label: '运营状态', modelValue: 'status', itemType: 'select', options: lotStatusOptions, colSpan: 12 },
        { label: '联系人', modelValue: 'contactName', itemType: 'input', colSpan: 12 },
        { label: '联系电话', modelValue: 'contactPhone', itemType: 'input', colSpan: 12 },
        // { label: '二维码内容', modelValue: 'payQrContent', itemType: 'input', type: 'textarea', colSpan: 24 },
        // { label: '二维码链接', modelValue: 'payQrUrl', itemType: 'input', colSpan: 24 },
      ],

      formRule: {
        tenantId: [{ required: true, message: '请选择租户', trigger: 'change' }],
        //lotCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
        lotName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
        chargeRuleId: [{ required: true, message: '请选择规则', trigger: 'change' }],
        totalSpace: [{ required: true, message: '请输入总车位数', trigger: 'blur' }],
        status: [{ required: true, message: '请选择状态', trigger: 'change' }],
        contactPhone: [{ pattern: /^([0-9-+()]{6,20})$/, message: '电话格式错误' }],
        fileUrl: [{ required: true, message: '请上传停车场图片', trigger: 'change' }],
        spaceType: [{ required: true, message: '请选择车位类型', trigger: 'change' }],
      },

      isDisable: false,
      isShowDialog: false,
      keyword: "",
      nearPointList: [],
      markersArray: [],
      geocoder: null,
      nowAddress: null,
      geocoderLocation: null,
    }
  },

  mounted() {
    this.hasTenantPermission = checkPermi(['system:tenant:list'])
    console.log("this.hasTenantPermission", this.hasTenantPermission);

    this.loadTenant()
    this.loadRule()
    this.getProvinceList()
  },

  methods: {
    getNowAddressText(item) {
      if (!item) return ''
      const comps = item.addressComponents || {}
      const streetNumber = typeof comps.streetNumber === 'string'
        ? comps.streetNumber
        : ((comps.street || '') + (comps.street_number || ''))
      return [
        comps.province || '',
        comps.city || '',
        comps.district || '',
        streetNumber || item.address || ''
      ].join('')
    },

    normalizeMapAddress(item) {
      if (!item) return null
      const ad = item.ad_info || {}
      const comps = item.addressComponents || item.address_component || {}
      const location = item.location || {}
      if (location.lng === undefined || location.lat === undefined) {
        return null
      }
      return {
        name: item.title || item.name || '',
        title: item.title || item.name || '',
        address: item.address || item.addr || '',
        location: {
          lng: location.lng,
          lat: location.lat
        },
        addressComponents: {
          province: comps.province || ad.province || '',
          city: comps.city || ad.city || '',
          district: comps.district || ad.district || '',
          streetNumber: typeof comps.streetNumber === 'string'
            ? comps.streetNumber
            : ((comps.street || '') + (comps.street_number || ''))
        }
      }
    },

    // 停车场图片上传
    handleUploadSuccess(res, file) {
      if (res.code !== 200) {
        this.$message.error(res.msg || '上传失败')
        return
      }
      const url = res.data && (res.data.fileUrl || res.data.url || res.data.imgID)
      this.formData.fileUrl = url || ''
      this.imageUrl = this.formData.fileUrl
      this.$refs.upload.clearFiles()
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.$refs.submitForm.clearValidate('fileUrl')
      })
    },

    beforeUpload(file) {
      const isImage = ['image/jpeg', 'image/png'].includes(file.type)
      const isLt5M = file.size / 1024 / 1024 < 5
      if (!isImage) {
        this.$message.error('停车场图片只能上传 JPG/PNG 格式')
      }
      if (!isLt5M) {
        this.$message.error('停车场图片大小不能超过 5MB')
      }
      return isImage && isLt5M
    },

    // 生成二维码
    generateQrCode(url) {
      //const qrContent = this.formData.payQrContent || this.formData.payQrUrl || url

      const domain = window.location.origin;
      const qrContent = 'http://huimao.h5.huizhidata.com/?lotCode=' + this.formData.lotCode;
      this.qrCodeContent = qrContent;

      if (!qrContent) {
        this.qrCodeUrl = ''
        return
      }
      QRCode.toDataURL(qrContent, { width: 200, margin: 2 }, (err, url) => {
        if (err) {
          console.error(err)
          return
        }
        this.qrCodeUrl = url
      })
    },

    // 复制二维码链接
    copyQrLink() {
      const input = document.createElement('input')
      input.value = this.qrCodeContent
      document.body.appendChild(input)
      input.select()
      document.execCommand('copy')
      document.body.removeChild(input)
      this.$message.success('复制成功')
    },

    // 下载二维码
    downloadQrCode() {
      if (!this.qrCodeUrl) return
      const link = document.createElement('a')
      link.href = this.qrCodeUrl
      link.download = `停车场二维码_${this.formData.lotName || '未命名'}.png`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
    },

    loadTenant() {
      if (!this.hasTenantPermission) {
        const currentTenantId = getTenant()
        if (currentTenantId) {
          this.tenantOptions = [{
            id: currentTenantId,
            tenantName: `当前租户(${currentTenantId})`
          }]
          this.formData.tenantId = this.formData.tenantId || currentTenantId
        }
        return
      }
      listTenant({ pageNum: 1, pageSize: 1000 }).then(res => {
        this.tenantOptions = res.data || []
      })
    },

    loadRule(tenantId) {
      const params = { pageNum: 1, pageSize: 1000 }
      if (tenantId) params.tenantId = tenantId
      listRule(params).then(res => {
        this.ruleOptions = res.data || []
      })
    },

    handleFormTenantChange(val) {
      this.formData.chargeRuleId = undefined
      this.loadRule(val)
    },

    getProvinceList() {
      getProvinceList().then((res) => {
        this.provinceList = [...res.data]
      })
    },

    selProvince() {
      const province = this.provinceList.find(item => item.id === this.province)
      this.formData.provinceCode = this.province
      this.formData.provinceName = province?.name || ''
      this.city = ''
      this.area = ''
      this.areaList = []
      this.resetCityArea()
      getCityList({ pid: this.province }).then(res => {
        this.cityList = res.data || []
      })
    },

    selCity() {
      const city = this.cityList.find(item => item.id === this.city)
      this.formData.cityCode = this.city
      this.formData.cityName = city?.name || ''
      this.area = ''
      this.formData.areaCode = ''
      this.formData.areaName = ''
      getCityList({ pid: this.city }).then(res => {
        this.areaList = res.data || []
      })
    },

    getXY() {
      this.area = this.formData.areaCode
      const area = this.areaList.find(item => item.id === this.formData.areaCode)
      this.formData.areaName = area?.name || ''
    },

    resetCityArea() {
      this.formData.cityCode = ''
      this.formData.cityName = ''
      this.formData.areaCode = ''
      this.formData.areaName = ''
    },

    openDialog(data) {
      this.isAdd = !data
      this.dialogTitle = this.isAdd ? '新增停车场' : '修改停车场'
      this.formData = { ...this.$options.data().formData }
      this.imageUrl = ''
      this.qrCodeUrl = ''

      if (data) {
        Object.assign(this.formData, data)
        this.imageUrl = data.fileUrl || ''
        this.province = this.formData.provinceCode || ''
        this.city = this.formData.cityCode || ''
        this.area = this.formData.areaCode || ''

        // 编辑模式生成二维码
        this.generateQrCode(this.formData.payQrContent || this.formData.payQrUrl)

        if (this.province) {
          getCityList({ pid: this.province }).then(res => { this.cityList = res.data || [] })
        }
        if (this.city) {
          getCityList({ pid: this.city }).then(res => { this.areaList = res.data || [] })
        }
      } else {
        if (!this.hasTenantPermission) {
          this.formData.tenantId = getTenant()
        }
        this.province = ''
        this.city = ''
        this.area = ''
        this.cityList = []
        this.areaList = []
      }
      this.dialogVisible = true
    },

    closeDialog() {
      this.dialogVisible = false
      this.$refs.form.clearValidate()
    },

    async submitHandler() {
      try {
        await this.$refs.form.validate()
        this.loading = true
        const api = this.isAdd ? addLot : updateLot
        await api(this.formData)
        this.$message.success('操作成功')
        this.closeDialog()
        this.$emit('success')
      } catch (e) {} finally {
        this.loading = false
      }
    },

    // 地图相关方法
    openSelectAddress() {
      this.isShowDialog = true;
      setTimeout(() => {
        var sdd = "";
        if (this.province) {
          if (isNaN(this.province)) { sdd = sdd + this.province;
          } else { this.provinceList.map((item) => { if (item.id == this.province) { sdd = sdd + item.name; } }); }
        }
        if (this.city) {
          if (isNaN(this.city)) { sdd = sdd + this.city;
          } else { this.cityList.map((item) => { if (item.id == this.city) { sdd = sdd + item.name; } }); }
        }
        if (this.formData.areaCode) {
          this.areaList.map((item) => { if (item.id == this.formData.areaCode) { sdd = sdd + item.name; } });
        }
        this.keyword = sdd + (this.formData.address || '');
        this.initMap();
        if (this.keyword) { this.searchAddress(); }
      });
    },

    async selectAddress(item, type) {
      this.isDisable = false;
      const target = this.normalizeMapAddress(item)
      if (!target) {
        return
      }
      if (type === 1) {
        this.formData.lng = target.location.lng
        this.formData.lat = target.location.lat
        this.formData.address = (target.address || '').replace("中国", "")
        this.nowAddress = target
        const ssq = [
          target.addressComponents.province,
          target.addressComponents.city,
          target.addressComponents.district,
        ]
        this.selectSSQ(ssq)
        this.isShowDialog = false;
      }
      if (type === 2) {
        this.formData.lng = target.location.lng
        this.formData.lat = target.location.lat
        this.formData.address = (target.address || '') + (target.title || '')
        this.nowAddress = target
        var reg = /.+?(省|市|自治区|自治州|县|区)/g;
        var ssq = this.formData.address.match(reg) || []
        if (ssq.length < 3 && ssq.length > 0) {
          ssq.unshift(ssq[0]);
        }
        this.selectSSQ(ssq)
        this.isShowDialog = false;
      }
    },

    selectSSQ(ssq) {
      if (!ssq || ssq.length < 3) {
        return;
      }
      this.provinceList.map((item1) => {
        if (item1.name.indexOf(ssq[0]) !== -1) {
          this.province = item1.id;
          this.formData.provinceCode = item1.id
          this.formData.provinceName = item1.name
          getCityList({ pid: item1.id }).then((res2) => {
            this.cityList = [...(res2.data || [])];
            this.cityList.map((item2) => {
              if (item2.name.indexOf(ssq[1]) !== -1) {
                this.city = item2.id;
                this.formData.cityCode = item2.id
                this.formData.cityName = item2.name
                getCityList({ pid: item2.id }).then((res3) => {
                  this.areaList = [...(res3.data || [])];
                  this.areaList.map((item3) => {
                    if (item3.name.indexOf(ssq[2]) !== -1) {
                      this.area = item3.id;
                      this.formData.areaCode = item3.id;
                      this.formData.areaName = item3.name
                    }
                  });
                });
              }
            });
          });
        }
      });
      this.formData.address = (this.formData.address || '')
        .replace(ssq[0], "")
        .replace(ssq[1], "")
        .replace(ssq[2], "")
    },

    searchAddress() {
      if (!this.keyword) { return this.$message.error("请输入搜索地址信息"); }
      this.setLocationByAddress(this.keyword);
    },

    setLocationByAddress(address) {
      setTimeout(() => { this.getNearby({ lat: '', lng: '', keyword: address }) });
    },

    getNearby(params) {
      let that = this;
      getMapSuggestion(params).then(res => {
        that.nearPointList = []
        let data = (res.data || []).map(item => that.normalizeMapAddress(item)).filter(Boolean)
        if (!data.length) {
          return that.$message.warning('未找到相关地址')
        }
        that.nearPointList = data;
        let location = new qq.maps.LatLng(data[0].location.lat, data[0].location.lng);
        that.location = location
        that.map.setCenter(location);
        if (that.centerMarker) { that.centerMarker.setMap(null) }
        that.centerMarker = new qq.maps.Marker({ map: that.map, position: location });
      }).catch(() => {
        that.$message.error('地址搜索失败，请检查地图接口配置')
      })
    },

    initMap() {
      let that = this;
      var map = new qq.maps.Map(document.getElementById("mapContainer"), {
        zoom: 13, mapTypeId: qq.maps.MapTypeId.ROADMAP,
      });
      that.map = map
      var listener = qq.maps.event.addListener(map, "click", function (event) {
        that.setLocationByLatLng(event.latLng.getLat(), event.latLng.getLng());
      });
    },

    setLocationByLatLng(lat, lng) {
      let that = this;
      getMapGeocoder({ lat: lat, lng: lng }).then(res2 => {
        if (res2.data != null) {
          that.nearPointList = (res2.data.pois || []).map(item => that.normalizeMapAddress(item)).filter(Boolean);
          that.nowAddress = that.normalizeMapAddress(res2.data);
          let location = new qq.maps.LatLng(res2.data.location.lat, res2.data.location.lng);
          that.map.setCenter(location);
          if (that.centerMarker) { that.centerMarker.setMap(null) }
          that.centerMarker = new qq.maps.Marker({ map: that.map, position: location });
          this.markersArray.push(that.centerMarker);
        }
      }).catch(() => {
        that.$message.error('地址解析失败，请检查地图接口配置')
      })
    },
  }
}
</script>

<style scoped>
.drawer-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 15px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: flex-end;
  z-index: 10;
}

::v-deep .el-drawer__body{
  padding: 0px 30px 100px 30px;
}

/* 停车场图片样式 */
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  border: 1px dashed #d9d9d9;
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 200px;
  line-height: 200px;
  text-align: center;
}
.avatar {
  width: 200px;
  height: 200px;
  display: block;
}

/* 二维码样式 */
.qr-section {
  margin-top: 20px;
  padding-top: 20px;
}
.qr-container {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 0px 20px 20px 20px;
}
.qr-image {
  width: 150px;
  height: 150px;
  border: 1px solid #eee;
}
.qr-right {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.qr-top{
  display: flex;
  align-items: center;
}
.qr-title{
    font-size: 14px;
    color: #606266;
    margin-right: 12px;
}
.qr-input {
  width: 300px;
}
.qr-btn {
  width: 120px;
}
.download-btn {
  margin-left: 0px !important;
}

.form-group-title {
  font-weight: 600;
  font-size: 14px;
  color: #292929;
  margin-bottom: 30px;
  display: flex;
  align-items: center;
  margin-left:20px;

  &::before {
    content: "";
    width: 5px;
    height: 14px;
    background: #436EFE;
    margin-right: 12px;
    border-radius: 4px;
  }
}

/* 居中抽屉 */
.center-drawer .el-drawer__wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}
.center-drawer .el-drawer {
  position: relative !important;
  height: auto !important;
  max-height: 90vh;
  left: unset !important;
  right: unset !important;
}
</style>
