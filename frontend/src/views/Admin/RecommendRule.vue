<template>
  <div class="page-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div>
            <h2>推荐规则配置</h2>
            <p>用于控制“冲/稳/保”三段推荐的位次区间，实时作用于学生端。</p>
          </div>
          <el-button type="primary" :loading="saving" @click="saveRule">保存配置</el-button>
        </div>
      </template>
      <el-form :model="form" label-width="160px" class="rule-form">
        <el-form-item label="冲刺：落后位次不小于">
          <el-input-number v-model="form.rushOffset" :min="50" :controls="false" />
          <span class="unit">名</span>
        </el-form-item>
        <el-form-item label="稳妥：上下浮动">
          <el-input-number v-model="form.matchOffset" :min="10" :controls="false" />
          <span class="unit">名</span>
        </el-form-item>
        <el-form-item label="保底：领先位次不小于">
          <el-input-number v-model="form.safeOffset" :min="50" :controls="false" />
          <span class="unit">名</span>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchRecommendRule, updateRecommendRule } from '../../api/admin'

const form = reactive({ rushOffset: 200, matchOffset: 200, safeOffset: 200 })
const saving = ref(false)

const loadRule = async () => {
  try {
    const { data } = await fetchRecommendRule()
    Object.assign(form, data)
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '加载配置失败')
  }
}

const saveRule = async () => {
  saving.value = true
  try {
    await updateRecommendRule(form)
    ElMessage.success('推荐规则已更新')
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadRule)
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

.card-header h2 {
  margin: 0 0 4px;
}

.card-header p {
  margin: 0;
  color: #6b7280;
}

.rule-form {
  max-width: 520px;
}

.rule-form :deep(.el-input-number) {
  width: 160px;
}

.unit {
  margin-left: 8px;
  color: #6b7280;
}
</style>
