<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header">
          <span>录取数据管理</span>
          <el-button @click="goImport">Excel 导入</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="filters" class="filters">
        <el-form-item label="院校ID">
          <el-input v-model="filters.universityId" placeholder="输入院校ID" />
        </el-form-item>
        <el-form-item label="专业ID">
          <el-input v-model="filters.majorId" placeholder="输入专业ID" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="records">
        <el-table-column prop="universityId" label="院校ID" />
        <el-table-column prop="majorId" label="专业ID" />
        <el-table-column prop="year" label="年份" width="100" />
        <el-table-column prop="batch" label="批次" width="120" />
        <el-table-column prop="minScore" label="最低分" />
        <el-table-column prop="minRank" label="最低位次" />
        <el-table-column prop="avgScore" label="平均分" />
        <el-table-column prop="avgRank" label="平均位次" />
        <el-table-column prop="admitCount" label="录取人数" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="编辑录取数据" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="最低分">
          <el-input v-model.number="form.minScore" type="number" />
        </el-form-item>
        <el-form-item label="最低位次">
          <el-input v-model.number="form.minRank" type="number" />
        </el-form-item>
        <el-form-item label="平均分">
          <el-input v-model.number="form.avgScore" type="number" />
        </el-form-item>
        <el-form-item label="平均位次">
          <el-input v-model.number="form.avgRank" type="number" />
        </el-form-item>
        <el-form-item label="录取人数">
          <el-input v-model.number="form.admitCount" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAdmissions, updateAdmission, deleteAdmission } from '../../api/admin'

const router = useRouter()

const filters = reactive({ universityId: '', majorId: '' })
const records = ref<any[]>([])
const dialogVisible = ref(false)
const form = reactive<any>({})

const loadData = async () => {
  const { data } = await listAdmissions(filters)
  records.value = data
}

const openEdit = (row: any) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const save = async () => {
  await updateAdmission(form.id, form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const remove = (row: any) => {
  ElMessageBox.confirm('确定删除该条记录吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteAdmission(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const goImport = () => router.push('/admin/import')

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
