<template>
  <el-drawer direction="rtl" :wrapperClosable="false" v-bind="$attrs" size="70%">
    <div class="drawer-container">
      <slot></slot>
      <div class="drawer-container-btn">
        <slot name="footer"></slot>
      </div>
    </div>
  </el-drawer>
</template>

<script>
export default {
  name: 'Drawer',
  props: {},
  data() {
    return {};
  },
  methods: {}
};
</script>

<style scoped>
.drawer-container {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-right: 20px;
  padding-bottom: 20px;
  height: 100%;
}
.drawer-container-btn {
  text-align: right;
}
</style>
