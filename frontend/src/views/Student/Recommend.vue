<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>按位次三段推荐</span>
      </template>
      <el-alert
        v-if="!profile?.rank"
        type="warning"
        show-icon
        class="profile-alert"
        :closable="false"
      >
        <template #title>建议先在个人中心完善当前位次与省份信息</template>
        <template #default>
          <el-button link type="primary" @click="goProfile">立即前往</el-button>
        </template>
      </el-alert>
      <el-form :inline="true" :model="form" class="filters">
        <el-form-item label="当前位次">
          <el-input-number v-model="form.currentRank" :min="1" />
        </el-form-item>
        <el-form-item label="选考科目">
          <el-input v-model="form.subjects" placeholder="例如 物理 化学" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" placeholder="例如 浙江" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadData">开始推荐</el-button>
          <el-button @click="goSearch">返回检索</el-button>
        </el-form-item>
      </el-form>
      <el-tabs v-model="activeTab">
        <el-tab-pane
          v-for="segment in segments"
          :key="segment.segment"
          :label="segment.segment"
          :name="segment.segment"
        >
          <el-table :data="segment.items" style="width: 100%">
            <el-table-column prop="universityName" label="院校" />
            <el-table-column prop="majorName" label="专业" />
            <el-table-column prop="batch" label="批次" width="120" />
            <el-table-column prop="latestMinRank" label="最低位次" width="140" />
            <el-table-column prop="latestMinScore" label="最低分" width="120" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { recommend, fetchStudentProfile } from '../../api/student'
import { ElMessage } from 'element-plus'

const router = useRouter()

const form = reactive({
  currentRank: null as number | null,
  subjects: '',
  province: ''
})

const segments = ref<any[]>([])
const activeTab = ref('冲刺')
const loading = ref(false)
const profile = ref<any | null>(null)
const profileLoading = ref(false)

const loadData = async () => {
  if (!form.currentRank) {
    ElMessage.warning('请输入位次')
    return
  }
  loading.value = true
  try {
    const { data } = await recommend(form)
    segments.value = data
    if (data.length > 0) {
      activeTab.value = data[0].segment
    }
  } catch (err) {
    ElMessage.error('推荐失败')
  } finally {
    loading.value = false
  }
}

const loadProfile = async () => {
  profileLoading.value = true
  try {
    const { data } = await fetchStudentProfile()
    profile.value = data
    if (data) {
      if (data.rank && !form.currentRank) {
        form.currentRank = data.rank
      }
      if (data.subjects && !form.subjects) {
        form.subjects = data.subjects
      }
      if (data.province && !form.province) {
        form.province = data.province
      }
    }
    if (form.currentRank) {
      loadData()
    }
  } catch (err) {
    console.warn('加载档案失败', err)
  } finally {
    profileLoading.value = false
  }
}

const goSearch = () => router.push('/student/search')
const goProfile = () => router.push('/student/profile')

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.filters {
  margin-bottom: 16px;
}

.profile-alert {
  margin-bottom: 16px;
}
</style>
