<template>
  <div class="table-container" ref="tableContainerRef">
    <div class="table-header">
      <!-- <template v-if="$slots.title"> -->
        <slot name="title"></slot>
      <!-- </template> -->
      <!-- <span v-else-if="title">{{ title }}</span> -->
      <div>
        <slot name="toolbar"></slot>
      </div>
    </div>
    <el-table
      :data="data"
      v-bind="tableProps"
      style="width: 100%"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      header-row-class-name="table-custom-header" 
      class="table-container__table"
    >
      <el-table-column v-if="selectable" type="selection" width="50" />
      <el-table-column v-if="hasIndex" type="index" label="序号" align="center" width="80" />
      <template v-for="col in columns">
        <el-table-column v-if="!col.slotName" :key="col.prop" show-overflow-tooltip v-bind="col" />
        <el-table-column v-else :key="col.prop || col.slotName" show-overflow-tooltip v-bind="col">
          <template slot-scope="scope">
            <slot :name="col.slotName" :scope="scope"></slot>
          </template>
          <template slot="header" v-if="col.headerSlotName">
            <slot :name="col.headerSlotName"></slot>
          </template>
        </el-table-column>
      </template>
    </el-table>
    <el-pagination
      v-if="hasPagination"
      class="table-pagination"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @size-change="onSizeChange"
      @current-change="onCurrentChange"
      :page-sizes="pageSizes"
    />
  </div>
</template>

<script>
export default {
  name: 'TableContainer',
  props: {
    title: {
      type: String,
      default: ''
    },
    columns: {
      type: Array,
      required: true
    },
    data: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    },
    selectable: {
      type: Boolean,
      default: false
    },
    tableProps: {
      type: Object,
      default: () => ({})
    },
    pageSize: {
      type: Number,
      default: 10
    },
    currentPage: {
      type: Number,
      default: 1
    },
    total: {
      type: Number,
      default: 0
    },
    pageSizes: {
      type: Array,
      default: () => [10, 20, 50, 100]
    },
    hasPagination: {
      type: Boolean,
      default: true
    },
    hasIndex: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    handleSelectionChange(val) {
      this.$emit('selection-change', val);
    },
    handleSortChange(...args) {
      this.$emit('sort-change', ...args);
    },
    onSizeChange(size) {
      this.$emit('pageChange', { size, page: this.currentPage });
    },
    onCurrentChange(page) {
      this.$emit('pageChange', { page, size: this.pageSize });
    }
  }
};
</script>

<style lang="scss" scoped>
@import "~@/assets/styles/mixin.scss";
@import "~@/assets/styles/variables.scss";
 
.table-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background-color: $--color-white;
  border-radius: $--border-radius-base;
  padding: 16px;

  .table-header {
    @include flexBetweenCenter;
    justify-content: flex-end;
  }
  ::v-deep .table-container__table {
    .el-table__body,
    .el-table__header {
      td,
      th {
        .cell {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }
}
.table-pagination {
  margin-top: 16px;
  align-self: flex-end;
}
::v-deep .table-custom-header th {
  background-color: $--background-color-base !important;
  font-weight: 500 !important;
  font-size: 12px  !important;
  color:#121c2a !important;
}

::v-deep .el-table__row .el-button--text{
  font-weight: 500 !important;
  margin-left: 12px !important;
}

::v-deep .el-table__row .el-button--text:first-child{
  margin-left: 2px !important;
}
</style>
