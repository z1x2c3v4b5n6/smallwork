import http from './http'

export interface MajorOption {
  id: number
  name: string
}

export const searchMajors = (params: Record<string, any>) => http.get('/student/search', { params })
export const fetchMajorDetail = (universityId: number, majorId: number) =>
  http.get(`/student/universities/${universityId}/majors/${majorId}`)
export const recommend = (data: { currentRank: number; subjects?: string; province?: string }) =>
  http.post('/student/recommend', data)
export const fetchStudentProfile = () => http.get('/student/profile')
export const updateStudentProfile = (data: Record<string, any>) => http.put('/student/profile', data)
export const fetchProfileSummary = () => http.get('/student/profile/summary')
export const fetchMajorOptions = (params?: { keyword?: string }) =>
  http.get<MajorOption[]>('/student/majors/options', { params })
