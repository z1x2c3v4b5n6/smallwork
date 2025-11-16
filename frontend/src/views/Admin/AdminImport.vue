<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>Excel 数据导入</span>
        </div>
      </template>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="院校 & 专业数据" name="major">
          <el-upload drag :auto-upload="false" :limit="1" v-model:file-list="majorFiles" accept=".xlsx,.xls">
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽或点击上传招生计划 Excel</div>
          </el-upload>
          <div class="actions">
            <el-button type="primary" :loading="loadingMajor" @click="handleMajorUpload">导入院校/专业数据</el-button>
          </div>
          <el-alert
            title="模板说明"
            type="info"
            :closable="false"
            description="列示例：院校名称、院校省份、院校城市、院校层次、院校类型、是否双一流、专业名称、专业类别、专业学科、选考要求、专业层次、批次、学制、学费、年份、最低分、最低位次、平均分、平均位次、招生计划"
          />
        </el-tab-pane>
        <el-tab-pane label="学生档案导入" name="student">
          <el-upload drag :auto-upload="false" :limit="1" v-model:file-list="studentFiles" accept=".xlsx,.xls">
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽或点击上传学生档案 Excel</div>
          </el-upload>
          <el-form label-position="top" class="strategy-form">
            <el-form-item label="已有账号处理方式">
              <el-radio-group v-model="studentStrategy">
                <el-radio-button label="overwrite">覆盖全部档案</el-radio-button>
                <el-radio-button label="scores">仅更新成绩</el-radio-button>
                <el-radio-button label="skip">跳过已存在</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-form>
          <div class="actions">
            <el-button type="primary" :loading="loadingStudent" @click="handleStudentUpload">导入学生档案</el-button>
          </div>
          <el-alert
            title="模板说明"
            type="info"
            :closable="false"
            description="支持列：用户名、姓名、昵称、省份、总分、位次、一模分/位次、二模分/位次、选考科目、目标专业、初始密码。系统将自动识别表头匹配导入。"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { importMajorExcel, importStudentExcel } from '../../api/admin'

const activeTab = ref('major')
const majorFiles = ref<any[]>([])
const studentFiles = ref<any[]>([])
const studentStrategy = ref('overwrite')
const loadingMajor = ref(false)
const loadingStudent = ref(false)

const handleMajorUpload = async () => {
  if (!majorFiles.value.length) {
    ElMessage.warning('请选择院校/专业 Excel 文件')
    return
  }
  const file = majorFiles.value[0].raw as File
  loadingMajor.value = true
  try {
    const { data } = await importMajorExcel(file)
    ElMessage.success(`导入成功，共 ${data.count} 条记录`)
    majorFiles.value = []
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '导入失败')
  } finally {
    loadingMajor.value = false
  }
}

const handleStudentUpload = async () => {
  if (!studentFiles.value.length) {
    ElMessage.warning('请选择学生档案 Excel 文件')
    return
  }
  const file = studentFiles.value[0].raw as File
  loadingStudent.value = true
  try {
    const { data } = await importStudentExcel(file, studentStrategy.value)
    ElMessage.success(`导入成功，共 ${data.count} 名学生`)
    studentFiles.value = []
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '导入失败')
  } finally {
    loadingStudent.value = false
  }
}
</script>

<style scoped>
.page-container {
  padding: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  margin: 16px 0;
}

.strategy-form {
  margin-top: 12px;
}
</style>
