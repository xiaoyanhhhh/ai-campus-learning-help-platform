# AI赋能校园学习互助与课程资源推荐平台

基于 Spring Boot 3.x、JDK 17+、Maven、Spring Security、MyBatis、Thymeleaf、H2 实现的 JavaEE 期末大作业项目。

## 已实现功能

- 用户注册、登录、退出、BCrypt 密码加密、RBAC 角色访问控制。
- 管理员、教师/助教、学生三类角色；后台支持用户状态审核/禁用和角色分配。
- 课程资源上传、关键词检索、课程/类型/标签筛选、分页、排序、收藏、浏览量、学生资源审核。
- 学习求助发布、认领、提交解答、发布者确认完成、积分事务结算。
- 求助详情支持评论/补充说明，保留创建、认领、提交、完成等关键时间。
- 个人中心支持资料维护和积分明细查看。
- AI 功能：智能推荐、资源摘要、内容审核、求助 AI 辅助解答，支持 `application.yml` 配置 mock/真实大模型参数。
- AI 调用日志：记录功能、用户、请求摘要、响应摘要、耗时、状态。
- AOP 操作审计：记录资源查看、资源审核、求助认领、积分相关关键操作。
- 积分明细、排行榜、后台审计日志、用户治理、热门资源等统计展示。
- 统一异常页面、参数校验、缓存示例和文件上传。

## 本地运行

```powershell
mvn -s settings-local.xml spring-boot:run
```

浏览器访问：

```text
http://localhost:8080
```

测试账号：

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | admin | 123456 |
| 教师/助教 | teacher | 123456 |
| 助教 | assistant | 123456 |
| 学生 | student | 123456 |
| 学生 | student2 | 123456 |
| 互助者 | helper | 123456 |
| 待审核账号 | pending_student | 123456 |
| 禁用账号 | disabled_student | 123456 |

系统启动时会幂等补充演示数据：7 门课程、十余条课程资源、多种审核状态资源、不同状态的学习求助、评论/补充说明、积分流水和审计演示数据。

H2 控制台：

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:file:./data/aihelp
User: sa
Password: 空
```

## 打包

```powershell
mvn -s settings-local.xml clean package
java -jar target/aihelp-1.0.0.jar
```

## AI 配置

默认启用 mock 模式，可直接演示。真实大模型接入时不要硬编码 Key，使用环境变量。项目兼容 OpenAI-compatible 的 `chat/completions` 接口：

```yaml
app:
  ai:
    provider: ${AI_PROVIDER:mock}
    base-url: ${AI_BASE_URL:}
    api-key: ${AI_API_KEY:}
    model: ${AI_MODEL:gpt-4o-mini}
    timeout-ms: ${AI_TIMEOUT_MS:10000}
    mock-enabled: ${AI_MOCK_ENABLED:true}
```

PowerShell 示例：

```powershell
$env:AI_MOCK_ENABLED="false"
$env:AI_PROVIDER="openai-compatible"
$env:AI_BASE_URL="https://你的模型服务商地址/v1"
$env:AI_MODEL="你的模型名称"
$env:AI_API_KEY="你的API Key"
mvn -s settings-local.xml spring-boot:run
```

进入求助详情页后点击“AI 辅助解答”，系统会把求助标题、描述、标签、评论和平台已审核资源一起发送给模型，要求模型优先基于平台资料给出解题思路和引用资源，并将调用记录写入 `ai_call_log`。

## 云服务器部署概要

1. 云服务器安装 JDK 17 或更高版本。
2. 上传 `target/aihelp-1.0.0.jar` 到 `/opt/aihelp/aihelp.jar`。
3. 上传 `deploy/aihelp.service` 到 `/etc/systemd/system/aihelp.service`。
4. 执行：

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now aihelp
sudo systemctl status aihelp
```

5. 如需域名或 80 端口反向代理，使用 `deploy/nginx-aihelp.conf`。

## 项目结构

```text
src/main/java/com/campus/aihelp
  aop          AOP 审计注解与切面
  config       安全、初始化、异常处理、配置属性
  controller   页面与业务入口
  domain       领域对象
  mapper       MyBatis 数据访问
  service      业务层、AI 服务、文件存储、积分事务
src/main/resources
  templates    Thymeleaf 页面
  static/css   页面样式
  schema.sql   建表脚本
deploy         云部署示例
docs           课程报告草稿
```
