<template>
  <div class="app-shell">
    <GlobalNav v-if="showNav" />
    <main :class="['app-shell__main', { 'app-shell__main--with-nav': showNav }]"><router-view /></main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import GlobalNav from './components/GlobalNav.vue'

const route = useRoute()
const hideNavRoutes = ['/login']

const showNav = computed(() => !hideNavRoutes.includes(route.path))
</script>

<style>
body {
  margin: 0;
  font-family: 'PingFang SC', 'Microsoft Yahei', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background: #f5f7fb;
}

.app-shell__main {
  min-height: 100vh;
}

.app-shell__main--with-nav {
  padding-top: 24px;
}

@media (max-width: 768px) {
  .app-shell__main--with-nav {
    padding-top: 16px;
  }
}
</style>
