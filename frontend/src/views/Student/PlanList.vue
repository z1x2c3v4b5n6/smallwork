<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header">
          <span>志愿方案管理</span>
          <el-button type="primary" @click="goSearch">返回检索</el-button>
        </div>
      </template>
      <div class="actions">
        <el-input v-model="planName" placeholder="新方案名称" style="width: 240px" />
        <el-button type="primary" @click="create">创建方案</el-button>
      </div>
      <el-table :data="plans" style="width: 100%">
        <el-table-column prop="name" label="方案名称" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button link type="primary" @click="openPlan(row)">查看志愿</el-button>
            <el-button link type="danger" @click="removePlan(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="drawer" :title="currentPlan?.name" size="50%">
      <div class="drawer-actions">
        <el-input v-model.number="newItem.orderNo" placeholder="顺序" type="number" style="width: 90px" />
        <el-input v-model="newItem.universityId" placeholder="院校ID" style="width: 120px" />
        <el-input v-model="newItem.majorId" placeholder="专业ID" style="width: 120px" />
        <el-input v-model="newItem.batch" placeholder="批次" style="width: 120px" />
        <el-button type="primary" @click="addItem">添加志愿</el-button>
      </div>
      <el-table :data="planItems" style="width: 100%">
        <el-table-column prop="orderNo" label="顺序" width="80" />
        <el-table-column prop="universityId" label="院校ID" />
        <el-table-column prop="majorId" label="专业ID" />
        <el-table-column prop="batch" label="批次" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="danger" @click="deleteItem(row)">删除</el-button>
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
import { listPlans, createPlan, deletePlan, listPlanItems, addPlanItem, deletePlanItem } from '../../api/plan'

const router = useRouter()

const plans = ref<any[]>([])
const planName = ref('')
const drawer = ref(false)
const planItems = ref<any[]>([])
const currentPlan = ref<any>(null)
const newItem = ref({
  orderNo: 1,
  universityId: '',
  majorId: '',
  batch: ''
})

const loadPlans = async () => {
  const { data } = await listPlans()
  plans.value = data
}

const create = async () => {
  if (!planName.value) {
    ElMessage.warning('请输入方案名称')
    return
  }
  await createPlan({ name: planName.value })
  ElMessage.success('创建成功')
  planName.value = ''
  loadPlans()
}

const removePlan = (row: any) => {
  ElMessageBox.confirm(`确定删除方案「${row.name}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deletePlan(row.id)
    ElMessage.success('删除成功')
    loadPlans()
  })
}

const openPlan = async (row: any) => {
  currentPlan.value = row
  drawer.value = true
  const { data } = await listPlanItems(row.id)
  planItems.value = data
}

const addItem = async () => {
  if (!currentPlan.value) return
  await addPlanItem(currentPlan.value.id, newItem.value)
  ElMessage.success('添加成功')
  const { data } = await listPlanItems(currentPlan.value.id)
  planItems.value = data
}

const deleteItem = async (row: any) => {
  if (!currentPlan.value) return
  await deletePlanItem(currentPlan.value.id, row.id)
  ElMessage.success('删除成功')
  planItems.value = planItems.value.filter((item) => item.id !== row.id)
}

const goSearch = () => router.push('/student/search')

onMounted(loadPlans)
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

.actions {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}

.drawer-actions {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
}
</style>
