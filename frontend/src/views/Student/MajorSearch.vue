<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header">
          <span>志愿检索</span>
          <div>
            <el-button type="primary" @click="goRecommend">智能推荐</el-button>
            <el-button @click="goPlans">我的方案</el-button>
          </div>
        </div>
      </template>
      <el-form :inline="true" :model="filters" class="filters">
        <el-form-item label="院校名称">
          <el-input v-model="filters.universityName" placeholder="输入院校" />
        </el-form-item>
        <el-form-item label="专业名称">
          <el-input v-model="filters.majorName" placeholder="输入专业" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="filters.province" placeholder="输入省份" />
        </el-form-item>
        <el-form-item label="批次">
          <el-input v-model="filters.batch" placeholder="如本科一批" />
        </el-form-item>
        <el-form-item label="专业类别">
          <el-input v-model="filters.category" placeholder="如工学" />
        </el-form-item>
        <el-form-item label="专业层次">
          <el-input v-model="filters.level" placeholder="如重点" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="universityName" label="院校" width="160" />
        <el-table-column prop="province" label="省份" width="90" />
        <el-table-column prop="majorName" label="专业" />
        <el-table-column prop="category" label="专业类别" width="120" />
        <el-table-column prop="subjectReq" label="选考要求" width="120" />
        <el-table-column prop="level" label="专业层次" width="120" />
        <el-table-column prop="batch" label="批次" width="120" />
        <el-table-column prop="latestMinScore" label="近一年最低分" width="120" />
        <el-table-column prop="latestMinRank" label="近一年最低位次" width="140" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="历年录取情况" width="60%">
      <el-table :data="detailData">
        <el-table-column prop="year" label="年份" width="100" />
        <el-table-column prop="batch" label="批次" width="120" />
        <el-table-column prop="minScore" label="最低分" />
        <el-table-column prop="minRank" label="最低位次" />
        <el-table-column prop="avgScore" label="平均分" />
        <el-table-column prop="avgRank" label="平均位次" />
        <el-table-column prop="admitCount" label="录取人数" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { searchMajors, fetchMajorDetail } from '../../api/student'
import { ElMessage } from 'element-plus'

const router = useRouter()

const filters = reactive({
  universityName: '',
  majorName: '',
  province: '',
  batch: '',
  category: '',
  level: ''
})

const tableData = ref<any[]>([])
const detailData = ref<any[]>([])
const detailVisible = ref(false)
const currentRow = ref<any>(null)

const loadData = async () => {
  try {
    const { data } = await searchMajors(filters)
    tableData.value = data
  } catch (err) {
    ElMessage.error('查询失败')
  }
}

const viewDetail = async (row: any) => {
  currentRow.value = row
  try {
    const { data } = await fetchMajorDetail(row.universityId, row.majorId)
    detailData.value = data
    detailVisible.value = true
  } catch (err) {
    ElMessage.error('加载详情失败')
  }
}

const goRecommend = () => router.push('/student/recommend')
const goPlans = () => router.push('/student/plans')

onMounted(loadData)
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filters {
  margin-bottom: 16px;
}
</style>
