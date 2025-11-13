import { defineStore } from 'pinia'

interface UserInfo {
  username: string
  role: string
  token: string
}

export const useAuthStore = defineStore('auth', {
  state: (): UserInfo => ({
    username: '',
    role: '',
    token: localStorage.getItem('token') || ''
  }),
  actions: {
    setUser(info: Partial<UserInfo>) {
      if (info.username !== undefined) this.username = info.username
      if (info.role !== undefined) this.role = info.role
      if (info.token !== undefined) {
        this.token = info.token
        if (info.token) {
          localStorage.setItem('token', info.token)
        } else {
          localStorage.removeItem('token')
        }
      }
    },
    logout() {
      this.username = ''
      this.role = ''
      this.setUser({ token: '' })
    }
  }
})
