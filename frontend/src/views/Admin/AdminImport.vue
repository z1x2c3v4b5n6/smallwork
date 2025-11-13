<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <span>Excel 数据导入</span>
      </template>
      <el-upload
        drag
        :auto-upload="false"
        :limit="1"
        v-model:file-list="fileList"
        accept=".xlsx,.xls"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽或点击上传 Excel 文件</div>
      </el-upload>
      <div class="actions">
        <el-button type="primary" :loading="loading" @click="handleUpload">开始导入</el-button>
      </div>
      <el-alert
        title="模板说明"
        type="info"
        :closable="false"
        description="列顺序：院校名称、院校省份、院校城市、院校层次、院校类型、是否双一流、专业名称、专业类别、专业学科、选考要求、专业层次、批次、学制年限、学费、年份、最低分、最低位次、平均分、平均位次、录取人数"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { importExcel } from '../../api/admin'

const fileList = ref<any[]>([])
const loading = ref(false)

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择 Excel 文件')
    return
  }
  const file = fileList.value[0].raw as File
  loading.value = true
  try {
    const { data } = await importExcel(file)
    ElMessage.success(`导入成功，共 ${data.count} 条记录`)
    fileList.value = []
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '导入失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.actions {
  margin-top: 16px;
}
</style>
