# 基于 Web 的高考志愿填报辅助系统

## 项目简介
本项目实现了一个前后端分离的高考志愿填报辅助系统，涵盖院校与专业信息管理、历年录取数据展示、按位次三段推荐、志愿方案管理以及 Excel 批量导入等功能。系统包含管理员和学生两种角色，管理员负责数据维护与导入，学生可以查询院校专业、查看录取趋势、获取推荐并管理自己的志愿方案。

系统整体架构：前端基于 Vue3 + Vite + TypeScript + Element Plus，后端使用 Spring Boot 3 + Spring MVC + Spring Security + MyBatis-Plus，数据库选用 MySQL。

## 技术栈
- **前端**：Vue 3、Vite、TypeScript、Element Plus、Pinia、Axios
- **后端**：Spring Boot 3、Spring MVC、Spring Security、MyBatis-Plus、Hibernate Validator
- **数据库**：MySQL 8+
- **Excel 解析**：Apache POI

## 环境要求
- JDK 17
- Maven 3.8+
- Node.js 18+（含 npm）
- MySQL 8 及以上

## 数据库初始化
1. 在 MySQL 中创建数据库：
   ```sql
   CREATE DATABASE gaokao_volunteer CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
   ```
2. 执行 `backend/src/main/resources/db/schema.sql`，完成所有表结构初始化并插入基础示例数据（含管理员与学生账号）。

示例账号（密码均为 `123456`，数据库中以明文存储，方便本地体验）：
- 管理员：用户名 `admin`
- 学生：用户名 `student`

## 后端运行（IntelliJ IDEA）
1. 使用 IDEA 打开 `backend` 目录作为 Maven 工程，等待依赖下载完成。
2. 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息（url、username、password）。
3. 右键运行主启动类 `com.example.gaokao.GaokaoApplication`。
4. 默认后端访问地址为 `http://localhost:8080`。

## 前端运行
```bash
cd frontend
pnpm install
pnpm dev
```
默认前端访问地址为 `http://localhost:5173`，已通过 Vite 代理将 `/api` 请求转发至后端。

## 功能概览
### 登录与权限
- `/api/auth/login` 登录，返回 JWT Token；未登录用户只能访问登录页。
- 管理员（ROLE_ADMIN）可访问后台管理、Excel 导入等接口和页面。
- 学生（ROLE_STUDENT）可访问检索、推荐、志愿方案等前台功能。

### 管理端功能
- 院校、专业、院校开设专业、录取数据的增删改查。
- Excel 批量导入院校/专业/历年录取数据。

### 学生端功能
- 条件检索院校专业、查看详情与历年录取趋势。
- 根据当前位次进行“冲/稳/保”三段推荐。
- 志愿方案的创建、查看、删除，以及方案内志愿的维护。

## Excel 导入说明
- 管理员登录后进入“数据管理 → Excel 导入”页面（`/admin/import`），选择本地 Excel 文件并点击“开始导入”。
- 模板字段顺序如下（从第一列开始）：
  1. 院校名称
  2. 院校省份
  3. 院校城市
  4. 院校层次
  5. 院校类型
  6. 是否双一流（是/否 或 true/false）
  7. 专业名称
  8. 专业类别
  9. 专业学科
  10. 选考要求
  11. 专业层次
  12. 批次
  13. 学制年限（整数）
  14. 学费（整数）
  15. 年份
  16. 最低分
  17. 最低位次
  18. 平均分
  19. 平均位次
  20. 录取人数

- 导入失败会返回错误提示并回滚事务；成功时返回导入的记录条数。

## 目录结构
```
backend/            # Spring Boot 后端工程
  src/main/java/com/example/gaokao
    config/        # 安全、JWT、跨域配置
    controller/    # 管理端、学生端、认证接口
    domain/        # 实体与 DTO
    mapper/        # MyBatis-Plus Mapper
    service/       # Service 与实现
  src/main/resources
    application.yml
    db/schema.sql  # 建表及初始化数据
frontend/           # Vue3 前端工程
  src/api           # 后端接口封装
  src/router        # 路由配置
  src/store         # Pinia 状态管理
  src/views         # 页面视图
```

至此即可启动整个毕业设计项目，管理员导入数据后，学生端即可进行志愿检索、推荐与方案管理。
