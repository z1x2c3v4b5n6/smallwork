import http from './http'

export const searchMajors = (params: Record<string, any>) => http.get('/student/search', { params })
export const fetchMajorDetail = (universityId: number, majorId: number) =>
  http.get(`/student/universities/${universityId}/majors/${majorId}`)
export const recommend = (data: { currentRank: number; subjects?: string; province?: string }) =>
  http.post('/student/recommend', data)
