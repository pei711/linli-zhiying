# 邻里智营--魔改点评

一个基于 Spring Boot 和 Vue 2 的 AI 本地生活与商家经营演示项目，包含点评式消费端、商家 AI 内容工具，以及可选的 MCP 查询接口。

[English](README.md)

## 功能

- 浏览店铺和分类、查看店铺详情、发现附近店铺。
- 浏览探店笔记，支持点赞、关注、签到、优惠券和秒杀订单等功能。
- 登录后创建和查看店铺预约。
- 使用可配置的 AI Skills 生成评价摘要、回复草稿、探店笔记草稿、优惠券文案和短视频脚本。
- 登录后的 AI 商家顾问可以调用业务工具；预约创建仍走当前登录用户的业务流程。
- 可选开启只读 MCP 查询接口，用于查询店铺、分类和优惠券。配置 `MCP_API_TOKEN` 前，MCP 接口默认关闭。

## 架构

```text
浏览器（Vue 2 + Element UI） -> Nginx -> Spring Boot REST API
                                             |-> MySQL 8
                                             |-> Redis 7 / Redisson
                                             |-> OpenAI 兼容模型 API（可选）
                                             `-> MCP HTTP 接口（可选，Bearer Token）
```

## Docker Compose 快速启动

需要 Docker Desktop 和 Compose v2。AI 模型密钥是可选的；不配置密钥时，浏览等功能仍可使用，但 AI 调用无法生成内容。

```bash
cp .env.example .env
# 编辑 .env；需要聊天和 AI Skills 时填写 AI_API_KEY。
docker compose up --build
```

打开 <http://localhost:8080>。前端通过同源地址访问后端。MySQL 映射到 `127.0.0.1:3307`；Redis 只在 Compose 网络内部开放。

首次启动会创建表结构和少量合成店铺/分类演示数据。MySQL 数据卷非空后不会再次执行初始化脚本。要重置演示数据库和 Redis 数据，运行 `docker compose down -v` 后重新启动。

## 不使用 Docker 运行后端

需要 Java 17+、Maven 3.9+、MySQL 8+ 和 Redis 7+。创建数据库后，按顺序执行：

```text
db/mysql/001-schema.sql
db/mysql/002-demo-data.sql
```

按需设置 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`REDIS_HOST` 和 `REDIS_PORT`，然后运行：

```bash
cd AI-dianping-backend
mvn spring-boot:run
```

使用 AI 功能还需要配置 `AI_BASE_URL`、`AI_API_KEY` 和 `AI_MODEL_NAME`。`MCP_API_TOKEN` 为可选项；未设置时 `/mcp` 返回 HTTP 503。启用后用 `Authorization: Bearer <token>` 认证。MCP 只注册只读的店铺、分类和优惠券查询工具。

## 测试

运行后端单元测试：

```bash
cd AI-dianping-backend
mvn test
```

测试不依赖本机 MySQL 或 Redis。GitHub Actions 会在 Java 17 环境运行相同的 Maven 测试命令。

## 配置项

| 变量 | 用途 | 默认值 |
| --- | --- | --- |
| `DB_URL` | JDBC 连接串 | Compose 本地 MySQL，端口 3307 |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号 | `hmdp` / 仅供本地开发的密码 |
| `REDIS_HOST` / `REDIS_PORT` | Redis 连接 | Compose 外默认为 `localhost:6379` |
| `AI_BASE_URL` | OpenAI 兼容 API 地址 | DashScope 兼容接口 |
| `AI_API_KEY` | 模型服务密钥 | 占位值，替换后才能调用 AI |
| `AI_MODEL_NAME` | 对话模型 | `qwen-flash` |
| `MCP_API_TOKEN` | MCP 接口 Bearer Token | 留空表示关闭 |

不要提交 `.env` 或生产凭据。部署到本机以外前，请修改开发环境数据库密码。

## 目录结构

- `AI-dianping-backend/`：Spring Boot API、业务服务、AI Skills、顾问工具和 MCP 服务。
- `AI-dianping-frontend/html/hmdp/`：Vue 静态前端。
- `AI-dianping-frontend/conf/nginx.conf`：本地及 Compose 使用的 Nginx 配置。
- `db/mysql/`：建表脚本和合成演示数据，不包含导入的用户账号、手机号或探店笔记。

## 当前限制

- 短信登录需要接入短信服务，仓库没有包含短信服务配置。
- AI 聊天和内容生成需要可用的模型服务密钥。
- 项目没有可核验的性能基准材料，因此不在此声明项目介绍中提到的性能百分比。
- 这是学习/演示项目。用于生产前还需评估认证、限流、存储和部署配置。

## 贡献与安全

贡献步骤见 [CONTRIBUTING.md](CONTRIBUTING.md)，漏洞报告方式见 [SECURITY.md](SECURITY.md)。源码来源见 [UPSTREAM.md](UPSTREAM.md)。本仓库使用 MIT License，详见 [LICENSE](LICENSE)。
前端第三方依赖声明见 [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md)。
