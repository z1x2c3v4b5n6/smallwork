import http from './http'

export const listUniversities = () => http.get('/admin/universities')
export const createUniversity = (data: any) => http.post('/admin/universities', data)
export const updateUniversity = (id: number, data: any) => http.put(`/admin/universities/${id}`, data)
export const deleteUniversity = (id: number) => http.delete(`/admin/universities/${id}`)

export const listMajors = () => http.get('/admin/majors')
export const createMajor = (data: any) => http.post('/admin/majors', data)
export const updateMajor = (id: number, data: any) => http.put(`/admin/majors/${id}`, data)
export const deleteMajor = (id: number) => http.delete(`/admin/majors/${id}`)

export const listUniversityMajors = () => http.get('/admin/university-majors')
export const createUniversityMajor = (data: any) => http.post('/admin/university-majors', data)
export const updateUniversityMajor = (id: number, data: any) => http.put(`/admin/university-majors/${id}`, data)
export const deleteUniversityMajor = (id: number) => http.delete(`/admin/university-majors/${id}`)

export const listAdmissions = (params: Record<string, any>) => http.get('/admin/admissions', { params })
export const updateAdmission = (id: number, data: any) => http.put(`/admin/admissions/${id}`, data)
export const deleteAdmission = (id: number) => http.delete(`/admin/admissions/${id}`)

export const importMajorExcel = (file: File) => {
  const form = new FormData()
  form.append('file', file)
  return http.post('/admin/import/excel', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const importStudentExcel = (file: File, strategy: string) => {
  const form = new FormData()
  form.append('file', file)
  return http.post(`/admin/import/students?strategy=${strategy}`, form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export const fetchStudents = (params: Record<string, any>) => http.get('/admin/students', { params })
export const updateStudentProfile = (userId: number, data: Record<string, any>) =>
  http.put(`/admin/students/${userId}/profile`, data)
export const resetStudentPassword = (userId: number, data: Record<string, any>) =>
  http.post(`/admin/students/${userId}/reset-password`, data)
export const updateStudentStatus = (userId: number, data: { enabled: boolean }) =>
  http.patch(`/admin/students/${userId}/status`, data)
export const fetchRecommendRule = () => http.get('/admin/recommend-rule')
export const updateRecommendRule = (data: Record<string, any>) => http.put('/admin/recommend-rule', data)
