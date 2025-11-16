<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>系统公告管理</span>
          <el-button type="primary" @click="openDialog()">发布公告</el-button>
        </div>
      </template>
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable>
            <el-option label="已发布" value="published" />
            <el-option label="草稿" value="draft" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="loadData">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="items" v-loading="loading" border>
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.status === 'published' ? 'success' : 'info'" effect="dark">
              {{ row.status === 'published' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column label="置顶" width="100">
          <template #default="{ row }">
            <el-switch :model-value="row.pinned" @change="(val) => changePinned(row, val)" />
          </template>
        </el-table-column>
        <el-table-column prop="audience" label="面向角色" width="140" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 'published' ? 'warning' : 'success'"
              @click="togglePublish(row)"
            >
              {{ row.status === 'published' ? '设为草稿' : '立即发布' }}
            </el-button>
            <el-button link type="danger" @click="removeItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑公告' : '发布公告'" width="520px">
      <el-form label-width="80px" :model="form">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input type="textarea" v-model="form.content" :rows="4" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="草稿" value="draft" />
            <el-option label="已发布" value="published" />
          </el-select>
        </el-form-item>
        <el-form-item label="面向角色">
          <el-input v-model="form.audience" placeholder="例如 学生 / 管理员" />
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.pinned" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAnnouncementsAdmin, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '../../api/admin'

const filters = reactive({ status: '' })
const items = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const form = reactive({ title: '', content: '', status: 'draft', audience: '学生', pinned: false })

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, any> = {}
    if (filters.status) params.status = filters.status
    const { data } = await fetchAnnouncementsAdmin(params)
    items.value = data
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '加载公告失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.status = ''
  loadData()
}

const openDialog = (row?: any) => {
  dialogVisible.value = true
  if (row) {
    editingId.value = row.id
    Object.assign(form, row)
  } else {
    editingId.value = null
    Object.assign(form, { title: '', content: '', status: 'draft', audience: '学生', pinned: false })
  }
}

const submit = async () => {
  if (!form.title) {
    ElMessage.warning('请输入公告标题')
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateAnnouncement(editingId.value, form)
    } else {
      await createAnnouncement(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const removeItem = (row: any) => {
  ElMessageBox.confirm(`确认删除公告「${row.title}」吗？`, '提示', { type: 'warning' }).then(async () => {
    await deleteAnnouncement(row.id)
    ElMessage.success('已删除')
    loadData()
  })
}

const togglePublish = async (row: any) => {
  const nextStatus = row.status === 'published' ? 'draft' : 'published'
  const success = await patchAnnouncement(row, { status: nextStatus })
  if (success) {
    ElMessage.success(nextStatus === 'published' ? '已发布' : '已设为草稿')
    loadData()
  }
}

const patchAnnouncement = async (row: any, payload: Record<string, any>) => {
  try {
    await updateAnnouncement(row.id, { ...row, ...payload })
    Object.assign(row, payload)
    return true
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '更新失败')
    return false
  }
}

const changePinned = async (row: any, value: boolean) => {
  const success = await patchAnnouncement(row, { pinned: value })
  if (!success) {
    row.pinned = !value
  }
}

onMounted(loadData)
</script>

<style scoped>
.page-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-form {
  margin-bottom: 16px;
}
</style>
