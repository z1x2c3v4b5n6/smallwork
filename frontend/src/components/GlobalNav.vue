<template>
  <header class="global-nav">
    <div class="global-nav__inner">
      <div class="global-nav__brand" @click="goHome">
        <span class="global-nav__logo">🎓</span>
        <div class="global-nav__title">
          <strong>高考志愿填报助手</strong>
          <small>Intelligent Admission Planning</small>
        </div>
      </div>
      <nav class="global-nav__menu" aria-label="主导航">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="global-nav__link"
          :class="{ 'is-active': isActive(item.path) }"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
      <div class="global-nav__user" v-if="auth.token">
        <div class="global-nav__user-info">
          <span class="global-nav__user-name">{{ auth.username || '访客' }}</span>
          <span class="global-nav__user-role">{{ roleLabel }}</span>
        </div>
        <el-button text type="primary" @click="handleLogout">退出登录</el-button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const baseNavItems = [
  { label: '个人中心', path: '/student/profile', roles: ['ROLE_STUDENT', 'ROLE_ADMIN'] },
  { label: '志愿检索', path: '/student/search', roles: ['ROLE_STUDENT', 'ROLE_ADMIN'] },
  { label: '位次推荐', path: '/student/recommend', roles: ['ROLE_STUDENT', 'ROLE_ADMIN'] },
  { label: '志愿方案', path: '/student/plans', roles: ['ROLE_STUDENT', 'ROLE_ADMIN'] },
  { label: '学生管理', path: '/admin/students', roles: ['ROLE_ADMIN'] },
  { label: '推荐规则', path: '/admin/rules', roles: ['ROLE_ADMIN'] },
  { label: '数据导入（管理员）', path: '/admin/import', roles: ['ROLE_ADMIN'] }
]

const navItems = computed(() => {
  const role = auth.role || 'ROLE_STUDENT'
  return baseNavItems.filter((item) => !item.roles || item.roles.includes(role))
})

const roleLabel = computed(() => {
  if (auth.role === 'ROLE_ADMIN') return '管理员'
  if (auth.role === 'ROLE_STUDENT') return '学生用户'
  return '未登录'
})

const isActive = (path: string) => route.path.startsWith(path)

const handleLogout = () => {
  auth.logout()
  router.push('/login')
}

const goHome = () => {
  if (auth.role === 'ROLE_ADMIN') {
    router.push('/admin/universities')
  } else {
    router.push('/student/search')
  }
}
</script>

<style scoped>
.global-nav {
  background: #0b1f66;
  color: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 4px 16px rgba(15, 33, 90, 0.25);
}

.global-nav__inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 12px 24px;
  display: flex;
  align-items: center;
  gap: 32px;
}

.global-nav__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  white-space: nowrap;
}

.global-nav__logo {
  font-size: 28px;
}

.global-nav__title {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.global-nav__title strong {
  font-size: 16px;
}

.global-nav__title small {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.global-nav__menu {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  flex-wrap: wrap;
}

.global-nav__link {
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  padding: 6px 12px;
  border-radius: 999px;
  transition: background 0.2s ease, color 0.2s ease;
}

.global-nav__link:hover {
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
}

.global-nav__link.is-active {
  background: #fff;
  color: #0b1f66;
  font-weight: 600;
}

.global-nav__user {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(255, 255, 255, 0.08);
  padding: 8px 12px;
  border-radius: 12px;
}

.global-nav__user-info {
  display: flex;
  flex-direction: column;
  font-size: 13px;
  line-height: 1.2;
}

.global-nav__user-name {
  font-weight: 600;
}

.global-nav__user-role {
  color: rgba(255, 255, 255, 0.7);
}

@media (max-width: 768px) {
  .global-nav__inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .global-nav__menu {
    width: 100%;
    justify-content: flex-start;
  }

  .global-nav__user {
    align-self: stretch;
  }
}
</style>
