<template>
  <div class="profile-page">
    <section class="profile-grid">
      <el-card shadow="hover" v-loading="summaryLoading">
        <template #header>
          <div class="card-header">
            <div>
              <h2>个人档案</h2>
              <p>系统将优先使用这些数据进行位次推荐、风险评估与统计展示。</p>
            </div>
            <el-button type="primary" :loading="saving" @click="saveProfile">保存信息</el-button>
          </div>
        </template>
        <el-form label-position="top" :model="form" class="profile-form">
          <div class="profile-form__row">
            <el-form-item label="姓名">
              <el-input v-model="form.realName" placeholder="真实姓名" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" placeholder="展示昵称" />
            </el-form-item>
          </div>
          <div class="profile-form__row">
            <el-form-item label="省份">
              <el-input v-model="form.province" placeholder="高考所在省份" />
            </el-form-item>
            <el-form-item label="选考科目">
              <el-input v-model="form.subjects" placeholder="如 物理 化学" />
            </el-form-item>
          </div>
          <div class="profile-form__row">
            <el-form-item label="当前分数">
              <el-input-number v-model="form.score" :min="0" :max="750" :controls="false" placeholder="总分" />
            </el-form-item>
            <el-form-item label="当前位次">
              <el-input-number v-model="form.rank" :min="1" :controls="false" placeholder="位次" />
            </el-form-item>
          </div>
          <div class="profile-form__row">
            <el-form-item label="一模分数">
              <el-input-number v-model="form.firstMockScore" :min="0" :max="750" :controls="false" />
            </el-form-item>
            <el-form-item label="一模位次">
              <el-input-number v-model="form.firstMockRank" :min="1" :controls="false" />
            </el-form-item>
          </div>
          <div class="profile-form__row">
            <el-form-item label="二模分数">
              <el-input-number v-model="form.secondMockScore" :min="0" :max="750" :controls="false" />
            </el-form-item>
            <el-form-item label="二模位次">
              <el-input-number v-model="form.secondMockRank" :min="1" :controls="false" />
            </el-form-item>
          </div>
          <el-form-item label="目标专业方向">
            <el-input v-model="form.targetMajorType" type="textarea" placeholder="例如 计算机类 / 金融学 / 师范类" />
          </el-form-item>
        </el-form>
      </el-card>

      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <div>
              <h2>推荐概览</h2>
              <p>根据当前档案数据实时计算三段推荐的数量分布。</p>
            </div>
            <el-button text type="primary" @click="refreshSummary">刷新</el-button>
          </div>
        </template>
        <el-empty v-if="!summary.ready" description="完善档案后可查看推荐概览" />
        <div v-else class="summary-grid">
          <div class="summary-card rush">
            <span class="summary-card__label">冲刺</span>
            <span class="summary-card__value">{{ summary.rushCount }}</span>
          </div>
          <div class="summary-card match">
            <span class="summary-card__label">稳妥</span>
            <span class="summary-card__value">{{ summary.matchCount }}</span>
          </div>
          <div class="summary-card safe">
            <span class="summary-card__label">保底</span>
            <span class="summary-card__value">{{ summary.safeCount }}</span>
          </div>
        </div>
        <el-button type="primary" plain class="summary-action" @click="goRecommend">查看推荐列表</el-button>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchStudentProfile, updateStudentProfile, fetchProfileSummary } from '../../api/student'

const router = useRouter()

const form = reactive({
  realName: '',
  nickname: '',
  province: '',
  subjects: '',
  score: null as number | null,
  rank: null as number | null,
  firstMockScore: null as number | null,
  firstMockRank: null as number | null,
  secondMockScore: null as number | null,
  secondMockRank: null as number | null,
  targetMajorType: ''
})

const saving = ref(false)
const summary = ref({ ready: false, rushCount: 0, matchCount: 0, safeCount: 0 })
const summaryLoading = ref(false)

const loadProfile = async () => {
  try {
    const { data } = await fetchStudentProfile()
    if (data) {
      Object.assign(form, data)
    }
  } catch (err) {
    console.warn('加载档案失败', err)
  }
}

const saveProfile = async () => {
  saving.value = true
  try {
    await updateStudentProfile(form)
    ElMessage.success('保存成功，推荐逻辑将自动使用最新数据')
    refreshSummary()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const refreshSummary = async () => {
  summaryLoading.value = true
  try {
    const { data } = await fetchProfileSummary()
    summary.value = data
  } catch (err) {
    console.warn('加载推荐概览失败', err)
  } finally {
    summaryLoading.value = false
  }
}

const goRecommend = () => router.push('/student/recommend')

onMounted(() => {
  loadProfile()
  refreshSummary()
})
</script>

<style scoped>
.profile-page {
  padding: 32px 24px;
  min-height: 100%;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(0, 1fr);
  gap: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-header h2 {
  margin: 0 0 4px;
}

.card-header p {
  margin: 0;
  color: #6b7280;
}

.profile-form__row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.profile-form :deep(.el-input-number) {
  width: 100%;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.summary-card {
  padding: 16px;
  border-radius: 12px;
  color: #fff;
}

.summary-card__label {
  display: block;
  font-size: 14px;
  opacity: 0.8;
}

.summary-card__value {
  font-size: 28px;
  font-weight: 600;
}

.summary-card.rush {
  background: linear-gradient(135deg, #f97316, #fb923c);
}

.summary-card.match {
  background: linear-gradient(135deg, #facc15, #fcd34d);
}

.summary-card.safe {
  background: linear-gradient(135deg, #22c55e, #4ade80);
}

.summary-action {
  width: 100%;
}

@media (max-width: 960px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
