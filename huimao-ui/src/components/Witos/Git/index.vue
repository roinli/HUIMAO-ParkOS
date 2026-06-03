<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'huimaoGit',
  data() {
    return {
      url: 'https://github.com/roinli/HUIMAO-ParkOS.git'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>
