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
        <div class="hero__profile" :class="{ 'hero__profile--empty': !profile }">
          <div class="hero__profile-item">
            <span class="hero__profile-label">当前位次</span>
            <span class="hero__profile-value">{{ profile?.rank ? formatNumber(profile.rank) : '--' }}</span>
          </div>
          <div class="hero__profile-item">
            <span class="hero__profile-label">当前分数</span>
            <span class="hero__profile-value">{{ profile?.score ? formatNumber(profile.score) : '--' }}</span>
          </div>
          <div class="hero__profile-item">
            <span class="hero__profile-label">科目组合</span>
            <span class="hero__profile-value">{{ profile?.subjects || '待完善' }}</span>
          </div>
          <p class="hero__profile-tip" v-if="!profile">尚未获取到个人成绩信息，请联系管理员完善学生档案。</p>
        </div>
      </div>
      <div class="hero__stats">
        <div class="stat-item" v-for="stat in summaryStats" :key="stat.label">
          <span class="stat-item__label">{{ stat.label }}</span>
          <span class="stat-item__value">{{ stat.value }}</span>
          <span v-if="stat.extra" class="stat-item__sub">{{ stat.extra }}</span>
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

    <section class="results">
      <div class="results__main">
        <div v-if="loading" class="results__skeleton">
          <el-skeleton
            v-for="item in skeletonItems"
            :key="item"
            animated
            class="major-card major-card--skeleton"
          >
            <template #template>
              <el-skeleton-item variant="text" style="width: 28%; height: 18px; margin-bottom: 12px" />
              <el-skeleton-item variant="text" style="width: 60%; height: 24px; margin-bottom: 8px" />
              <el-skeleton-item variant="text" style="width: 45%; height: 20px; margin-bottom: 24px" />
              <div style="display: flex; gap: 16px; margin-bottom: 16px">
                <el-skeleton-item variant="rect" style="width: 160px; height: 64px" />
                <el-skeleton-item variant="rect" style="width: 160px; height: 64px" />
              </div>
              <el-skeleton-item variant="text" style="width: 100%; height: 16px; margin-bottom: 8px" />
              <el-skeleton-item variant="text" style="width: 80%; height: 16px" />
            </template>
          </el-skeleton>
        </div>
        <template v-else>
          <transition-group name="fade" tag="div">
            <el-card
              v-for="item in paginatedData"
              :key="`${item.universityId}-${item.majorId}`"
              class="major-card"
              shadow="hover"
            >
              <div class="major-card__header">
                <div class="major-card__title">
                  <div class="major-card__badge">{{ item.batch || '批次待定' }}</div>
                  <h3>{{ item.universityName }}</h3>
                  <p class="major-card__major">{{ item.majorName }}</p>
                  <div v-if="getUniversityTags(item).length" class="major-card__tags">
                    <el-tag
                      v-for="tag in getUniversityTags(item)"
                      :key="tag.label"
                      :type="tag.type"
                      effect="dark"
                      round
                      size="small"
                    >
                      {{ tag.label }}
                    </el-tag>
                  </div>
                </div>
                <div class="major-card__header-actions">
                  <el-tag class="risk-tag" :type="getRiskInfo(item).type" effect="dark" round>
                    {{ getRiskInfo(item).label }}
                  </el-tag>
                  <el-button type="primary" link @click="viewDetail(item)">查看详情</el-button>
                </div>
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
                    <span>{{ getLocationText(item) }}</span>
                  </li>
                  <li>
                    <el-icon><OfficeBuilding /></el-icon>
                    <span>{{ getLevelText(item) }}</span>
                  </li>
                  <li>
                    <el-icon><Collection /></el-icon>
                    <span>{{ getDisciplineText(item) }}</span>
                  </li>
                  <li>
                    <el-icon><Reading /></el-icon>
                    <span>{{ item.subjectReq || '选考要求待补充' }}</span>
                  </li>
                  <li>
                    <el-icon><Timer /></el-icon>
                    <span>{{ getDurationText(item) }}</span>
                  </li>
                  <li>
                    <el-icon><Coin /></el-icon>
                    <span>{{ getTuitionText(item) }}</span>
                  </li>
                </ul>
              </div>
              <div class="major-card__footer">
                <div class="major-card__footer-note">
                  <el-icon><Star /></el-icon>
                  <span>快速加入志愿方案，随时对比和调整组合。</span>
                </div>
                <el-button type="primary" plain size="small" :icon="CirclePlus" @click="openPlanDialog(item)">
                  加入方案
                </el-button>
              </div>
            </el-card>
          </transition-group>
          <el-empty v-if="!paginatedData.length" image-size="160" class="results__empty">
            <template #description>
              <p>当前条件下未检索到志愿，请尝试放宽筛选或切换省份/批次。</p>
              <el-button type="primary" text @click="resetFilters">清空条件</el-button>
            </template>
          </el-empty>
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
        </template>
      </div>
      <aside class="results__sidebar">
        <el-card class="sidebar-card" shadow="never">
          <template #header>
            <div class="sidebar-card__title">
              <el-icon><TrendCharts /></el-icon>
              <span>位次三段推荐</span>
            </div>
          </template>
          <ul class="segment-list">
            <li><strong>冲刺段：</strong>当前位次比往年最低位次落后 200 名以上，录取风险较高。</li>
            <li><strong>稳妥段：</strong>当前位次与往年最低位次相差 ±200 名，录取概率较为稳定。</li>
            <li><strong>保底段：</strong>当前位次领先往年最低位次 200 名以上，建议至少保留 2-3 个。</li>
          </ul>
          <el-button type="primary" plain size="small" @click="goRecommend">刷新位次推荐</el-button>
        </el-card>
        <el-card class="sidebar-card" shadow="never">
          <template #header>
            <div class="sidebar-card__title">
              <el-icon><List /></el-icon>
              <span>我的志愿方案</span>
            </div>
          </template>
          <p>管理你的志愿方案，记录意向院校与专业。可在检索结果中直接加入方案，也可前往方案页调整顺序。</p>
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
            <li
              v-for="tip in adviceTips"
              :key="tip.text"
              :class="['tips-item', { 'tips-item--warning': tip.type === 'warning' }]"
            >
              <el-icon><Star /></el-icon>
              <span>{{ tip.text }}</span>
            </li>
          </ul>
        </el-card>
      </aside>
    </section>

    <el-dialog v-model="planDialogVisible" title="加入志愿方案" width="420px" @closed="resetPlanDialog">
      <div v-if="planDialogTarget" class="plan-dialog__target">
        <div class="plan-dialog__badge">{{ planDialogTarget.batch || '批次待定' }}</div>
        <h4>{{ planDialogTarget.universityName }}</h4>
        <p>{{ planDialogTarget.majorName }}</p>
      </div>
      <el-form label-position="top" class="plan-dialog__form">
        <el-form-item label="选择方案">
          <el-select v-model="selectedPlanId" placeholder="请选择方案">
            <el-option v-for="plan in plans" :key="plan.id" :label="plan.name" :value="plan.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="志愿顺序（可选）">
          <el-input-number v-model="planOrderNo" :min="1" :controls="false" placeholder="自动顺序" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="addingToPlan" @click="confirmAddToPlan">确认加入</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="历年录取情况" width="60%">
      <div v-if="detailData.length" class="detail-content">
        <div v-if="chartMetrics" class="detail-chart">
          <svg
            :viewBox="`0 0 ${chartMetrics.chartWidth} ${chartMetrics.chartHeight}`"
            preserveAspectRatio="none"
          >
            <line
              class="chart-axis"
              :x1="chartMetrics.offsetX"
              :y1="chartMetrics.axisY"
              :x2="chartMetrics.axisXEnd"
              :y2="chartMetrics.axisY"
            />
            <line
              class="chart-axis"
              :x1="chartMetrics.offsetX"
              y1="20"
              :x2="chartMetrics.offsetX"
              :y2="chartMetrics.axisY"
            />
            <path
              v-if="chartMetrics.scoreSeries.path"
              :d="chartMetrics.scoreSeries.path"
              class="chart-line chart-line--score"
            />
            <path
              v-if="chartMetrics.rankSeries.path"
              :d="chartMetrics.rankSeries.path"
              class="chart-line chart-line--rank"
            />
            <g class="chart-points">
              <circle
                v-for="(point, index) in chartMetrics.scoreSeries.points"
                :key="`score-${index}`"
                :cx="point.x"
                :cy="point.y"
                r="4"
                class="chart-point chart-point--score"
              />
              <circle
                v-for="(point, index) in chartMetrics.rankSeries.points"
                :key="`rank-${index}`"
                :cx="point.x"
                :cy="point.y"
                r="4"
                class="chart-point chart-point--rank"
              />
            </g>
            <g class="chart-labels">
              <text
                v-for="(year, index) in chartMetrics.years"
                :key="year"
                :x="chartMetrics.xPositions[index]"
                :y="chartMetrics.axisY + 24"
                text-anchor="middle"
              >
                {{ year }}
              </text>
            </g>
          </svg>
          <div class="detail-chart__legend">
            <div class="detail-chart__legend-item">
              <span class="legend-dot legend-dot--score"></span>
              <span>
                最低分
                <template v-if="chartMetrics.scoreSeries.min !== null">
                  {{ formatNumber(chartMetrics.scoreSeries.min) }} ~
                  {{ formatNumber(chartMetrics.scoreSeries.max) }}
                </template>
                <template v-else>暂无数据</template>
              </span>
            </div>
            <div class="detail-chart__legend-item">
              <span class="legend-dot legend-dot--rank"></span>
              <span>
                最低位次
                <template v-if="chartMetrics.rankSeries.min !== null">
                  {{ formatNumber(chartMetrics.rankSeries.min) }} ~
                  {{ formatNumber(chartMetrics.rankSeries.max) }}
                </template>
                <template v-else>暂无数据</template>
              </span>
            </div>
          </div>
        </div>
        <el-table :data="detailData" border>
          <el-table-column prop="year" label="年份" width="100" />
          <el-table-column prop="batch" label="批次" width="120" />
          <el-table-column prop="minScore" label="最低分" />
          <el-table-column prop="minRank" label="最低位次" />
          <el-table-column prop="avgScore" label="平均分" />
          <el-table-column prop="avgRank" label="平均位次" />
          <el-table-column prop="admitCount" label="录取人数" />
        </el-table>
      </div>
      <el-empty v-else description="暂无历年数据" />
    </el-dialog>

    <div class="mobile-mask">
      <div class="mobile-mask__content">
        <h3>建议在电脑端使用</h3>
        <p>当前页面针对桌面端设计，推荐使用宽屏设备以获得最佳体验。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CirclePlus,
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
  TrendCharts,
  Timer,
  Coin
} from '@element-plus/icons-vue'
import { fetchMajorDetail, fetchStudentProfile, searchMajors } from '../../api/student'
import { addPlanItem, listPlans } from '../../api/plan'

interface Profile {
  score?: number | null
  rank?: number | null
  subjects?: string | null
  province?: string | null
}

interface MajorResult {
  universityId: number
  universityName: string
  province?: string | null
  city?: string | null
  universityLevel?: string | null
  universityType?: string | null
  doubleFirstClass?: boolean | null
  universityRemark?: string | null
  majorId: number
  majorName: string
  category?: string | null
  subjectReq?: string | null
  level?: string | null
  discipline?: string | null
  majorRemark?: string | null
  batch?: string | null
  duration?: number | null
  tuition?: number | null
  latestMinScore?: number | null
  latestMinRank?: number | null
}

interface AdmissionStat {
  year: number
  batch: string
  minScore: number | null
  minRank: number | null
  avgScore: number | null
  avgRank: number | null
  admitCount: number | null
}

interface PlanSummary {
  id: number
  name: string
  createTime?: string
}

interface AdviceTip {
  text: string
  type?: 'default' | 'warning'
}

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
const results = ref<MajorResult[]>([])
const detailData = ref<AdmissionStat[]>([])
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
const profile = ref<Profile | null>(null)
const plans = ref<PlanSummary[]>([])
const planDialogVisible = ref(false)
const planDialogTarget = ref<MajorResult | null>(null)
const selectedPlanId = ref<number | null>(null)
const planOrderNo = ref<number | null>(null)
const addingToPlan = ref(false)
const errorMessage = ref('')

const skeletonItems = computed(() => Array.from({ length: pageSize.value }, (_, index) => index))

const formatNumber = (value: number | null | undefined) => {
  if (value === null || value === undefined) {
    return '--'
  }
  return Number(value).toLocaleString()
}

const formatRange = (min: number | null, max: number | null) => {
  if (min === null && max === null) return '--'
  if (min !== null && max !== null) {
    return `${formatNumber(min)} ~ ${formatNumber(max)}`
  }
  return formatNumber(min ?? max ?? undefined)
}

const computeStats = (values: (number | null | undefined)[]) => {
  const numbers = values.filter((value): value is number => typeof value === 'number')
  if (!numbers.length) {
    return { min: null as number | null, max: null as number | null, avg: null as number | null }
  }
  const min = Math.min(...numbers)
  const max = Math.max(...numbers)
  const avg = Math.round(numbers.reduce((sum, value) => sum + value, 0) / numbers.length)
  return { min, max, avg }
}

const sortedData = computed<MajorResult[]>(() => {
  const data = [...results.value]
  const compareNumber = (
    a: number | null | undefined,
    b: number | null | undefined,
    reverse = false
  ) => {
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

const rankStats = computed(() => computeStats(sortedData.value.map((item) => item.latestMinRank)))
const scoreStats = computed(() => computeStats(sortedData.value.map((item) => item.latestMinScore)))

const summaryStats = computed(() => [
  { label: '符合筛选条件的专业', value: formatNumber(totalResults.value), extra: '' },
  {
    label: '位次区间',
    value: formatRange(rankStats.value.min, rankStats.value.max),
    extra: rankStats.value.avg !== null ? `平均位次 ${formatNumber(rankStats.value.avg)}` : ''
  },
  {
    label: '分数区间',
    value: formatRange(scoreStats.value.min, scoreStats.value.max),
    extra: scoreStats.value.avg !== null ? `平均分 ${formatNumber(scoreStats.value.avg)}` : ''
  }
])

const baseTips: AdviceTip[] = [
  { text: '综合考虑分数、位次、地域、专业热度等因素，制定多套志愿组合。' },
  { text: '冲刺段：当前位次比往年最低位次落后 200 名以上，需要充分评估风险。' },
  { text: '稳妥段：当前位次与往年最低位次相差 ±200 名，注意填报顺序与志愿梯度。' },
  { text: '保底段：当前位次领先往年最低位次 200 名以上，可确保录取底线。' },
  { text: '关注专业选考要求，确保与自身科目组合匹配。' }
]

const adviceTips = computed(() => {
  const tips: AdviceTip[] = [...baseTips]
  if (errorMessage.value) {
    tips.unshift({ text: errorMessage.value, type: 'warning' })
  }
  return tips
})

const chartMetrics = computed(() => {
  if (!detailData.value.length) {
    return null
  }
  const sorted = [...detailData.value].sort((a, b) => a.year - b.year)
  const years = sorted.map((item) => item.year)
  const offsetX = 60
  const offsetY = 32
  const baseWidth = 860
  const baseHeight = 200
  const xStep = years.length > 1 ? baseWidth / (years.length - 1) : 0
  const xPositions = years.map((_, index) =>
    years.length === 1 ? offsetX + baseWidth / 2 : offsetX + xStep * index
  )

  const buildSeries = (values: (number | null)[], invert = false) => {
    const numbers = values.filter((value): value is number => typeof value === 'number')
    if (!numbers.length) {
      return { path: '', points: [] as { x: number; y: number; value: number }[], min: null as number | null, max: null as number | null }
    }
    const min = Math.min(...numbers)
    const max = Math.max(...numbers)
    const range = max === min ? 1 : max - min
    const commands: string[] = []
    const points: { x: number; y: number; value: number }[] = []
    values.forEach((value, index) => {
      if (value === null || value === undefined) {
        return
      }
      const x = xPositions[index]
      let ratio = range === 0 ? 0.5 : (value - min) / range
      if (invert) {
        ratio = 1 - ratio
      }
      const y = offsetY + baseHeight - ratio * baseHeight
      commands.push(`${commands.length ? 'L' : 'M'}${x.toFixed(2)} ${y.toFixed(2)}`)
      points.push({ x, y, value })
    })
    return { path: commands.join(' '), points, min, max }
  }

  const scoreSeries = buildSeries(sorted.map((item) => item.minScore))
  const rankSeries = buildSeries(sorted.map((item) => item.minRank), true)

  const axisXEnd = xPositions[xPositions.length - 1] + 20
  const chartWidth = axisXEnd + 20
  const chartHeight = offsetY + baseHeight + 40

  return {
    years,
    scoreSeries,
    rankSeries,
    xPositions,
    offsetX,
    offsetY,
    chartWidth,
    chartHeight,
    axisY: offsetY + baseHeight,
    axisXEnd
  }
})

const getRiskInfo = (item: MajorResult) => {
  if (!item.latestMinRank || !profile.value?.rank) {
    return { label: '待评估', type: 'info' as const }
  }
  const diff = (profile.value.rank ?? 0) - item.latestMinRank
  if (diff > 200) {
    return { label: '冲', type: 'danger' as const }
  }
  if (Math.abs(diff) <= 200) {
    return { label: '稳', type: 'warning' as const }
  }
  return { label: '保', type: 'success' as const }
}

const tagStyleMap: Record<string, 'primary' | 'success' | 'warning' | 'info' | 'danger'> = {
  '985': 'danger',
  '211': 'warning',
  '双一流': 'success',
  '一本': 'primary',
  '二本': 'info'
}

const getUniversityTags = (item: MajorResult) => {
  const tags: { label: string; type: 'primary' | 'success' | 'warning' | 'info' | 'danger' }[] = []
  const addTag = (label: string) => {
    const value = label.trim()
    if (!value || tags.find((tag) => tag.label === value)) {
      return
    }
    const type = tagStyleMap[value] || 'info'
    tags.push({ label: value, type })
  }

  const tokens = (item.universityLevel || '').split(/[、,，\s/]+/)
  tokens.forEach((token) => addTag(token))
  if (item.doubleFirstClass) {
    addTag('双一流')
  }
  if (item.universityRemark && item.universityRemark.includes('双一流')) {
    addTag('双一流')
  }
  if (!tags.length && item.universityLevel) {
    addTag(item.universityLevel)
  }
  return tags
}

const getLocationText = (item: MajorResult) => {
  const parts = [item.province, item.city].filter(Boolean)
  return parts.length ? parts.join(' · ') : '暂无省市信息'
}

const getLevelText = (item: MajorResult) => {
  const parts = [item.universityType, item.universityLevel].filter(Boolean)
  return parts.length ? parts.join(' / ') : '院校层次待补充'
}

const getDisciplineText = (item: MajorResult) => {
  if (item.category || item.discipline) {
    return [item.category, item.discipline].filter(Boolean).join(' · ')
  }
  return '专业方向待补充'
}

const getDurationText = (item: MajorResult) => {
  if (item.duration) {
    return `学制 ${item.duration} 年`
  }
  return '学制待补充'
}

const getTuitionText = (item: MajorResult) => {
  if (item.tuition) {
    return `学费 ${formatNumber(item.tuition)} 元/年`
  }
  return '学费待补充'
}

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
  errorMessage.value = ''
  try {
    const { data } = await searchMajors(filters)
    results.value = data as MajorResult[]
    currentPage.value = 1
  } catch (err: any) {
    errorMessage.value = err?.response?.data?.message || '查询失败，请稍后再试'
    ElMessage.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

const loadProfile = async () => {
  try {
    const { data } = await fetchStudentProfile()
    profile.value = data
  } catch (err) {
    console.warn('加载学生档案失败', err)
  }
}

const loadPlans = async () => {
  try {
    const { data } = await listPlans()
    plans.value = data as PlanSummary[]
  } catch (err) {
    console.warn('加载志愿方案失败', err)
  }
}

const viewDetail = async (row: MajorResult) => {
  try {
    const { data } = await fetchMajorDetail(row.universityId, row.majorId)
    detailData.value = (data as AdmissionStat[]).sort((a, b) => a.year - b.year)
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

const openPlanDialog = (item: MajorResult) => {
  if (!plans.value.length) {
    ElMessage.info('请先在“志愿方案”页面创建方案后再添加志愿')
    return
  }
  planDialogTarget.value = item
  if (!selectedPlanId.value && plans.value.length) {
    selectedPlanId.value = plans.value[0].id
  }
  planOrderNo.value = null
  planDialogVisible.value = true
}

const resetPlanDialog = () => {
  planDialogTarget.value = null
  planOrderNo.value = null
  addingToPlan.value = false
}

const confirmAddToPlan = async () => {
  if (!planDialogTarget.value || !selectedPlanId.value) {
    ElMessage.warning('请选择要加入的方案')
    return
  }
  addingToPlan.value = true
  try {
    await addPlanItem(selectedPlanId.value, {
      universityId: planDialogTarget.value.universityId,
      majorId: planDialogTarget.value.majorId,
      batch: planDialogTarget.value.batch,
      orderNo: planOrderNo.value || undefined
    })
    ElMessage.success('已加入志愿方案')
    planDialogVisible.value = false
    resetPlanDialog()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '加入方案失败')
  } finally {
    addingToPlan.value = false
  }
}

const goRecommend = () => router.push('/student/recommend')
const goPlans = () => router.push('/student/plans')

watch(planDialogVisible, (visible) => {
  if (!visible) {
    resetPlanDialog()
  }
})

watch(plans, (newPlans) => {
  if (!newPlans.length) {
    selectedPlanId.value = null
  } else if (!selectedPlanId.value) {
    selectedPlanId.value = newPlans[0].id
  }
})

onMounted(() => {
  loadProfile()
  loadPlans()
  loadData()
})
</script>

<style scoped>
.major-search-page {
  padding: 32px 24px 48px;
  background: linear-gradient(180deg, #f0f5ff 0%, #ffffff 32%);
  min-height: 100%;
  position: relative;
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
  flex: 1 1 440px;
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
  margin-bottom: 20px;
}

.hero__profile {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
}

.hero__profile--empty {
  background: rgba(255, 255, 255, 0.08);
}

.hero__profile-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 120px;
}

.hero__profile-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
}

.hero__profile-value {
  font-size: 20px;
  font-weight: 600;
}

.hero__profile-tip {
  width: 100%;
  margin: 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
}

.hero__stats {
  flex: 0 0 300px;
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

.stat-item__sub {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
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

.results__main {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.results__skeleton {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.major-card {
  border-radius: 16px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.major-card--skeleton {
  padding: 24px;
}

.major-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.major-card__title h3 {
  margin: 0;
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

.major-card__tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
}

.major-card__header-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.risk-tag {
  font-weight: 600;
  letter-spacing: 1px;
}

.major-card__body {
  display: flex;
  flex-direction: column;
  gap: 16px;
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
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
  font-size: 14px;
  color: #4b5563;
}

.major-card__meta li {
  display: flex;
  align-items: center;
  gap: 8px;
}

.major-card__footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding-top: 4px;
  border-top: 1px dashed #e5e7eb;
}

.major-card__footer-note {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4b5563;
  font-size: 13px;
}

.results__empty {
  margin-top: 24px;
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

.segment-list {
  list-style: none;
  padding: 0;
  margin: 0 0 12px;
  display: grid;
  gap: 8px;
  color: #4b5563;
  font-size: 14px;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
  color: #4b5563;
}

.tips-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tips-item--warning {
  color: #d97706;
}

.plan-dialog__target {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.plan-dialog__badge {
  align-self: flex-start;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(31, 78, 214, 0.12);
  color: #1f4ed6;
  font-size: 12px;
  font-weight: 600;
}

.plan-dialog__form {
  display: grid;
  gap: 12px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-chart {
  width: 100%;
}

.detail-chart svg {
  width: 100%;
  height: 260px;
}

.chart-axis {
  stroke: #e5e7eb;
  stroke-width: 1;
}

.chart-line {
  fill: none;
  stroke-width: 2.5;
}

.chart-line--score {
  stroke: #1f4ed6;
}

.chart-line--rank {
  stroke: #00c89d;
}

.chart-point {
  stroke: #fff;
  stroke-width: 2;
}

.chart-point--score {
  fill: #1f4ed6;
}

.chart-point--rank {
  fill: #00c89d;
}

.chart-labels text {
  font-size: 12px;
  fill: #4b5563;
}

.detail-chart__legend {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  flex-wrap: wrap;
  font-size: 13px;
  color: #4b5563;
}

.detail-chart__legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

.legend-dot--score {
  background: #1f4ed6;
}

.legend-dot--rank {
  background: #00c89d;
}

.mobile-mask {
  display: none;
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

@media (max-width: 1200px) {
  .results {
    grid-template-columns: 1fr;
  }

  .results__sidebar {
    order: 2;
  }

  .results__main {
    order: 1;
  }
}

@media (max-width: 960px) {
  .hero {
    flex-direction: column;
  }

  .hero__stats {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .major-search-page {
    padding: 16px 12px 32px;
  }

  .hero {
    padding: 24px;
  }

  .filters {
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  }

  .mobile-mask {
    position: fixed;
    inset: 0;
    background: rgba(15, 27, 68, 0.92);
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
    z-index: 200;
  }

  .mobile-mask__content {
    background: #fff;
    padding: 24px;
    border-radius: 16px;
    text-align: center;
    max-width: 320px;
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.25);
  }

  .mobile-mask__content h3 {
    margin-bottom: 12px;
  }
}
</style>
