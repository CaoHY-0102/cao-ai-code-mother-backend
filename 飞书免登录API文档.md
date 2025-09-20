# 飞书免登录功能API文档

## 概述

飞书免登录功能通过飞书官方接口验证用户身份，自动关联或创建系统用户，实现一键登录。整个流程分为四个步骤：飞书客户端触发→后端校验→数据同步→生成凭证。

## 核心流程

### 1. 前端触发飞书授权
前端集成飞书JS SDK，引导用户点击「飞书登录」，通过SDK获取飞书返回的临时授权code。

### 2. 后端校验用户身份
后端接收前端传来的code，结合飞书开放平台的应用凭证，调用飞书官方接口换取用户完整信息。

### 3. 数据同步与用户关联
- 同步飞书用户信息到`lark_user_info`表
- 根据`unionId`关联或创建系统用户到`user`表
- 建立飞书用户与系统用户的关联关系

### 4. 生成登录凭证
生成系统登录令牌，返回给前端，实现免登录。

## API接口

### 1. 生成飞书授权URL

**接口地址：** `GET /api/user/lark/login`

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| state | String | 否 | 状态参数，用于防CSRF攻击 |

**响应示例：**
```json
{
  "code": 0,
  "data": "https://open.feishu.cn/open-apis/authen/v1/index?app_id=cli_a84cc126327ad00b&redirect_uri=http%3A%2F%2Flocalhost%3A8123%2Fapi%2Fuser%2FlarkCallback&state=123",
  "message": "ok"
}
```

### 2. 飞书免登录接口

**接口地址：** `POST /api/user/lark/login`

**请求参数：**
```json
{
  "code": "飞书授权码",
  "state": "状态参数（可选）"
}
```

**响应示例：**
```json
{
  "code": 0,
  "data": {
    "id": 1234567890,
    "userAccount": "ou_89abc123456",
    "userName": "张三",
    "userAvatar": "https://lf123.com/avatar.jpg",
    "userRole": "user",
    "createTime": "2024-01-01T10:00:00",
    "updateTime": "2024-01-01T10:00:00"
  },
  "message": "ok"
}
```

### 3. 飞书登录回调接口

**接口地址：** `GET /api/user/larkCallback`

**请求参数：**
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| code | String | 是 | 飞书授权码 |
| state | String | 否 | 状态参数 |

**功能：** 处理飞书授权回调，自动完成登录并重定向到前端页面。

### 4. 获取当前登录用户

**接口地址：** `GET /api/user/get/login`

**响应示例：**
```json
{
  "code": 0,
  "data": {
    "id": 1234567890,
    "userAccount": "ou_89abc123456",
    "userName": "张三",
    "userAvatar": "https://lf123.com/avatar.jpg",
    "userRole": "user"
  },
  "message": "ok"
}
```

## 数据库设计

### lark_user_info表（飞书用户信息）
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| userId | VARCHAR | 飞书用户ID |
| name | VARCHAR | 用户姓名 |
| enName | VARCHAR | 用户英文名 |
| avatarUrl | VARCHAR | 用户头像URL |
| openId | VARCHAR | 应用内唯一标识 |
| unionId | VARCHAR | 飞书全域唯一标识（核心关联字段） |
| email | VARCHAR | 用户邮箱 |
| mobile | VARCHAR | 手机号 |
| tenantKey | VARCHAR | 租户Key |
| employeeNo | VARCHAR | 员工工号 |
| createTime | DATETIME | 创建时间 |
| updateTime | DATETIME | 更新时间 |
| isDelete | TINYINT | 是否删除 |

### user表（系统用户）
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键ID |
| userAccount | VARCHAR | 账号（使用unionId） |
| userPassword | VARCHAR | 密码（随机生成） |
| userName | VARCHAR | 用户昵称 |
| userAvatar | VARCHAR | 用户头像 |
| userRole | VARCHAR | 用户角色 |
| lark_union_id | VARCHAR | 飞书用户统一ID（关联字段） |
| createTime | DATETIME | 创建时间 |
| updateTime | DATETIME | 更新时间 |
| isDelete | TINYINT | 是否删除 |

## 配置说明

### application.yml配置
```yaml
# 飞书配置
lark:
  clientId: cli_a84cc126327ad00b  # 飞书应用App ID
  clientSecret: YwN3sTu3ogC0PGaQgCw2igKg7NFxVcuC  # 飞书应用App Secret
  redirectUri: http://localhost:8123/api/user/larkCallback  # 授权回调地址
```

### 飞书开放平台配置
1. 在飞书开放平台创建应用
2. 配置应用权限：`user:read`
3. 设置回调地址：`http://localhost:8123/api/user/larkCallback`
4. 获取App ID和App Secret

## 使用示例

### 前端集成示例

```javascript
// 1. 获取飞书授权URL
async function getLarkAuthUrl() {
  const response = await fetch('/api/user/lark/login?state=web_login');
  const result = await response.json();
  return result.data;
}

// 2. 打开飞书授权页面
function openLarkAuth() {
  getLarkAuthUrl().then(url => {
    window.open(url, '_blank');
  });
}

// 3. 处理授权码登录
async function larkLogin(code) {
  const response = await fetch('/api/user/lark/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      code: code,
      state: 'web_login'
    })
  });
  
  const result = await response.json();
  if (result.code === 0) {
    console.log('登录成功', result.data);
    // 存储用户信息，跳转到主页面
  } else {
    console.error('登录失败', result.message);
  }
}
```

### 移动端集成示例

```javascript
// 使用飞书移动端SDK
import { getAuthCode } from '@larksuiteoapi/js-sdk';

async function larkMobileLogin() {
  try {
    const { code } = await getAuthCode();
    await larkLogin(code);
  } catch (error) {
    console.error('飞书登录失败', error);
  }
}
```

## 错误处理

### 常见错误码
| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 40001 | 无效的授权码 | 检查授权码是否过期或已使用 |
| 40002 | 应用配置错误 | 检查App ID和App Secret配置 |
| 40003 | 权限不足 | 检查应用是否申请了user:read权限 |
| 50001 | 飞书API调用失败 | 检查网络连接和飞书服务状态 |

### 异常处理示例
```java
try {
    LoginUserVO loginUserVO = larkService.loginByCode(code, state, request);
    return ResultUtils.success(loginUserVO);
} catch (BusinessException e) {
    log.error("飞书登录失败: {}", e.getMessage());
    return ResultUtils.error(e.getCode(), e.getMessage());
} catch (Exception e) {
    log.error("系统异常", e);
    return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
}
```

## 安全注意事项

1. **授权码有效期**：飞书授权码有效期很短（通常5分钟），需要及时使用
2. **状态参数验证**：使用state参数防止CSRF攻击
3. **HTTPS要求**：生产环境必须使用HTTPS
4. **敏感信息保护**：不要在前端暴露App Secret
5. **用户信息更新**：定期同步飞书用户信息，保持数据一致性

## 测试说明

### 测试页面
访问 `http://localhost:8123/lark-login-test.html` 进行功能测试

### 测试步骤
1. 启动后端服务
2. 访问测试页面
3. 点击"测试生成授权URL"验证配置
4. 点击"飞书授权登录"完成授权流程
5. 测试免登录功能
6. 验证用户信息获取

### 调试日志
后端会输出详细的调试日志，包括：
- 飞书API调用日志
- 用户信息同步日志
- 错误信息和异常堆栈

## 部署注意事项

1. **环境变量**：生产环境使用环境变量管理敏感配置
2. **域名配置**：确保回调地址域名正确配置
3. **SSL证书**：生产环境必须配置SSL证书
4. **防火墙**：确保服务器可以访问飞书API
5. **监控告警**：配置API调用监控和异常告警

## 常见问题

### Q1: 授权码获取失败
**A:** 检查飞书应用配置，确保回调地址正确，权限申请完整。

### Q2: 用户信息同步失败
**A:** 检查数据库连接，确保表结构正确，字段映射无误。

### Q3: 登录状态丢失
**A:** 检查Session配置，确保Redis连接正常，Session超时设置合理。

### Q4: 跨域问题
**A:** 配置CORS，允许前端域名访问后端API。

---

**注意：** 本功能需要飞书企业账号和开放平台应用，个人用户无法直接使用。建议在测试环境充分验证后再部署到生产环境。
