# Common API

## Common Response Code

| Status Code | Status Name           | Description                                                         |
|-------------|-----------------------|---------------------------------------------------------------------|
| 200         | OK                    | the request has succeeded                                           |
| 201         | Created               | the request has succeeded and has led to the creation of a resource |
| 400         | Bad Request           | the request payload is invalid                                      |
| 401         | Unauthorized          | the API Key header is invalid                                       |
| 404         | Not Found             | the requested path does not exist                                   |
| 409         | Conflict              | the request is conflicted with the current state                    |
| 500         | Internal Server Error | something wrong in the server side                                  |

### Response Body Data Structure For Error

| Field       | Type    | Description                         |
|-------------|---------|-------------------------------------|
| `timestamp` | string  | ISO 8601 Timestamp string formatted |
| `status`    | integer | HTTP status code                    |
| `error`     | string  | error type                          |
| `message`   | string  | error message                       |
| `path`      | string  | the requested path                  |

#### Example response body

```json
{
  "timestamp": "2024-07-15T07:06:45.929+00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Resource type not found",
  "path": "/rbac/v1/resource-types/xxx"
}
```

## Pagination API

Method: `GET`

Request Parameters:

| Parameter | Type    | Rules                                                                  | Description                                 | Example                               |
|-----------|---------|------------------------------------------------------------------------|---------------------------------------------|---------------------------------------|
| `page`    | integer | - starts from 0<br>- optional with default value is 0                  | the page number                             | `GET /rbac/v1/resource-types?page=0`  |
| `size`    | integer | - must be positive number (minimum 1)<br>- optional with default is 10 | the page size. the length of the slice data | `GET /rbac/v1/resource-types?size=15` |

Response Body Format:

| Field                     | Type            | Rules    | Description                                 |
|---------------------------|-----------------|----------|---------------------------------------------|
| `results`                 | array of object | required | contains the multiple content data          |
| `metadata`                | object          | required | contains the pagination data                |
| `metadata.totalCount`     | integer         | required | the number of data from all pages           |
| `metadata.totalPageCount` | integer         | required | the number of pages                         |
| `metadata.size`           | integer         | required | the page size. the length of the slice data |

Example response data:

```json
{
  "results": [
    {
      "key": "Term",
      "description": "Term",
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ]
    }
  ],
  "metadata": {
    "totalCount": 100,
    "totalPageCount": 10,
    "size": 10
  }
}
```

# Project API

Set of APIs to manage project. It is only accessible by **HMG-Role Admin**

## API Functions

**Context Path**: `/admin/v1/projects`

| Function        | Description                          |
|-----------------|--------------------------------------|
| `POST /`        | create a new project                 |
| `GET /`         | retrieve a paginated list of project |
| `GET /{key}`    | retrieve a single project by key     |
| `PUT /{key}`    | replace an existing project entirely |
| `DELETE /{key}` | delete a project by key              |

## Data Structure

| Field  | Type   | Rules    | Description                        |
|--------|--------|----------|------------------------------------|
| `key`  | string | required | ID of the project                  |
| `name` | string | required | Human readable name of the project |

### Example Data

```json
{
  "key": "hmg-notice",
  "name": "HMG Notice"
}
```

**Context Path**: `/admin/v1/projects?multiple=true`

| Function   | Description                            |
|------------|----------------------------------------|
| `POST /`   | create bulk new project                |
| `PUT /`    | replace bulk existing project entirely |
| `DELETE /` | delete bulk project                    |

## Data Structure

| Field  | Type   | Rules    | Description                        |
|--------|--------|----------|------------------------------------|
| `key`  | string | required | ID of the project                  |
| `name` | string | required | Human readable name of the project |

### Example Data

```json
[{
  "key": "hmg-notice",
  "name": "HMG Notice"
}]
```

# Member API

Set of APIs to manage member of the project. It is only accessible by **HMG-Role Admin**

## API Functions

**Context Path**: `/admin/v1/projects/{projectKey}/members`

**Path Variable**:

- `projectKey`: ID of the project

| Function        | Description                         |
|-----------------|-------------------------------------|
| `POST /`        | create a new member                 |
| `GET /`         | retrieve a paginated list of member |
| `GET /{key}`    | retrieve a single member by key     |
| `PUT /{key}`    | replace an existing member entirely |
| `DELETE /{key}` | delete a member by key              |

## Data Structure

| Field    | Type   | Rules                                                                                                | Description                                                                                                                         |
|----------|--------|------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `key`    | string | required, unique                                                                                     | ID of the member                                                                                                                    |
| `name`   | string | required                                                                                             | Human readable name of the member                                                                                                   |
| `apiKey` | string | required, global unique                                                                              | To be used for accessing the other APIs                                                                                             |

### Example Data

```json
{
  "key": "hmg-notice-admin-1",
  "name": "Shin Tae-yong",
  "apiKey": "5db2bab3-2cf4-4b96-aaa0-3b908f9590dc"
}
```

**Context Path**: `/admin/v1/projects/{projectKey}/members?multiple=true`

**Path Variable**:

- `projectKey`: ID of the project

| Function   | Description                              |
|------------|------------------------------------------|
| `POST /`   | create bulk of new member                |
| `PUT /`    | replace bulk of existing member entirely |
| `DELETE /` | delete bulk of existing member           |

## Data Structure

| Field    | Type   | Rules                                                                                                | Description                                                                                                                         |
|----------|--------|------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `key`    | string | required, unique                                                                                     | ID of the member                                                                                                                    |
| `name`   | string | required                                                                                             | Human readable name of the member                                                                                                   |
| `apiKey` | string | required, global unique                                                                              | To be used for accessing the other APIs                                                                                             |

### Example Data

```json
[{
  "key": "hmg-notice-admin-1",
  "name": "Shin Tae-yong",
  "apiKey": "5db2bab3-2cf4-4b96-aaa0-3b908f9590dc"
}]
```

# Role Based Access Control

## Permission Checking API

This is the main API entrypoint to check permission for a set of resources

### API Functions

Context Path: `/rbac/v1/permissions`

| Function                         | Description         |
|----------------------------------|---------------------|
| `POST /?flattenResponseFormat=&` | to check permission |

#### Parameters

* `flattenResponseFormat`
    * description: to enable flatten response format
    * type: `boolean` \(true\|false\)
    * rules: optional, default value is `flattenResponseFormat=false`
    * example: `flattenResponseFormat=true`

### Request Body

#### Data Structure

| Field                        | Type            | Rules    | Description                                                                                   |
|------------------------------|-----------------|----------|-----------------------------------------------------------------------------------------------|
| `user`                       | object          | required | the user whose permissions are being checked                                                  |
| `user.id`                    | string          | optional | ID of the user                                                                                |
| `user.scope`                 | string          | optional | User policy scope                                                                             |
| `user.roles`                 | array of string | required | The roles attached to this user                                                               |
| `resources`                  | array of object | required | List of resources that the user is attempting to access.                                      |
| `resources[].actions`        | array of string | required | List of actions being performed on the resource                                               |
| `resources[].resource`       | object          | required | The resource object                                                                           |
| `resources[].resource.id`    | string          | required | ID of the resource                                                                            |
| `resources[].resource.type`  | string          | required | Resource kind.<br>This is used to determine the resource policy that applies to this resource |
| `resources[].resource.scope` | string          | optional | Resource policy scope                                                                         |

#### Example

```json
{
  "user": {
    "id": "alice@hmg",
    "roles": [
      "ADMIN"
    ],
    "scope": "CCI"
  },
  "resources": [
    {
      "resource": {
        "id": "CCI-FAQ-1",
        "type": "FAQ",
        "scope": "CCI"
      },
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ]
    }
  ]
}
```

### Response Body with `flattenResponseFormat=false`

#### Data Structure

| Field                              | Type            | Rules                                           | Description                                                      |
|------------------------------------|-----------------|-------------------------------------------------|------------------------------------------------------------------|
| `results`                          | array of object | required                                        | the container array for the results                              |
| `results[].resource`               | object          | required                                        | the requested resource object                                    |
| `results[].resource.id`            | string          | required                                        | the resource ID                                                  |
| `results[].resource.type`          | string          | required                                        | the resource type                                                |
| `results[].resource.scope`         | string          | optional                                        | the resource scope                                               |
| `results[].actionEffects`          | object          | required                                        | the permission result of the action based from the policy        |
| `results[].actionEffects[].role`   | string          | required                                        | the role of the user                                             |
| `results[].actionEffects[].action` | string          | required                                        | the requested action by the user                                 |
| `results[].actionEffects[].effect` | string          | - required<br>- allowed values: `ALLOW`, `DENY` | the permission effect result of the action based from the policy |

#### Example

```json
{
  "results": [
    {
      "resource": {
        "id": "CCI-FAQ-1",
        "type": "FAQ",
        "scope": "CCI"
      },
      "actionEffects": [
        {
          "role": "ADMIN",
          "action": "view",
          "effect": "ALLOW"
        },
        {
          "role": "ADMIN",
          "action": "update",
          "effect": "ALLOW"
        },
        {
          "role": "ADMIN",
          "action": "create",
          "effect": "ALLOW"
        },
        {
          "role": "ADMIN",
          "action": "delete",
          "effect": "ALLOW"
        }
      ]
    }
  ]
}
```

### Response Body with `flattenResponseFormat=true`

#### Data Structure

| Field              | Type            | Rules                                             | Description                                                      |
|--------------------|-----------------|---------------------------------------------------|------------------------------------------------------------------|
| `results`          | array of object | required                                          | the container array for the results                              |
| `results[].id`     | string          | required                                          | the resource ID                                                  |
| `results[].type`   | string          | required                                          | the resource type                                                |
| `results[].scope`  | string          | optional                                          | the resource scope                                               |
| `results[].role`   | string          | optional                                          | the role of the use                                              |
| `results[].action` | string          | required                                          | the requested action by the user                                 |
| `results[].effect` | string          | \- required<br>\- allowed values: `ALLOW`, `DENY` | the permission effect result of the action based from the policy |

#### Example

```json
{
  "results": [
    {
      "id": "CCI-FAQ-1",
      "type": "FAQ",
      "scope": "CCI",
      "role": "ADMIN",
      "action": "view",
      "effect": "ALLOW"
    },
    {
      "key": "CCI-FAQ-1",
      "type": "FAQ",
      "scope": "CCI",
      "role": "ADMIN",
      "action": "create",
      "effect": "DENY"
    },
    {
      "key": "CCI-FAQ-1",
      "type": "FAQ",
      "scope": "CCI",
      "role": "ADMIN",
      "action": "update",
      "effect": "DENY"
    },
    {
      "key": "CCI-FAQ-1",
      "type": "FAQ",
      "scope": "CCI",
      "role": "ADMIN",
      "action": "delete",
      "effect": "DENY"
    }
  ]
}
```

## Resource Type API

### API Functions

Context Path: `/rbac/v1/resource-types`

| Function        | Description                                |
|-----------------|--------------------------------------------|
| `POST /`        | create a new resource type                 |
| `GET /`         | retrieve a list (page) of resource type    |
| `GET /{key}`    | retrieve a single resource type by key     |
| `PUT /{key}`    | replace an existing resource type entirely |
| `DELETE /{key}` | delete a resource type by key              |

### Data Structure

| Field         | Type            | Rules    | Description                                     |
|---------------|-----------------|----------|-------------------------------------------------|
| `key`         | string          | required | ID of the resource type                         |
| `description` | string          | optional | Description of the resource type                |
| `actions`     | array of string | required | List of actions being available on the resource |

### Example Data

```json
{
  "key": "FAQ",
  "description": "Frequently Asked Question",
  "actions": [
    "view",
    "update",
    "create",
    "delete"
  ]
}
```

### Create Multiple Resource Type

#### API

`POST /rbac/v1/resource-types?multiple=true`

#### Example Data Request

```json

  [
    {
      "key": "FAQ",
      "description": "Frequently Asked Question",
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ]
    },
    {
      "key": "Term",
      "description": "Term",
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ]
    }
  ]

```

### Update Multiple Resource Type

#### API

`PUT /rbac/v1/resource-types?multiple=true`

#### Example Data Request

```json

 [
    {
      "key": "FAQ",
      "description": "Frequently Asked Question",
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ]
    },
    {
      "key": "Term",
      "description": "Term",
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ]
    }
  ]

```

### Delete Multiple Resource Type

#### API

`DELETE /rbac/v1/resource-types?multiple=true`

#### Example Data Request

```json
{
  "keys": ["FAQ", "Term"]
}

```

#### Parameter

* `multiple`
    * description: flag to enable multiple creation mode
    * type: `boolean` \(true\|false\)
    * rules:
        * optional, default value is `multiple=false` means single resource (not array)
        * the request body must be an array of object with max entry 50
        * if any <span style="color:#e11d21;">**error** </span>happens, then <span style="color:#e11d21;">**rollback
          ** </span>for all



#### Example Data Response

Response Code: `201`

```json
[
  {
    "key": "FAQ",
    "description": "Frequently Asked Question",
    "actions": [
      "view",
      "update",
      "create",
      "delete"
    ]
  },
  {
    "key": "Term",
    "description": "Term",
    "actions": [
      "view",
      "update",
      "create",
      "delete"
    ]
  }
]
```

## Policy API

### API Functions

Context Path: `/rbac/v1/policies`

| Function        | Description                                |
|-----------------|--------------------------------------------|
| `POST /`        | create a new policy                        |
| `GET /`         | retrieve a list (page) of policy           |
| `GET /{key}`    | retrieve a single policy by key            |
| `PUT /{key}`    | replace an existing policy entirely by key |
| `DELETE /{key}` | delete a policy by key                     |

### Data Structure

| Field          | Type            | Rules                                                                                 | Description                                                                         |
|----------------|-----------------|---------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| `key`          | string          | required                                                                              | ID of the policy                                                                    |
| `description`  | string          | optional                                                                              | Description of the policy                                                           |
| `resourceType` | string          | required                                                                              | key from resource type or resource type                                             |
| `scope`        | string          | optional                                                                              | the scope that user and resource must match                                         |
| `actions`      | array of string | \- required<br>\- source value must be from the resource type<br>\- size minimum is 1 | the actions that are defined from the resource type                                 |
| `roles`        | array of string | \- required<br>\- size minimum is 1                                                   | the role of the user                                                                |
| `effect`       | string          | \- required<br>\- values are `ALLOW`, `DENY`                                          | the policy result effect when the resource type and action and user roles are match |

### Example Data

```json
{
  "key": "FAQ_ADMIN_POLICY",
  "description": "FAQ resource policy for Admin",
  "resourceType": "FAQ",
  "scope": "CCI",
  "actions": [
    "view",
    "update",
    "create",
    "delete"
  ],
  "roles": [
    "ADMIN"
  ],
  "effect": "ALLOW"
}
```

### Create Multiple / Bulk 

Context Path:
`POST /rbac/v1/policies?multiple=true`

#### Example Data Request

```json

  [
    {
      "key": "FAQ_ADMIN_POLICY",
      "description": "Frequently Asked Question Policy",
      "scope": "CCI",
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ],
      "roles": [
        "ADMIN"
      ],
      "effect": "ALLOW"
    },
    {
      "key": "Term_Policy",
      "description": "Term",
      "scope": "notice",
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ],
      "roles": [
         "STAFF"
      ],
      "effect": "ALLOW"
    }
  ]

```

### Update Multiple / Bulk

Context Path:
`PUT /rbac/v1/policies?multiple=true`

#### Example Data Request

```json

  [
    {
      "key": "FAQ_ADMIN_POLICY",
      "description": "Frequently Asked Question Policy",
      "scope": "CCI",
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ],
      "roles": [
        "ADMIN"
      ],
      "effect": "ALLOW"
    },
    {
      "key": "Term_Policy",
      "description": "Term",
      "scope": "notice",
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ],
      "roles": [
         "STAFF"
      ],
      "effect": "ALLOW"
    }
  ]

```

### Delete Multiple / Bulk

Context Path:
`DELETE /rbac/v1/policies?multiple=true`

#### Example Data Request

```json
{
  "keys": ["FAQ_ADMIN_POLICY", "Term_Policy"]
}

```

#### Parameter

* `multiple`
    * description: flag to enable multiple creation mode
    * type: `boolean` \(true\|false\)
    * rules:
        * optional, default value is `multiple=false` means single resource (not array)
        * the request body must be an array of object with max entry 50
        * if any <span style="color:#e11d21;">**error** </span>happens, then <span style="color:#e11d21;">**rollback
          ** </span>for all


## Role API

### API Functions

Context Path: `/rbac/v1/roles`

| Function         | Description                               |
|------------------|-------------------------------------------|
| `POST /`         | create a new role                         |
| `GET /`          | retrieve a list (page) of role            |
| `GET /{name}`    | retrieve a single role by name            |
| `PUT /{name}`    | replace an existing role entirely by name |
| `DELETE /{name}` | delete a role by name                     |

### Data Structure

| Field         | Type   | Rules    | Description             |
|---------------|--------|----------|-------------------------|
| `name`        | string | required | Name of the Role        |
| `description` | string | optional | description of the role |

### Example Data

```json
{
  "name": "admin",
  "description": "Admin Role"
}
```


### Create Multiple / Bulk

Context Path:
`POST /rbac/v1/roles?multiple=true`

#### Example Data Request

```json

  [
    {
      "name": "admin",
      "description": "Admin Role"
    },
    {
      "name": "staff",
      "description": "Staff Role"
    }
  ]

```

### Update Multiple / Bulk

Context Path:
`PUT /rbac/v1/roles?multiple=true`

#### Example Data Request

```json

  [
    {
      "key": "FAQ_ADMIN_POLICY",
      "description": "Frequently Asked Question Policy",
      "scope": "CCI",
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ],
      "roles": [
        "ADMIN"
      ],
      "effect": "ALLOW"
    },
    {
      "key": "Term_Policy",
      "description": "Term",
      "scope": "notice",
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ],
      "roles": [
         "STAFF"
      ],
      "effect": "ALLOW"
    }
  ]

```

### Delete Multiple / Bulk

Context Path:
`DELETE /rbac/v1/policies?multiple=true`

#### Example Data Request

```json
{
  "keys": ["FAQ_ADMIN_POLICY", "Term_Policy"]
}

```

#### Parameter

* `multiple`
    * description: flag to enable multiple creation mode
    * type: `boolean` \(true\|false\)
    * rules:
        * optional, default value is `multiple=false` means single resource (not array)
        * the request body must be an array of object with max entry 50
        * if any <span style="color:#e11d21;">**error** </span>happens, then <span style="color:#e11d21;">**rollback
          ** </span>for all

## User API

### API Functions

Context Path: `/rbac/v1/users`

| Function        | Description                              |
|-----------------|------------------------------------------|
| `POST /`        | create a new user                        |
| `GET /`         | retrieve a list (page) of user           |
| `GET /{key}`    | retrieve a single user by key            |
| `PUT /{key}`    | replace an existing user entirely by key |
| `DELETE /{key}` | delete a user by key                     |

### Data Structure

| Field   | Type            | Rules                               | Description         |
|---------|-----------------|-------------------------------------|---------------------|
| `key`   | string          | required                            | ID of the User      |
| `name`  | string          | optional                            | Name of the User    |
| `roles` | array of string | \- required<br>\- size minimum is 1 | role(s) of the user |
| `scope` | string          | optional                            | scope of the user   |


### Example Data

```json
{
  "key": "shintaeyong@hyundai.com",
  "name": "Shin Tae Yong",
  "roles": [
    "MEMBER"
  ],
  "scope": "cci"
}
```

### Create Multiple / Bulk

Context Path:
`POST /rbac/v1/users?multiple=true`

#### Example Data Request

```json

  [
    {
      "key": "shintaeyong@hyundai.com",
      "name": "Shin Tae Yong",
      "roles": ["STAFF"]
    },
    {
      "key": "kevin@hyundai.com",
      "name": "Kevin Yang",
      "roles": ["ADMIN"]
    }
  ]

```

### Update Multiple / Bulk

Context Path:
`PUT /rbac/v1/users?multiple=true`

#### Example Data Request

```json

[
  {
    "key": "shintaeyong@hyundai.com",
    "name": "Shin Tae Yong",
    "roles": ["STAFF"]
  },
  {
    "key": "kevin@hyundai.com",
    "name": "Kevin Yang",
    "roles": ["ADMIN"]
  }
]

```

### Delete Multiple / Bulk

Context Path:
`DELETE /rbac/v1/users?multiple=true`

#### Example Data Request

```json
{
  "keys": ["shintaeyong@hyundai.com", "kevin@hyundai.com"]
}

```

#### Parameter

* `multiple`
    * description: flag to enable multiple creation mode
    * type: `boolean` \(true\|false\)
    * rules:
        * optional, default value is `multiple=false` means single resource (not array)
        * the request body must be an array of object with max entry 50
        * if any <span style="color:#e11d21;">**error** </span>happens, then <span style="color:#e11d21;">**rollback
          ** </span>for all

# Attribute Based Access Control

## Permission Checking API

This is the main API entrypoint to check permission for a set of resources

### API Functions

Context Path: `/abac/v1/permissions`

| Function                         | Description         |
|----------------------------------|---------------------|
| `POST /?flattenResponseFormat=&` | to check permission |

#### Parameters

* `flattenResponseFormat`
    * description: to enable flatten response format
    * type: `boolean` \(true\|false\)
    * rules: optional, default value is `flattenResponseFormat=false`
    * example: `flattenResponseFormat=true`

### Request Body

#### Data Structure

| Field                             | Type            | Rules    | Description                                                                                                    |
|-----------------------------------|-----------------|----------|----------------------------------------------------------------------------------------------------------------|
| `user`                            | object          | required | the user whose permissions are being checked                                                                   |
| `user.id`                         | string          | optional | ID of the user                                                                                                 |
| `user.scope`                      | string          | optional | User policy scope                                                                                              |
| `user.attributes`                 | object          | required | Free-form context data about this user.<br>ABAC Policy rule conditions are evaluated based on these values.    |
| `resources`                       | array of object | required | List of resources that the user is attempting to access.                                                       |
| `resources[].actions`             | array of string | required | List of actions being performed on the resource                                                                |
| `resources[].resource`            | object          | required | The resource object                                                                                            |
| `resources[].resource.id`         | string          | required | ID of the resource                                                                                             |
| `resources[].resource.scope`      | string          | optional | Resource policy scope                                                                                          |
| `resources[].resource.attributes` | object          | required | Free-form context data about this resource.<br>ABAC Policy rule conditions are evaluated based on these values |

#### Example

```json
{
  "user": {
    "id": "alice@kia",
    "scope": "CCI",
    "attributes": {
      "department": "it_service_planning",
      "company": "KIA",
      "country": "USA"
    }
  },
  "resources": [
    {
      "resource": {
        "id": "CCI-FAQ-1",
        "scope": "CCI",
        "attributes": {
          "country": "USA",
          "ownerId": "john",
          "team": "website"
        }
      },
      "actions": [
        "view",
        "update",
        "create",
        "delete"
      ]
    }
  ]
}
```

### Response Body with `flattenResponseFormat=false`

#### Data Structure

| Field                               | Type            | Description                                                      |
|-------------------------------------|-----------------|------------------------------------------------------------------|
| `results`                           | array of object | the container array for the results                              |
| `results[].resource`                | object          | the requested resource object                                    |
| `results[].resource.id`             | string          | the resource instance id                                         |
| `results[].resource.resourceSet`    | string          | the matched resource-set key                                     |
| `results[].resource.scope`          | string          | the resource scope                                               |
| `results[].actionEffects`           | object          | the permission result of the action based from the policy        |
| `results[].actionEffects[].userId`  | string          | the user instance id                                             |
| `results[].actionEffects[].userSet` | string          | the matched user-set                                             |
| `results[].actionEffects[].action`  | string          | the requested action by the user                                 |
| `results[].actionEffects[].effect`  | string          | the permission effect result of the action based from the policy |

#### Example

```json
{
  "results": [
    {
      "resource": {
        "id": "CCI-FAQ-1",
        "resourceSet": "FAQ",
        "scope": "CCI"
      },
      "actionEffects": [
        {
          "userId": "alice@kia",
          "userSet": "KIA_USA",
          "action": "view",
          "effect": "ALLOW"
        },
        {
          "userId": "alice@kia",
          "userSet": "KIA_USA",
          "action": "update",
          "effect": "ALLOW"
        },
        {
          "userId": "alice@kia",
          "userSet": "KIA_USA",
          "action": "create",
          "effect": "ALLOW"
        },
        {
          "userId": "alice@kia",
          "userSet": "KIA_USA",
          "action": "delete",
          "effect": "ALLOW"
        }
      ]
    }
  ]
}
```

### Response Body with `flattenResponseFormat=true`

#### Data Structure

| Field                   | Type            | Description                                                      |
|-------------------------|-----------------|------------------------------------------------------------------|
| `results`               | array of object | the container array for the results                              |
| `results[].resourceId`  | string          | the resource instance id                                         |
| `results[].resourceSet` | string          | the resource-set key                                             |
| `results[].userId`      | string          | the user instance id                                             |
| `results[].userSet`     | string          | the user-set key                                                 |
| `results[].scope`       | string          | the resource scope                                               |
| `results[].action`      | string          | the requested action by the user                                 |
| `results[].effect`      | string          | the permission effect result of the action based from the policy |

#### Example

```json
{
  "results": [
    {
      "resourceId": "CCI-FAQ-1",
      "resourceSet": "FAQ",
      "scope": "CCI",
      "userId": "alice@kia",
      "userSet": "KIA_USA",
      "action": "view",
      "effect": "ALLOW"
    },
    {
      "resourceId": "CCI-FAQ-1",
      "resourceSet": "FAQ",
      "scope": "CCI",
      "userId": "alice@kia",
      "userSet": "KIA_USA",
      "action": "create",
      "effect": "DENY"
    },
    {
      "resourceId": "CCI-FAQ-1",
      "resourceSet": "FAQ",
      "scope": "CCI",
      "userId": "alice@kia",
      "userSet": "KIA_USA",
      "action": "update",
      "effect": "DENY"
    },
    {
      "resourceId": "CCI-FAQ-1",
      "resourceSet": "FAQ",
      "scope": "CCI",
      "userId": "alice@kia",
      "userSet": "KIA_USA",
      "action": "delete",
      "effect": "DENY"
    }
  ]
}
```

## Resource Set API

Resource set is a set of condition groups that match specific characteristics of the resource.

### API Functions

Context Path: `/abac/v1/resource-sets`

| Function        | Description                                      |
|-----------------|--------------------------------------------------|
| `POST /`        | create a new resource-set                        |
| `GET /`         | retrieve a list (page) of resource-set           |
| `GET /{key}`    | retrieve a single resource-set by key            |
| `PUT /{key}`    | replace an existing resource-set entirely by key |
| `DELETE /{key}` | delete a resource-set by key                     |

### Data Structure

| Field                            | Type            | Rules                                                                                                                                                                      | Description                                                          |
|----------------------------------|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| `key`                            | string          | required                                                                                                                                                                   | ID of the resource set                                               |
| `description`                    | string          | optional                                                                                                                                                                   | Description of the resource set                                      |
| `actions`                        | array of string | required                                                                                                                                                                   | List of actions being available on the resource                      |
| `conditionGroupOperator`         | string          | \- required<br>\- values are `AND`, `OR`                                                                                                                                   | logical operator for the condition group                             |
| `conditionGroup`                 | array of object | \- required<br>\- at least 1 entry                                                                                                                                         | set of the conditions                                                |
| `conditionGroup[].left`          | object          | required                                                                                                                                                                   | the left operand object                                              |
| `conditionGroup[].left.operand`  | string          | \- required<br>\- if the `type` is `attribute` then it must be from resource attribute <br>\- if the `type` is not `attribute` then it is literal value                    | left operand of the expression attribute reference or literal value  |
| `conditionGroup[].left.type`     | string          | \- required<br>\- allowed values: <ul><li>`attribute`</li><li>`string`</li><li>`number`</li><li>`boolean`</li></ul>                                                        | the type of the operand                                              |
| `conditionGroup[].operator`      | string          | \- required<br>\- allowed values: <ul><li>`equals`, `not-equals`, </li><li>`less-than`, `less-than-or-equals`, </li><li>`greater-than`, `greater-than-or-equals`</li></ul> | condition operator to be applied                                     |
| `conditionGroup[].right`         | object          | required                                                                                                                                                                   | the right operand object                                             |
| `conditionGroup[].right.operand` | string          | \- required<br>\- if the `type` is `attribute` then it must be from resource attribute <br>\- if the `type` is `value` then it is the constant value                       | right operand of the expression attribute reference or literal value |
| `conditionGroup[].right.type`    | string          | \- required<br>\- allowed values: <ul><li>`attribute`</li><li>`string`</li><li>`number`</li><li>`boolean`</li></ul>                                                        | the type of the operand                                              |

### Example Data

```json
{
  "key": "FAQ_USA",
  "description": "all FAQ resources from USA",
  "actions": [
    "create",
    "read",
    "update",
    "delete"
  ],
  "conditionGroupOperator": "AND",
  "conditionGroup": [
    {
      "left": {
        "operand": "country",
        "type": "attribute"
      },
      "operator": "equals",
      "right": {
        "operand": "USA",
        "type": "value"
      }
    }
  ]
}
```

## User Set API

User set is a set of condition groups that match specific characteristics of the user. User set is a kind of derived
role.

### API Functions

Context Path: `/abac/v1/user-sets`

| Function        | Description                                  |
|-----------------|----------------------------------------------|
| `POST /`        | create a new user-set                        |
| `GET /`         | retrieve a list (page) of user-set           |
| `GET /{key}`    | retrieve a single user-set by key            |
| `PUT /{key}`    | replace an existing user-set entirely by key |
| `DELETE /{key}` | delete a user-set by key                     |

### Data Structure

| Field                            | Type            | Rules                                                                                                                                                                      | Description                                                          |
|----------------------------------|-----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| `key`                            | string          | required                                                                                                                                                                   | ID of the user set                                                   |
| `description`                    | string          | optional                                                                                                                                                                   | Description of the user set                                          |
| `conditionGroupOperator`         | string          | \- required<br>\- allowed values are `AND`, `OR`                                                                                                                           | logical operator for the condition group                             |
| `conditionGroup`                 | array of object | required                                                                                                                                                                   | set of condition groups                                              |
| `conditionGroup[].left`          | object          | required                                                                                                                                                                   | the left operand object                                              |
| `conditionGroup[].left.operand`  | string          | \- required<br>\- if the `type` is `attribute` then it must be from resource attribute <br>\- if the `type` not `attribute` then it is literal value                       | left operand of the expression attribute reference or literal value  |
| `conditionGroup[].left.type`     | string          | \- required<br>\- allowed values: <ul><li>`attribute`</li><li>`string`</li><li>`number`</li><li>`boolean`</li></ul>                                                        | the type of the operand                                              |
| `conditionGroup[].operator`      | string          | \- required<br>\- allowed values: <ul><li>`equals`, `not-equals`, </li><li>`less-than`, `less-than-or-equals`, </li><li>`greater-than`, `greater-than-or-equals`</li></ul> | condition operator to be applied                                     |
| `conditionGroup[].right`         | object          | required                                                                                                                                                                   | the right operand object                                             |
| `conditionGroup[].right.operand` | string          | \- required<br>\- if the `type` is `attribute` then it must be from resource attribute <br>\- if the `type` is `value` then it is the constant value                       | right operand of the expression attribute reference or literal value |
| `conditionGroup[].right.type`    | string          | \- required<br>\- allowed values: <ul><li>`attribute`</li><li>`string`</li><li>`number`</li><li>`boolean`</li></ul>                                                        | the type of the operand                                              |

### Example Data

```json
{
  "key": "IT_DEPT_OUTSIDE_USA",
  "description": "all users from department IT",
  "conditionGroupOperator": "AND",
  "conditionGroup": [
    {
      "left": {
        "operand": "department",
        "type": "attribute"
      },
      "operator": "equals",
      "right": {
        "operand": "it_service_planning",
        "type": "value"
      }
    },
    {
      "left": {
        "operand": "country",
        "type": "attribute"
      },
      "operator": "not-equals",
      "right": {
        "operand": "USA",
        "type": "value"
      }
    }
  ]
}
```

## Policy API

### API Functions

Context Path: `/abac/v1/policies`

| Function        | Description                                |
|-----------------|--------------------------------------------|
| `POST /`        | create a new policy                        |
| `GET /`         | retrieve a list (page) of policy           |
| `GET /{key}`    | retrieve a single policy by key            |
| `PUT /{key}`    | replace an existing policy entirely by key |
| `DELETE /{key}` | delete a policy by key                     |

### Data Structure

| Field         | Type            | Rules                                                                                 | Description                                                                       |
|---------------|-----------------|---------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| `key`         | string          | required                                                                              | ID of the policy                                                                  |
| `description` | string          | optional                                                                              | Description of the policy                                                         |
| `resourceSet` | string          | required                                                                              | key of the resource set                                                           |
| `scope`       | string          | optional                                                                              | the scope that user and resource must match                                       |
| `actions`     | array of string | \- required<br>\- source value must be from the resource type<br>\- size minimum is 1 | the actions that are defined from the resource type                               |
| `userSets`    | array of string | \- required<br>\- size minimum is 1<br>\- value source must from user\-set key        | the user-set key                                                                  |
| `effect`      | string          | \- required<br>\- values are `ALLOW`, `DENY`                                          | the policy result effect when the resource type and action and user-set are match |

### Example Data

```json
{
  "key": "POLICY_FAQ_USA_IT_DEPT",
  "scope": "CCI",
  "resourceSet": "FAQ_USA",
  "actions": [
    "view",
    "update",
    "create",
    "delete"
  ],
  "userSets": [
    "IT_DEPT"
  ],
  "effect": "ALLOW"
}
```
