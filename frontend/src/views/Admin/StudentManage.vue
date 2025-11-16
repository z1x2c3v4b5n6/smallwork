<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>学生档案管理</span>
          <el-button type="primary" @click="loadStudents">刷新列表</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="filters" label-width="80px" class="filter-form">
        <el-form-item label="用户名">
          <el-input v-model="filters.username" placeholder="用户名" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="filters.realName" placeholder="姓名" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="filters.province" placeholder="省份" />
        </el-form-item>
        <el-form-item label="分数区间">
          <div class="range-inputs">
            <el-input-number v-model="filters.minScore" :min="0" :max="750" :controls="false" placeholder="最低分" />
            <span>~</span>
            <el-input-number v-model="filters.maxScore" :min="0" :max="750" :controls="false" placeholder="最高分" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadStudents">查询</el-button>
          <el-button @click="resetFilters">清空</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="students" v-loading="loading" style="width: 100%">
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="province" label="省份" width="100" />
        <el-table-column prop="score" label="总分" width="100" />
        <el-table-column prop="rank" label="位次" width="120" />
        <el-table-column prop="subjects" label="选考科目" min-width="140" />
        <el-table-column prop="targetMajorType" label="目标专业" min-width="160" />
        <el-table-column label="启用" width="100">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled" @change="(val) => handleStatusChange(row, val)" />
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最近登录" min-width="180" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑档案</el-button>
            <el-button link type="warning" @click="promptResetPassword(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="drawer" :title="`编辑档案 · ${currentStudent?.realName || ''}`" size="40%">
      <el-form label-position="top" :model="profileForm" class="profile-form">
        <div class="form-row">
          <el-form-item label="姓名">
            <el-input v-model="profileForm.realName" />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="省份">
            <el-input v-model="profileForm.province" />
          </el-form-item>
          <el-form-item label="选考科目">
            <el-input v-model="profileForm.subjects" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="总分">
            <el-input-number v-model="profileForm.score" :min="0" :max="750" :controls="false" />
          </el-form-item>
          <el-form-item label="位次">
            <el-input-number v-model="profileForm.rank" :min="1" :controls="false" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="一模分">
            <el-input-number v-model="profileForm.firstMockScore" :min="0" :max="750" :controls="false" />
          </el-form-item>
          <el-form-item label="一模位次">
            <el-input-number v-model="profileForm.firstMockRank" :min="1" :controls="false" />
          </el-form-item>
        </div>
        <div class="form-row">
          <el-form-item label="二模分">
            <el-input-number v-model="profileForm.secondMockScore" :min="0" :max="750" :controls="false" />
          </el-form-item>
          <el-form-item label="二模位次">
            <el-input-number v-model="profileForm.secondMockRank" :min="1" :controls="false" />
          </el-form-item>
        </div>
        <el-form-item label="目标专业">
          <el-input v-model="profileForm.targetMajorType" type="textarea" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submitProfile">保存</el-button>
        </el-form-item>
      </el-form>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  fetchStudents,
  updateStudentProfile,
  resetStudentPassword,
  updateStudentStatus
} from '../../api/admin'

const filters = reactive<{ username?: string; realName?: string; province?: string; minScore?: number | null; maxScore?: number | null }>(
  {
    username: '',
    realName: '',
    province: '',
    minScore: null,
    maxScore: null
  }
)

const students = ref<any[]>([])
const loading = ref(false)
const drawer = ref(false)
const currentStudent = ref<any>(null)
const defaultProfileForm = () => ({
  realName: '',
  nickname: '',
  province: '',
  subjects: '',
  score: null,
  rank: null,
  firstMockScore: null,
  firstMockRank: null,
  secondMockScore: null,
  secondMockRank: null,
  targetMajorType: ''
})

const profileForm = reactive<any>(defaultProfileForm())
const saving = ref(false)

const loadStudents = async () => {
  loading.value = true
  try {
    const cleanFilters = { ...filters }
    if (!cleanFilters.minScore) delete cleanFilters.minScore
    if (!cleanFilters.maxScore) delete cleanFilters.maxScore
    const { data } = await fetchStudents(cleanFilters)
    students.value = data
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '加载学生列表失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.username = ''
  filters.realName = ''
  filters.province = ''
  filters.minScore = null
  filters.maxScore = null
  loadStudents()
}

const openEdit = (row: any) => {
  currentStudent.value = row
  drawer.value = true
  Object.assign(profileForm, defaultProfileForm(), row)
}

const submitProfile = async () => {
  if (!currentStudent.value) return
  saving.value = true
  try {
    await updateStudentProfile(currentStudent.value.userId, profileForm)
    ElMessage.success('档案已更新')
    drawer.value = false
    loadStudents()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const promptResetPassword = (row: any) => {
  ElMessageBox.prompt(`为学生 ${row.username} 设置新密码`, '重置密码', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '默认为 123456'
  })
    .then(async ({ value }) => {
      await resetStudentPassword(row.userId, { newPassword: value })
      ElMessage.success('密码已重置')
    })
    .catch(() => {})
}

const handleStatusChange = async (row: any, value: boolean) => {
  try {
    await updateStudentStatus(row.userId, { enabled: value })
    ElMessage.success('状态已更新')
    row.enabled = value
  } catch (err: any) {
    row.enabled = !value
    ElMessage.error(err?.response?.data?.message || '更新状态失败')
  }
}

onMounted(loadStudents)

watch(drawer, (visible) => {
  if (!visible) {
    currentStudent.value = null
    Object.assign(profileForm, defaultProfileForm())
  }
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 16px;
}

.range-inputs {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-inputs span {
  color: #6b7280;
}

.profile-form .form-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.profile-form :deep(.el-input-number) {
  width: 100%;
}
</style>
