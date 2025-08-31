<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record, index }">
        <template v-if="column.dataIndex === 'userName'">
          <div v-if="editingKey === record.id">
            <a-input v-model:value="record.userName" />
          </div>
          <span v-else>{{ record.userName }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'userAvatar'">
          <div v-if="editingKey === record.id">
            <a-input v-model:value="record.userAvatar" />
          </div>
          <span v-else>
            <a-image :src="record.userAvatar" :width="120" />
          </span>
        </template>
        <template v-else-if="column.dataIndex === 'userProfile'">
          <div v-if="editingKey === record.id">
            <a-input v-model:value="record.userProfile" />
          </div>
          <span v-else>{{ record.userProfile }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="editingKey === record.id">
            <ASelect v-model:value="record.userRole" style="width: 120px">
              <ASelectOption value="user">普通用户</ASelectOption>
              <ASelectOption value="admin">管理员</ASelectOption>
            </ASelect>
          </div>
          <div v-else>
            <div v-if="record.userRole === 'admin'">
              <a-tag color="green">管理员</a-tag>
            </div>
            <div v-else>
              <a-tag color="blue">普通用户</a-tag>
            </div>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div v-if="editingKey === record.id">
            <a-button type="primary" @click="save(record)">保存</a-button>
            <a-button style="margin-left: 8px" @click="cancel(record, index)">取消</a-button>
          </div>
          <div v-else>
            <a-button type="primary" @click="edit(record)">编辑</a-button>
            <a-button danger style="margin-left: 8px" @click="doDelete(record.id)">删除</a-button>
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUser, listUserVoByPage, updateUser } from '@/api/userController.ts'
import { message, Select } from 'ant-design-vue'
import dayjs from 'dayjs'

const ASelect = Select
const ASelectOption = Select.Option

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 编辑状态
const editingKey = ref('')
const editingRecord = ref<API.UserVO | null>(null)

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 编辑数据
const edit = (record: API.UserVO) => {
  editingKey.value = record.id as string
  // 保存编辑前的数据，用于取消时恢复
  editingRecord.value = JSON.parse(JSON.stringify(record))
}

// 保存数据
const save = async (record: API.UserVO) => {
  try {
    const res = await updateUser({
      id: record.id,
      userName: record.userName,
      userAvatar: record.userAvatar,
      userProfile: record.userProfile,
      userRole: record.userRole,
    })
    if (res.data.code === 0) {
      message.success('保存成功')
      editingKey.value = ''
      editingRecord.value = null
      // 刷新数据
      fetchData()
    } else {
      message.error('保存失败: ' + res.data.message)
    }
  } catch (error) {
    message.error('保存失败: ' + error)
  }
}

// 取消编辑
const cancel = (record: API.UserVO, index: number) => {
  editingKey.value = ''
  // 恢复编辑前的数据
  if (editingRecord.value) {
    data.value[index] = editingRecord.value
  }
  editingRecord.value = null
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}
</style>
