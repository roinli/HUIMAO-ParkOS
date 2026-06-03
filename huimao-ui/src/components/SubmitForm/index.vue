<template>
  <el-form
    ref="submitForm"
    :rules="rules"
    :model="params"
    @submit.native.prevent
    :label-width="labelWidth ? labelWidth + 'px' : ''"
    :size="size"
  >
    <el-row :gutter="20">
      <el-col v-for="(form, index) in forms.filter(f => typeof f.visible !== 'function' || f.visible(params))" :key="index" :span="form.colSpan || 24">

        <div v-if="form.itemType === 'groupTitle'" class="form-group-title">
          {{ form.label }}
        </div>

        <el-form-item :label="form.label" :prop="form.modelValue"  v-else>
          <template v-if="form.slotName">
            <slot :name="form.slotName" :scope="{ params, modelValue: form.modelValue }"></slot>
          </template>
          <span v-else-if="form.itemType === 'text'">{{params[form.modelValue]}}</span>
          <el-input v-else-if="form.itemType === 'input'" v-bind="form || {}" v-model="params[form.modelValue]" />
          <el-input v-else-if="form.itemType === 'textarea'" type="textarea" :rows="3" v-bind="form || {}" v-model="params[form.modelValue]" />
          <el-input-number v-else-if="form.itemType === 'inputNumber'" controls-position="right" v-bind="form || {}" v-model="params[form.modelValue]" />
          <el-select v-else-if="form.itemType === 'select'" v-bind="form || {}" v-model="params[form.modelValue]">
            <el-option
              v-for="option in form.options"
              :key="option[form.valueKey || 'value']"
              :value="typeof option === 'object' ? option[form.valueKey || 'value'] : option"
              :label="typeof option === 'object' ? option[form.labelKey || 'label'] : option"
            />
          </el-select>
          <el-radio-group v-else-if="form.itemType === 'radio'" v-bind="form || {}" v-model="params[form.modelValue]">
            <el-radio
              v-for="option in form.options"
              :key="option[form.valueKey || 'value']"
              :label="typeof option === 'object' ? option[form.valueKey || 'value'] : option"
            >
              {{ typeof option === 'object' ? option[form.labelKey || 'label'] : option }}
            </el-radio>
          </el-radio-group>
          <el-checkbox-group v-else-if="form.itemType === 'checkbox'" v-bind="form || {}" v-model="params[form.modelValue]">
            <el-checkbox
              v-for="option in form.options"
              :key="option[form.valueKey || 'value']"
              :label="typeof option === 'object' ? option[form.valueKey || 'value'] : option"
            >
              {{ typeof option === 'object' ? option[form.labelKey || 'label'] : option }}
            </el-checkbox>
          </el-checkbox-group>
          <el-cascader v-else-if="form.itemType === 'cascader'" v-bind="form || {}" v-model="params[form.modelValue]">
          </el-cascader>
          <el-date-picker
            v-else-if="form.itemType === 'date'"
            v-bind="form || {}"
            type="date"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            v-model="params[form.modelValue]"
          />
          <el-date-picker
            v-else-if="form.itemType === 'datetime'"
            v-bind="form || {}"
            type="datetime"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
            v-model="params[form.modelValue]"
          />
          <el-date-picker
            v-else-if="form.itemType === 'daterange'"
            v-bind="form || {}"
            v-model="params[form.modelValue]"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="yyyy-MM-dd"
            value-format="yyyy-MM-dd"
            @change="(date) => changeDate(date, form.prop[0], form.prop[1])"
          />
          <el-date-picker
            v-else-if="form.itemType === 'datetimerange'"
            v-bind="form || {}"
            type="datetimerange"
            v-model="params[form.modelValue]"
            @change="(date) => changeDate(date, form.prop[0], form.prop[1])"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="yyyy-MM-dd HH:mm:ss"
            value-format="yyyy-MM-dd HH:mm:ss"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script>
import { formProps } from './props';

export default {
  name: 'SubmitForm',
  props: formProps,
  emits: ['input'],
  data() {
    return {};
  },
  computed: {
    params: {
      get() {
        return this.value || {};
      },
      set(value) {
        this.$emit('input', value);
      }
    }
  },
  methods: {
    changeDate(date, startDate, endDate) {
      if (Array.isArray(date)) {
        this.params[startDate] = date[0];
        this.params[endDate] = date[1];
      } else {
        this.params[startDate] = '';
        this.params[endDate] = '';
      }
    },
    clearValidate() {
      this.$refs.submitForm.clearValidate();
    },
    validate() {
      return new Promise((resolve, reject) => {
        this.$refs.submitForm.validate((valid) => {
          if (valid) {
            resolve();
          } else {
            reject();
          }
        });
      });
    }
  }
};
</script>

<style lang="scss" scoped>
.el-select,
.el-date-editor {
  width: 100% !important;
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

</style>
