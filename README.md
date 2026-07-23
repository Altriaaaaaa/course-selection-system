# Course Selection System

高校选课管理系统，数据库原理课程项目（3 人小组），基于若依 RuoYi v4.8.3 二次开发。

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端 | Spring Boot 4.0.3 + MyBatis + Apache Shiro |
| 数据库 | MySQL 8.x + Druid |
| 前端 | Thymeleaf + Bootstrap 3 + jQuery + ECharts |
| 缓存 | Ehcache |
| 工具 | PageHelper / fastjson / Apache POI / PDFBox |

## 功能

三种角色 + RBAC 权限隔离：

| 管理员 | 教师 | 学生 |
|--------|------|------|
| 院系/学生/教师 CRUD | 开课申请 | 选课/退课 |
| 课程审核 | 课程公告 | 查看公告 |
| 授课分配 | 成绩录入 + 导出 Excel | 匿名评教 |
| 统计分析 + ECharts | 查看评教 | 成绩申诉 |
| 选课时间窗口配置 | 考试管理 | 成绩查询 + GPA |
| 安全审计 + 仪表盘 | 课程表 | PDF 成绩单 |
| | | 学分统计 |

## 数据库

12 张业务表 + 3 触发器 + 4 视图 + 1 存储过程

密码 BCrypt 加密，登录防暴破（5 次锁 10 分钟），XSS 过滤，AOP 审计日志。

## 快速开始

```bash
# 导入数据库
mysql -u root -p < deploy_package/sql/course_selection.sql

# 启动
cd RuoYi && mvn clean package -Dmaven.test.skip=true
java -jar ruoyi-admin/target/ruoyi-admin.jar
# http://localhost:80
```

演示账号：`admin / admin123`

## 文档

- [使用手册](使用手册.md) — 完整功能说明 + 课堂演示流程
- [ER 图](docs/er-diagram.md) — 数据库设计（5 实体 + 12 联系）
- [关系模式](docs/relation-schema.md) — 7 张业务表 + 范式分析
