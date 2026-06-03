<template>
  <!-- 规则设置弹窗 -->
  <el-dialog
    id="rulePopupModal"
    title="收费规则"
    :visible.sync="visible"
    width="500px"
  >
    <el-form
      ref="form_rule"
      :model="formData"
      label-width="150px"
      :rules="formRule"
      v-loading="loading"
    >
      <el-form-item class="nav-form-item" label="收费规则" prop="chargeRuleId">
        <el-select
          v-model="formData.chargeRuleId"
          placeholder="请选择收费规则"
          clearable
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="item in ruleList"
            :label="item.ruleName"
            :value="item.ruleId"
            :key="item.ruleId"
          />
        </el-select>
        <p
          class="status-name"
          style="color: #5f7df9; cursor: pointer"
          @click="editRule"
        >
          <i class="el-icon-plus"></i>新建收费规则
        </p>
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="confirmHandler">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import { listRule } from '@/api/parking/rule'
import { updateLot } from '@/api/parking/lot'

export default {
  name: 'PileRuleDialog',
  data() {
    return {
      // 控制弹窗显示
      visible: false,
      loading: false,
      // 规则列表
      ruleList: [],
      // 表单数据
      // formData: {
      //   lotId: '', 
      //   chargeRuleId: '',  // 规则ID
      // },
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
      // 校验规则
      formRule: {
        chargeRuleId: [{ required: true, message: '请选择规则', trigger: 'blur' }],
      },
      // 当前操作的充电桩行数据
      currentRow: null,
    };
  },
  methods: {
    /**
     * 打开规则设置弹窗
     * @param {Object} row 表格当前行
     */
    open(row) {
      this.currentRow = row;
      this.formData = row;
      this.formData.lotId = row.lotId;
      this.formData.chargeRuleId = row.chargeRuleId || '';
      this.visible = true;
      // 加载规则列表
      this.loadRuleList(row.tenantId);
    },

    /**
     * 加载规则列表
     */
    loadRuleList(tenantId) {
      this.loading = true;
      listRule({
        pageNum: 1,
        pageSize: 200,
        tenantId: tenantId,
      }).then(res => {
        this.ruleList = res.data || [];
      }).finally(() => {
        this.loading = false;
      });
    },

    /**
     * 新建规则（跳转到规则页面）
     */
    editRule() {
      this.$router.push({ path: '/parking/rule' });
      this.visible = false;
    },

    /**
     * 确认保存
     */
    confirmHandler() {
      this.$refs.form_rule.validate(valid => {
        if (!valid) return;

        updateLot(this.formData).then(() => {
          this.$message.success('设置成功');
          this.visible = false;
          // 通知父页面刷新列表
          this.$emit('success');
        });
      });
    },
  },
};
</script>
