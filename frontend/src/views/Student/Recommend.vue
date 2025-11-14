<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>按位次三段推荐</span>
      </template>
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
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { recommend } from '../../api/student'
import { ElMessage } from 'element-plus'

const router = useRouter()

const form = reactive({
  currentRank: 5000,
  subjects: '物理 化学',
  province: '浙江'
})

const segments = ref<any[]>([])
const activeTab = ref('冲刺')
const loading = ref(false)

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

const goSearch = () => router.push('/student/search')
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.filters {
  margin-bottom: 16px;
}
</style>
