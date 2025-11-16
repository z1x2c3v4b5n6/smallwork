<template>
  <div class="auth-wrapper">
    <el-card class="auth-card">
      <h2 class="title">创建学生账号</h2>
      <el-form :model="form" label-position="top" class="auth-form">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="用于登录的账号" />
        </el-form-item>
        <div class="auth-form__grid">
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" />
          </el-form-item>
        </div>
        <div class="auth-form__grid">
          <el-form-item label="姓名">
            <el-input v-model="form.realName" placeholder="真实姓名" />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" placeholder="展示昵称" />
          </el-form-item>
        </div>
        <div class="auth-form__grid">
          <el-form-item label="省份">
            <el-input v-model="form.province" placeholder="如 浙江" />
          </el-form-item>
          <el-form-item label="选考科目">
            <el-input v-model="form.subjects" placeholder="如 物理 化学" />
          </el-form-item>
        </div>
        <div class="auth-form__grid">
          <el-form-item label="当前分数">
            <el-input-number v-model="form.score" :min="0" :max="750" :controls="false" placeholder="总分" />
          </el-form-item>
          <el-form-item label="当前位次">
            <el-input-number v-model="form.rank" :min="1" :controls="false" placeholder="位次" />
          </el-form-item>
        </div>
        <el-form-item label="目标专业方向">
          <el-input v-model="form.targetMajorType" placeholder="如 计算机类 / 教育类" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister">立即注册</el-button>
          <el-button link type="primary" @click="goLogin">已有账号？登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/auth'
import { useAuthStore } from '../store/auth'

const router = useRouter()
const auth = useAuthStore()

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  nickname: '',
  province: '',
  subjects: '',
  score: null as number | null,
  rank: null as number | null,
  targetMajorType: ''
})

const loading = ref(false)

const handleRegister = async () => {
  if (!form.username || !form.password || !form.confirmPassword) {
    ElMessage.warning('请完整填写登录信息')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  loading.value = true
  try {
    const payload = { ...form }
    const { data } = await register(payload)
    auth.setUser({ username: data.username, role: data.role, token: data.token })
    ElMessage.success('注册成功，已自动登录')
    router.push('/student/profile')
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goLogin = () => router.push('/login')
</script>

<style scoped>
.auth-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(135deg, #e0e7ff 0%, #f8fafc 100%);
}

.auth-card {
  width: 480px;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.auth-form__grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.auth-form :deep(.el-input-number) {
  width: 100%;
}
</style>
