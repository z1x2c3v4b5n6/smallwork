<template>
  <div class="login-wrapper">
    <el-card class="login-card">
      <h2 class="title">高考志愿填报辅助系统</h2>
      <el-form :model="form" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../api/auth'
import { useAuthStore } from '../store/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()

const form = reactive({
  username: 'student',
  password: '123456'
})
const loading = ref(false)

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const { data } = await login(form)
    auth.setUser({ username: data.username, role: data.role, token: data.token })
    ElMessage.success('登录成功')
    if (data.role === 'ROLE_ADMIN') {
      router.push('/admin/universities')
    } else {
      router.push('/student/search')
    }
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #7f7fd5, #86a8e7, #91eae4);
}

.login-card {
  width: 360px;
}

.title {
  text-align: center;
  margin-bottom: 24px;
}
</style>
