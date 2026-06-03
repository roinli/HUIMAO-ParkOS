<template>
  <div :class="`common-search-form ${className}`">
    <div class="search-models" :style="{ maxHeight: showMore ? '100vh' : '80px' }">
      <el-row class="search-row" type="flex" :gutter="5">
        <el-col
          v-for="(form, index) in forms.filter((f) => typeof f.visible !== 'function' || f.visible(params))"
          :key="index"
          :span="form.colSpan || 8"
        >
          <div class="custom-form-item">
            <span v-if="form.label" class="custom-form-label" :style="{ width: labelWidth + 'px' }">{{
              form.label
            }}</span>
            <template v-if="form.slotName">
              <slot :name="form.slotName" :scope="{ params, modelValue: form.modelValue }"></slot>
            </template>
            <el-input
              v-else-if="form.itemType === 'input'"
              clearable
              v-bind="form || {}"
              v-model="params[form.modelValue]"
              @keyup.enter.native="searchHandler"
              @clear="searchHandler"
            />
            <el-select
              v-else-if="form.itemType === 'select'"
              clearable
              v-bind="form || {}"
              v-model="params[form.modelValue]"
              @change="searchHandler"
            >
              <el-option
                v-for="option in form.options"
                :key="option[form.valueKey || 'value']"
                :value="typeof option === 'object' ? option[form.valueKey || 'value'] : option"
                :label="typeof option === 'object' ? option[form.labelKey || 'label'] : option"
              />
            </el-select>
            <el-date-picker
              v-else-if="form.itemType === 'date'"
              clearable
              type="date"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd"
              v-model="params[form.modelValue]"
              :picker-options="pickerSingleOptions"
              v-bind="form || {}"
              @change="searchHandler"
            />
            <el-date-picker
              v-else-if="form.itemType === 'datetime'"
              clearable
              v-bind="form || {}"
              v-model="params[form.modelValue]"
              format="yyyy-MM-dd HH:mm:ss"
              value-format="yyyy-MM-dd HH:mm:ss"
              :picker-options="pickerSingleOptions"
              type="datetime"
              @change="searchHandler"
            />
            <el-date-picker
              v-else-if="form.itemType === 'daterange'"
              clearable
              v-model="params[form.modelValue]"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="yyyy-MM-dd"
              value-format="yyyy-MM-dd"
              :picker-options="form.pickerOptions || pickerRangeOptions"
              v-bind="form || {}"
              @change="(date) => changeDate(date, form.prop)"
            />
            <el-date-picker
              v-else-if="form.itemType === 'datetimerange'"
              clearable
              type="datetimerange"
              v-model="params[form.modelValue]"
              @change="(date) => changeDate(date, form.prop)"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              format="yyyy-MM-dd HH:mm:ss"
              value-format="yyyy-MM-dd HH:mm:ss"
              :default-time="['00:00:00', '23:59:59']"
              v-bind="form || {}"
              :picker-options="form.pickerOptions || (btnHandle ? pickerIntervalOptions : pickerRangeOptions)"
            />
          </div>
        </el-col>
      </el-row>
      <div :class="`search-handle ${btnHandle ? 'search-handle-width' : ''}`" :span="5">
        <div class="btn-handle">
          <el-button :size="size" v-if="!hideResetBtn" @click="resetForm" :loading="loading">
            {{ resetBtnText }}
          </el-button>
          <el-button type="primary" :size="size" @click="searchHandler" :loading="loading">
            {{ submitBtnText }}
          </el-button>
          <slot name="btnSlot"></slot>
        </div>
        <div v-if="showMoreBtn" style="text-align: right">
          <el-button type="text" @click="lookMore">
            <div class="btn-more-box" :class="showMore ? 'show-more' : ''">
              更多查询条件
              <div class="btn-more-icon">
                <svg-icon iconClass="ant-double-left"></svg-icon>
              </div>
            </div>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { formProps } from './props';
import { pickerRangeOptions, pickerSingleOptions, pickerIntervalOptions, dateClear } from '@/utils/datePickerOption';

export default {
  name: 'SearchForm',
  props: formProps,
  emits: ['input', 'search', 'reset'],
  data() {
    return {
      // 是否显示更多查询条件
      showMore: false,
      // 原始参数，用于重置
      originParams: {},
      pickerRangeOptions,
      pickerSingleOptions,
      pickerIntervalOptions
    };
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
  mounted() {
    this.originParams = JSON.parse(JSON.stringify(this.value));
  },
  methods: {
    resetForm() {
      this.params = JSON.parse(JSON.stringify(this.originParams));
      this.$emit('reset');
    },
    changeDate(date, prop) {
      dateClear();
      if (prop && Array.isArray(prop)) {
        const [startDate, endDate] = prop;
        if (Array.isArray(date)) {
          this.params[startDate] = date[0];
          this.params[endDate] = date[1];
        } else {
          this.params[startDate] = '';
          this.params[endDate] = '';
        }
      }
      // this.searchHandler();
    },
    searchHandler() {
      this.$emit('search');
    },
    lookMore() {
      this.showMore = !this.showMore;
    }
  }
};
</script>

<style lang="scss" scoped>
@import "~@/assets/styles/mixin.scss";
@import "~@/assets/styles/variables.scss";
  
.common-search-form {
  height: 100%;
  padding: 20px;
  border-radius: $--border-radius-base;
  background-color: $--color-white;
}

.search-models {
  display: flex;
  justify-content: space-between;
  flex-wrap: nowrap;
  overflow: hidden;
  transition: all 0.3s ease-in-out;
  .search-row {
    flex-wrap: wrap;
    row-gap: 10px;
    flex: 1;
  }
  .search-handle {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    width: 20%;
    flex-shrink: 0;
    .btn-handle {
      width: 100%;
      text-align: right;
    }
    .btn-more-box {
      @include flexBetweenCenter;
      gap: 2px;
      font-size: 13px;
      .btn-more-icon {
        transform: rotate(-90deg);
        transition: all 0.3s ease-in-out;
      }
      &.show-more .btn-more-icon {
        transform: rotate(-270deg);
      }
    }
  }
  .search-handle-width {
    width: 35%;
  }
}

.balance-search {
 padding: 0;
}

::v-deep .custom-form-item {
  display: flex;
  align-items: center;
  gap: 10px;
  .custom-form-label {
    text-align: right;
    font-size: 16px;
    flex-shrink: 0;
  }
}
</style>

<style>
.common-search-form .el-select,
.common-search-form .el-date-editor {
  width: 100% !important;
}
</style>
