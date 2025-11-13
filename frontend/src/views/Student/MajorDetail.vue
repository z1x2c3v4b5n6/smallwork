<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>{{ title }}</span>
      </template>
      <el-table :data="detail">
        <el-table-column prop="year" label="年份" width="100" />
        <el-table-column prop="batch" label="批次" width="120" />
        <el-table-column prop="minScore" label="最低分" />
        <el-table-column prop="minRank" label="最低位次" />
        <el-table-column prop="avgScore" label="平均分" />
        <el-table-column prop="avgRank" label="平均位次" />
        <el-table-column prop="admitCount" label="录取人数" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchMajorDetail } from '../../api/student'
import { ElMessage } from 'element-plus'

const route = useRoute()
const detail = ref<any[]>([])
const title = ref('专业详情')

onMounted(async () => {
  const universityId = Number(route.query.universityId)
  const majorId = Number(route.query.majorId)
  title.value = `${route.query.universityName || ''} - ${route.query.majorName || ''}`
  if (universityId && majorId) {
    try {
      const { data } = await fetchMajorDetail(universityId, majorId)
      detail.value = data
    } catch (err) {
      ElMessage.error('加载详情失败')
    }
  }
})
</script>

<style scoped>
.page-container {
  padding: 24px;
}
</style>
