import http from './http'

export interface LoginPayload {
  username: string
  password: string
}

export const login = (data: LoginPayload) => http.post('/auth/login', data)
export const fetchCurrentUser = () => http.get('/auth/me')
export const register = (data: Record<string, any>) => http.post('/auth/register', data)
