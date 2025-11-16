import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import MajorSearch from '../views/Student/MajorSearch.vue'
import Recommend from '../views/Student/Recommend.vue'
import PlanList from '../views/Student/PlanList.vue'
import StudentProfile from '../views/Student/Profile.vue'
import UniversityManage from '../views/Admin/UniversityManage.vue'
import MajorManage from '../views/Admin/MajorManage.vue'
import AdmissionManage from '../views/Admin/AdmissionManage.vue'
import AdminImport from '../views/Admin/AdminImport.vue'
import StudentManage from '../views/Admin/StudentManage.vue'
import RecommendRule from '../views/Admin/RecommendRule.vue'

const routes: RouteRecordRaw[] = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/student/search', component: MajorSearch },
  { path: '/student/recommend', component: Recommend },
  { path: '/student/plans', component: PlanList },
  { path: '/student/profile', component: StudentProfile },
  { path: '/admin/universities', component: UniversityManage },
  { path: '/admin/majors', component: MajorManage },
  { path: '/admin/admissions', component: AdmissionManage },
  { path: '/admin/import', component: AdminImport },
  { path: '/admin/students', component: StudentManage },
  { path: '/admin/rules', component: RecommendRule }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
