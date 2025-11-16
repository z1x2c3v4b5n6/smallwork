export const SUBJECT_OPTIONS = ['物理', '化学', '地理', '生物', '历史', '政治', '技术']

export const parseSubjectString = (value?: string | null) => {
  if (!value) {
    return [] as string[]
  }
  return value
    .replace(/[，,、]/g, ' ')
    .split(/\s+/)
    .map((item) => item.trim())
    .filter((item) => !!item)
}

export const stringifySubjects = (subjects: string[]) => {
  if (!subjects || !subjects.length) {
    return ''
  }
  return subjects.join(' ')
}
