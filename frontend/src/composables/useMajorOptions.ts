import { ref } from 'vue'
import { fetchMajorOptions, type MajorOption } from '../api/student'

export const useMajorOptions = () => {
  const majorOptions = ref<MajorOption[]>([])
  const majorLoading = ref(false)

  const loadMajorOptions = async (keyword = '') => {
    const query = keyword?.trim()
    majorLoading.value = true
    try {
      const params = query ? { keyword: query } : undefined
      const { data } = await fetchMajorOptions(params)
      majorOptions.value = data
    } catch (err) {
      console.error('加载专业列表失败', err)
    } finally {
      majorLoading.value = false
    }
  }

  return { majorOptions, majorLoading, loadMajorOptions }
}
