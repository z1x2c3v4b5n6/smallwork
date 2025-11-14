<template>
  <div class="major-search-page">
    <section class="hero">
      <div class="hero__content">
        <el-tag type="success" effect="dark" class="hero__tag">志愿检索中心</el-tag>
        <h1>发现与你最匹配的院校与专业</h1>
        <p>
          汇集全国院校专业录取数据，支持条件检索、位次排序与志愿方案管理，助你快速锁定心仪目标。
        </p>
        <div class="hero__actions">
          <el-button type="primary" :icon="Compass" @click="goRecommend">按位次智能推荐</el-button>
          <el-button plain :icon="Notebook" @click="goPlans">我的志愿方案</el-button>
        </div>
      </div>
      <div class="hero__stats">
        <div class="stat-item">
          <span class="stat-item__label">符合筛选条件的专业</span>
          <span class="stat-item__value">{{ totalResults }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-item__label">最新最低位次（中位数）</span>
          <span class="stat-item__value">{{ medianRank }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-item__label">最新最低分（中位数）</span>
          <span class="stat-item__value">{{ medianScore }}</span>
        </div>
      </div>
    </section>

    <el-card class="filters-card" shadow="hover">
      <template #header>
        <div class="filters-card__header">
          <div class="filters-card__title">
            <el-icon><Filter /></el-icon>
            <span>高级筛选</span>
          </div>
          <div class="filters-card__actions">
            <el-button text type="primary" @click="resetFilters">清空条件</el-button>
            <el-button type="primary" :loading="loading" @click="loadData">开始检索</el-button>
          </div>
        </div>
      </template>
      <el-form :inline="true" :model="filters" class="filters" label-position="top">
        <el-form-item label="院校名称">
          <el-input v-model="filters.universityName" placeholder="如 北京大学" clearable />
        </el-form-item>
        <el-form-item label="专业名称">
          <el-input v-model="filters.majorName" placeholder="如 计算机科学与技术" clearable />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="filters.province" placeholder="报考省份" clearable />
        </el-form-item>
        <el-form-item label="批次">
          <el-input v-model="filters.batch" placeholder="本科一批 / 提前批" clearable />
        </el-form-item>
        <el-form-item label="专业类别">
          <el-input v-model="filters.category" placeholder="如 工学、管理学" clearable />
        </el-form-item>
        <el-form-item label="专业层次">
          <el-input v-model="filters.level" placeholder="重点 / 一流学科" clearable />
        </el-form-item>
        <el-form-item label="排序方式" class="filters__sort">
          <el-select v-model="sortOption" placeholder="选择排序" @change="handleSortChange">
            <el-option
              v-for="option in sortOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <section class="results" v-loading="loading">
      <div class="results__main">
        <el-empty v-if="!loading && !paginatedData.length" description="暂无符合条件的志愿数据" />
        <transition-group name="fade" tag="div">
          <el-card
            v-for="item in paginatedData"
            :key="`${item.universityId}-${item.majorId}`"
            class="major-card"
            shadow="hover"
          >
            <div class="major-card__header">
              <div>
                <div class="major-card__badge">{{ item.batch || '批次待定' }}</div>
                <h3>{{ item.universityName }}</h3>
                <p class="major-card__major">{{ item.majorName }}</p>
              </div>
              <el-button type="primary" link @click="viewDetail(item)">查看详情</el-button>
            </div>
            <div class="major-card__body">
              <div class="major-card__scores">
                <div class="score-block">
                  <span class="score-block__label">近一年最低分</span>
                  <span class="score-block__value">{{ formatNumber(item.latestMinScore) }}</span>
                </div>
                <div class="score-block rank">
                  <span class="score-block__label">近一年最低位次</span>
                  <span class="score-block__value">{{ formatNumber(item.latestMinRank) }}</span>
                </div>
              </div>
              <ul class="major-card__meta">
                <li>
                  <el-icon><Location /></el-icon>
                  <span>{{ item.province || '暂无省份信息' }}</span>
                </li>
                <li>
                  <el-icon><OfficeBuilding /></el-icon>
                  <span>{{ item.level || '院校层次待补充' }}</span>
                </li>
                <li>
                  <el-icon><Collection /></el-icon>
                  <span>{{ item.category || '专业类别待补充' }}</span>
                </li>
                <li>
                  <el-icon><Reading /></el-icon>
                  <span>{{ item.subjectReq || '选考要求待补充' }}</span>
                </li>
              </ul>
            </div>
          </el-card>
        </transition-group>
        <div v-if="paginatedData.length" class="results__pagination">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next"
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[6, 12, 18]"
            :total="totalResults"
            @current-change="handlePageChange"
            @size-change="handlePageSizeChange"
          />
        </div>
      </div>
      <aside class="results__sidebar">
        <el-card class="sidebar-card" shadow="never">
          <template #header>
            <div class="sidebar-card__title">
              <el-icon><TrendCharts /></el-icon>
              <span>位次三段推荐</span>
            </div>
          </template>
          <p>基于你的高考位次，系统自动给出“冲、稳、保”志愿组合建议。</p>
          <el-button type="primary" plain size="small" @click="goRecommend">立即体验</el-button>
        </el-card>
        <el-card class="sidebar-card" shadow="never">
          <template #header>
            <div class="sidebar-card__title">
              <el-icon><List /></el-icon>
              <span>我的志愿方案</span>
            </div>
          </template>
          <p>集中管理你的志愿方案，记录意向院校与专业，随时调整组合。</p>
          <el-button type="primary" plain size="small" @click="goPlans">查看方案</el-button>
        </el-card>
        <el-card class="sidebar-card" shadow="never">
          <template #header>
            <div class="sidebar-card__title">
              <el-icon><Guide /></el-icon>
              <span>志愿填报建议</span>
            </div>
          </template>
          <ul class="tips-list">
            <li v-for="tip in tips" :key="tip">
              <el-icon><Star /></el-icon>
              <span>{{ tip }}</span>
            </li>
          </ul>
        </el-card>
      </aside>
    </section>

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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Collection,
  Compass,
  Filter,
  Guide,
  List,
  Location,
  Notebook,
  OfficeBuilding,
  Reading,
  Star,
  TrendCharts
} from '@element-plus/icons-vue'
import { searchMajors, fetchMajorDetail } from '../../api/student'

const router = useRouter()

const filters = reactive({
  universityName: '',
  majorName: '',
  province: '',
  batch: '',
  category: '',
  level: ''
})

const loading = ref(false)
const results = ref<any[]>([])
const detailData = ref<any[]>([])
const detailVisible = ref(false)

const sortOption = ref<'rankAsc' | 'rankDesc' | 'scoreDesc' | 'scoreAsc'>('rankAsc')
const sortOptions = [
  { value: 'rankAsc', label: '按最低位次升序' },
  { value: 'rankDesc', label: '按最低位次降序' },
  { value: 'scoreDesc', label: '按最低分从高到低' },
  { value: 'scoreAsc', label: '按最低分从低到高' }
]

const currentPage = ref(1)
const pageSize = ref(6)

const sortedData = computed(() => {
  const data = [...results.value]
  const compareNumber = (a: number | null | undefined, b: number | null | undefined, reverse = false) => {
    const valA = typeof a === 'number' ? a : Number.MAX_SAFE_INTEGER
    const valB = typeof b === 'number' ? b : Number.MAX_SAFE_INTEGER
    return reverse ? valB - valA : valA - valB
  }

  switch (sortOption.value) {
    case 'rankDesc':
      data.sort((a, b) => compareNumber(a.latestMinRank, b.latestMinRank, true))
      break
    case 'scoreDesc':
      data.sort((a, b) => compareNumber(b.latestMinScore, a.latestMinScore))
      break
    case 'scoreAsc':
      data.sort((a, b) => compareNumber(a.latestMinScore, b.latestMinScore))
      break
    default:
      data.sort((a, b) => compareNumber(a.latestMinRank, b.latestMinRank))
      break
  }

  return data
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return sortedData.value.slice(start, start + pageSize.value)
})

const totalResults = computed(() => sortedData.value.length)

const median = (values: number[]) => {
  if (!values.length) return '--'
  const sorted = [...values].sort((a, b) => a - b)
  const middle = Math.floor(sorted.length / 2)
  if (sorted.length % 2 === 0) {
    return Math.round((sorted[middle - 1] + sorted[middle]) / 2)
  }
  return sorted[middle]
}

const medianRank = computed(() => {
  const ranks = sortedData.value
    .map((item) => item.latestMinRank)
    .filter((rank) => typeof rank === 'number')
  return ranks.length ? median(ranks as number[]) : '--'
})

const medianScore = computed(() => {
  const scores = sortedData.value
    .map((item) => item.latestMinScore)
    .filter((score) => typeof score === 'number')
  return scores.length ? median(scores as number[]) : '--'
})

const tips = [
  '综合考虑分数、位次、地域、专业热度等因素。',
  '冲稳保比例建议约为 3:4:3，根据自身定位灵活调整。',
  '关注专业选考要求，确保与自身科目组合匹配。',
  '合理规划志愿顺序，兼顾冲刺目标与保底选择。'
]

const resetFilters = () => {
  filters.universityName = ''
  filters.majorName = ''
  filters.province = ''
  filters.batch = ''
  filters.category = ''
  filters.level = ''
}

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await searchMajors(filters)
    results.value = data
    currentPage.value = 1
  } catch (err) {
    ElMessage.error('查询失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

const viewDetail = async (row: any) => {
  try {
    const { data } = await fetchMajorDetail(row.universityId, row.majorId)
    detailData.value = data
    detailVisible.value = true
  } catch (err) {
    ElMessage.error('加载详情失败')
  }
}

const handleSortChange = () => {
  currentPage.value = 1
}

const handlePageChange = (page: number) => {
  currentPage.value = page
}

const handlePageSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const formatNumber = (value: number | null | undefined) => {
  if (value === null || value === undefined) {
    return '--'
  }
  return Number(value).toLocaleString()
}

const goRecommend = () => router.push('/student/recommend')
const goPlans = () => router.push('/student/plans')

onMounted(loadData)
</script>

<style scoped>
.major-search-page {
  padding: 32px 24px 48px;
  background: linear-gradient(180deg, #f0f5ff 0%, #ffffff 32%);
  min-height: 100%;
}

.hero {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 24px;
  background: #1f4ed6;
  border-radius: 16px;
  padding: 32px;
  color: #fff;
  margin-bottom: 24px;
  box-shadow: 0 14px 32px rgba(31, 78, 214, 0.25);
}

.hero__content {
  flex: 1 1 420px;
}

.hero__tag {
  margin-bottom: 16px;
}

.hero h1 {
  font-size: 28px;
  margin: 0 0 12px;
  font-weight: 600;
}

.hero p {
  margin: 0 0 24px;
  font-size: 16px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.9);
}

.hero__actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.hero__stats {
  flex: 0 0 280px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 16px;
  padding: 24px;
  display: grid;
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-item__label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.75);
}

.stat-item__value {
  font-size: 26px;
  font-weight: 600;
}

.filters-card {
  margin-bottom: 24px;
  border-radius: 14px;
}

.filters-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.filters-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.filters-card__actions {
  display: flex;
  gap: 8px;
}

.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px 24px;
}

.filters__sort {
  min-width: 220px;
}

.results {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 24px;
  align-items: start;
}

@media (max-width: 1100px) {
  .results {
    grid-template-columns: 1fr;
  }

  .hero {
    flex-direction: column;
  }

  .hero__stats {
    width: 100%;
  }
}

.results__main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.major-card {
  border-radius: 16px;
  overflow: hidden;
}

.major-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.major-card__badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(31, 78, 214, 0.1);
  color: #1f4ed6;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 8px;
}

.major-card__major {
  margin: 4px 0 0;
  color: #4b5563;
}

.major-card__body {
  display: flex;
  flex-wrap: wrap;
  gap: 16px 32px;
  align-items: center;
}

.major-card__scores {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.score-block {
  min-width: 160px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(31, 78, 214, 0.12), rgba(31, 78, 214, 0.05));
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.score-block.rank {
  background: linear-gradient(135deg, rgba(0, 200, 157, 0.12), rgba(0, 200, 157, 0.05));
}

.score-block__label {
  font-size: 13px;
  color: #5f6b7c;
}

.score-block__value {
  font-size: 22px;
  font-weight: 600;
  color: #1f2a44;
}

.major-card__meta {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
  font-size: 14px;
  color: #4b5563;
}

.major-card__meta li {
  display: flex;
  align-items: center;
  gap: 8px;
}

.results__pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

.results__sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-card {
  border-radius: 14px;
}

.sidebar-card__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
  color: #4b5563;
}

.tips-list li {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
