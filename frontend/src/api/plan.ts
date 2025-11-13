import http from './http'

export interface PlanPayload {
  name: string
}

export const listPlans = () => http.get('/student/plans')
export const createPlan = (data: PlanPayload) => http.post('/student/plans', data)
export const deletePlan = (id: number) => http.delete(`/student/plans/${id}`)
export const listPlanItems = (planId: number) => http.get(`/student/plans/${planId}/items`)
export const addPlanItem = (planId: number, data: any) => http.post(`/student/plans/${planId}/items`, data)
export const deletePlanItem = (planId: number, itemId: number) =>
  http.delete(`/student/plans/${planId}/items/${itemId}`)
