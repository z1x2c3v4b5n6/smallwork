<template>
  <div class="plan-page">
    <el-card class="plan-card" shadow="hover">
      <template #header>
        <div class="plan-card__header">
          <div>
            <h2>我的志愿方案</h2>
            <p>在检索与推荐页面挑选志愿后，可快速归档到不同方案中便于对比。</p>
          </div>
          <div class="plan-card__header-actions">
            <el-input v-model="planName" placeholder="输入方案名称" style="width: 220px" />
            <el-button type="primary" :loading="creating" @click="create">创建方案</el-button>
            <el-button @click="goSearch">去检索添加志愿</el-button>
          </div>
        </div>
      </template>
      <el-table :data="plans" stripe empty-text="暂未创建方案" style="width: 100%">
        <el-table-column prop="name" label="方案名称" min-width="160" />
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="openPlan(row)">查看志愿</el-button>
            <el-button link type="danger" @click="removePlan(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="drawer" :title="currentPlan?.name || '志愿列表'" size="60%">
      <el-alert type="info" show-icon class="plan-tip" :closable="false">
        请在“志愿检索”或“位次推荐”页面选择心仪志愿后，点击“加入方案”按钮即可同步到此处。
      </el-alert>
      <el-table :data="planItems" v-loading="itemsLoading" empty-text="尚未添加志愿">
        <el-table-column prop="orderNo" label="顺序" width="80" />
        <el-table-column label="院校/专业">
          <template #default="{ row }">
            <div class="plan-item__title">{{ row.universityName }}</div>
            <div class="plan-item__subtitle">{{ row.majorName }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="batch" label="批次" width="140" />
        <el-table-column prop="latestMinRank" label="近一年最低位次" width="160">
          <template #default="{ row }">
            {{ row.latestMinRank ? row.latestMinRank.toLocaleString() : '--' }}
          </template>
        </el-table-column>
        <el-table-column label="风险评估" width="140">
          <template #default="{ row }">
            <el-tag :type="row.riskType" effect="dark" round>{{ row.riskLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="danger" @click="deleteItem(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPlans, createPlan, deletePlan, listPlanItems, deletePlanItem } from '../../api/plan'

const router = useRouter()

const plans = ref<any[]>([])
const planName = ref('')
const creating = ref(false)
const drawer = ref(false)
const planItems = ref<any[]>([])
const currentPlan = ref<any>(null)
const itemsLoading = ref(false)

const loadPlans = async () => {
  const { data } = await listPlans()
  plans.value = data
}

const create = async () => {
  if (!planName.value) {
    ElMessage.warning('请输入方案名称')
    return
  }
  creating.value = true
  try {
    await createPlan({ name: planName.value })
    ElMessage.success('创建成功')
    planName.value = ''
    loadPlans()
  } finally {
    creating.value = false
  }
}

const removePlan = (row: any) => {
  ElMessageBox.confirm(`确定删除方案「${row.name}」吗？`, '提示', { type: 'warning' }).then(async () => {
    await deletePlan(row.id)
    ElMessage.success('删除成功')
    if (currentPlan.value?.id === row.id) {
      drawer.value = false
      planItems.value = []
    }
    loadPlans()
  })
}

const openPlan = async (row: any) => {
  currentPlan.value = row
  drawer.value = true
  await loadPlanItems()
}

const loadPlanItems = async () => {
  if (!currentPlan.value) return
  itemsLoading.value = true
  try {
    const { data } = await listPlanItems(currentPlan.value.id)
    planItems.value = data
  } finally {
    itemsLoading.value = false
  }
}

const deleteItem = async (row: any) => {
  if (!currentPlan.value) return
  await deletePlanItem(currentPlan.value.id, row.id)
  ElMessage.success('已移除该志愿')
  loadPlanItems()
}

const goSearch = () => router.push('/student/search')

onMounted(loadPlans)
</script>

<style scoped>
.plan-page {
  padding: 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.plan-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.plan-card__header h2 {
  margin: 0 0 4px;
}

.plan-card__header p {
  margin: 0;
  color: #6b7280;
}

.plan-card__header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.plan-tip {
  margin-bottom: 16px;
}

.plan-item__title {
  font-weight: 600;
  color: #1f2937;
}

.plan-item__subtitle {
  color: #6b7280;
  font-size: 13px;
}
</style>
