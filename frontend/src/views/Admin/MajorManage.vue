<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header">
          <span>专业管理</span>
          <div class="header-actions">
            <el-button @click="goUniversities">院校管理</el-button>
            <el-button @click="goAdmissions">录取数据</el-button>
          </div>
        </div>
      </template>
      <div class="actions">
        <el-button type="primary" @click="openCreate">新增专业</el-button>
      </div>
      <el-table :data="majors">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="discipline" label="学科" />
        <el-table-column prop="subjectReq" label="选考要求" />
        <el-table-column prop="level" label="层次" width="120" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑专业' : '新增专业'" width="500px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类别">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="学科">
          <el-input v-model="form.discipline" />
        </el-form-item>
        <el-form-item label="选考要求">
          <el-input v-model="form.subjectReq" />
        </el-form-item>
        <el-form-item label="层次">
          <el-input v-model="form.level" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
import { listMajors, createMajor, updateMajor, deleteMajor } from '../../api/admin'

const router = useRouter()

const majors = ref<any[]>([])
const dialogVisible = ref(false)
const form = reactive<any>({})

const loadData = async () => {
  const { data } = await listMajors()
  majors.value = data
}

const openCreate = () => {
  Object.assign(form, { id: null, name: '', category: '', discipline: '', subjectReq: '', level: '', remark: '' })
  dialogVisible.value = true
}

const openEdit = (row: any) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const save = async () => {
  if (!form.name) {
    ElMessage.warning('请输入名称')
    return
  }
  if (form.id) {
    await updateMajor(form.id, form)
  } else {
    await createMajor(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const remove = (row: any) => {
  ElMessageBox.confirm(`确定删除专业「${row.name}」吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    await deleteMajor(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const goUniversities = () => router.push('/admin/universities')
const goAdmissions = () => router.push('/admin/admissions')

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

.header-actions {
  display: flex;
  gap: 12px;
}

.actions {
  margin-bottom: 16px;
}
</style>
