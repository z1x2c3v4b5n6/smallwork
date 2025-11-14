import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Login from '../views/Login.vue'
import MajorSearch from '../views/Student/MajorSearch.vue'
import Recommend from '../views/Student/Recommend.vue'
import PlanList from '../views/Student/PlanList.vue'
import UniversityManage from '../views/Admin/UniversityManage.vue'
import MajorManage from '../views/Admin/MajorManage.vue'
import AdmissionManage from '../views/Admin/AdmissionManage.vue'
import AdminImport from '../views/Admin/AdminImport.vue'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/student/search', component: MajorSearch },
  { path: '/student/recommend', component: Recommend },
  { path: '/student/plans', component: PlanList },
  { path: '/admin/universities', component: UniversityManage },
  { path: '/admin/majors', component: MajorManage },
  { path: '/admin/admissions', component: AdmissionManage },
  { path: '/admin/import', component: AdminImport }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
