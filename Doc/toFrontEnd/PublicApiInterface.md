# 公开API接口文档

## 概述

本文档描述了Group Center系统的公开API接口，这些接口无需认证即可访问，主要用于前端获取用户列表和项目订阅功能。

## 基础信息

- **基础URL**: `/api/public`
- **认证**: 无需认证
- **响应格式**: JSON
- **编码**: UTF-8

## 通用响应格式

所有接口都返回统一的响应格式：

```json
{
  "serverVersion": "1.0.0",
  "isAuthenticated": true,
  "haveError": false,
  "isSucceed": true,
  "result": {}
}
```

字段说明：
- `serverVersion`: 服务器版本号
- `isAuthenticated`: 是否已认证（公开接口始终为true）
- `haveError`: 是否有错误
- `isSucceed`: 是否成功
- `result`: 实际返回数据

## 接口列表

### 1. 获取用户列表

获取系统中所有用户的用户名和英文名列表。

**接口信息**
- **URL**: `/api/public/users`
- **方法**: GET
- **描述**: 获取所有用户的用户名和英文名列表

**请求参数**
无

**响应示例 - 成功**
```json
{
  "serverVersion": "1.0.0",
  "isAuthenticated": true,
  "haveError": false,
  "isSucceed": true,
  "result": [
    {
      "name": "张三",
      "nameEng": "zhangsan"
    },
    {
      "name": "李四",
      "nameEng": "lisi"
    }
  ]
}
```

**响应示例 - 错误**
```json
{
  "serverVersion": "1.0.0",
  "isAuthenticated": true,
  "haveError": true,
  "isSucceed": false,
  "result": "获取用户列表失败: 系统错误"
}
```

### 2. 订阅项目

用户订阅指定项目。

**接口信息**
- **URL**: `/api/public/projects/subscribe`
- **方法**: POST
- **描述**: 用户订阅指定项目

**请求参数**
```json
{
  "projectId": 12345,
  "userName": "张三"
}
```

字段说明：
- `projectId`: 项目ID（必填，数字类型）
- `userName`: 用户名（必填，字符串类型，支持中文或英文）

**用户名匹配规则**：
- 优先匹配英文名（usernameEng）
- 如果英文名没匹配到，再匹配中文名（name）
- 如果都没匹配到，返回用户不存在错误

**响应示例 - 成功**
```json
{
  "serverVersion": "1.0.0",
  "isAuthenticated": true,
  "haveError": false,
  "isSucceed": true,
  "result": {
    "success": true,
    "message": "订阅成功",
    "projectId": 12345,
    "userName": "张三",
    "userNameEng": "zhangsan"
  }
}
```

**响应示例 - 用户不存在**
```json
{
  "serverVersion": "1.0.0",
  "isAuthenticated": true,
  "haveError": true,
  "isSucceed": false,
  "result": {
    "success": false,
    "message": "用户不存在: 张三"
  }
}
```

**响应示例 - 重复订阅**
```json
{
  "serverVersion": "1.0.0",
  "isAuthenticated": true,
  "haveError": true,
  "isSucceed": false,
  "result": {
    "success": false,
    "message": "用户 张三 已经订阅了项目 12345"
  }
}
```

### 3. 取消订阅项目

用户取消订阅指定项目。

**接口信息**
- **URL**: `/api/public/projects/unsubscribe`
- **方法**: POST
- **描述**: 用户取消订阅指定项目

**请求参数**
```json
{
  "projectId": 12345,
  "userName": "张三"
}
```

字段说明：
- `projectId`: 项目ID（必填，数字类型）
- `userName`: 用户名（必填，字符串类型，支持中文或英文）

**响应示例 - 成功**
```json
{
  "serverVersion": "1.0.0",
  "isAuthenticated": true,
  "haveError": false,
  "isSucceed": true,
  "result": {
    "success": true,
    "message": "取消订阅成功",
    "projectId": 12345,
    "userName": "张三",
    "userNameEng": "zhangsan"
  }
}
```

**响应示例 - 用户不存在**
```json
{
  "serverVersion": "1.0.0",
  "isAuthenticated": true,
  "haveError": true,
  "isSucceed": false,
  "result": {
    "success": false,
    "message": "用户不存在: 张三"
  }
}
```

### 4. 获取用户订阅列表

获取指定用户订阅的所有项目列表。

**接口信息**
- **URL**: `/api/public/projects/subscriptions`
- **方法**: GET
- **描述**: 获取指定用户订阅的所有项目列表

**请求参数**
- `userName`: 用户名（查询参数，必填，字符串类型）

**URL示例**
```
/api/public/projects/subscriptions?userName=张三
```

**响应示例 - 成功**
```json
{
  "serverVersion": "1.0.0",
  "isAuthenticated": true,
  "haveError": false,
  "isSucceed": true,
  "result": {
    "userName": "张三",
    "subscriptions": ["12345", "67890"],
    "count": 2
  }
}
```

字段说明：
- `userName`: 查询的用户名
- `subscriptions`: 用户订阅的项目ID列表
- `count`: 订阅的项目数量

**响应示例 - 用户不存在**
```json
{
  "serverVersion": "1.0.0",
  "isAuthenticated": true,
  "haveError": true,
  "isSucceed": false,
  "result": "获取订阅列表失败: 用户不存在"
}
```

## 错误码说明

所有接口使用统一的错误处理机制，通过`haveError`和`isSucceed`字段标识操作状态。

常见错误情况：
1. **用户不存在**: 当传入的用户名无法匹配到系统中的用户时
2. **重复订阅**: 当用户已经订阅了某个项目时
3. **系统错误**: 服务器内部错误

## 使用示例

### 前端使用流程

1. **获取用户列表**
```javascript
// 获取用户列表供选择
fetch('/api/public/users')
  .then(response => response.json())
  .then(data => {
    if (data.isSucceed) {
      const userList = data.result;
      // 显示用户列表
    }
  });
```

2. **订阅项目**
```javascript
// 用户订阅项目
fetch('/api/public/projects/subscribe', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    projectId: 12345,
    userName: '张三'
  })
})
.then(response => response.json())
.then(data => {
  if (data.isSucceed) {
    alert('订阅成功');
  } else {
    alert(data.result.message);
  }
});
```

3. **获取用户订阅列表**
```javascript
// 查看用户订阅的项目
fetch('/api/public/projects/subscriptions?userName=张三')
  .then(response => response.json())
  .then(data => {
    if (data.isSucceed) {
      const subscriptions = data.result.subscriptions;
      // 显示用户订阅的项目
    }
  });
```

## 注意事项

1. **用户名匹配**: 支持中文和英文用户名，优先匹配英文名
2. **重复订阅**: 同一个用户不能重复订阅同一个项目
3. **项目ID**: 需要从数据库或其他接口获取有效的项目ID
4. **无认证**: 所有接口无需认证即可访问
5. **内存存储**: 订阅关系存储在内存中，重启服务会丢失

## 版本历史

- v1.0.0 (2025-10-13): 初始版本，提供用户列表和项目订阅功能