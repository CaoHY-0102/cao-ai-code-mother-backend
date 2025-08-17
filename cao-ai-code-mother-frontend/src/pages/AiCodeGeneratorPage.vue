<script setup lang="ts">
import { ref } from 'vue'
import { aiCodeGeneratorController } from '@/api'
import { message } from 'ant-design-vue'

const userMessage = ref('')
const generatedCode = ref('')
const loading = ref(false)

const generateCode = async () => {
  if (!userMessage.value.trim()) {
    message.warning('请输入您的需求')
    return
  }

  loading.value = true
  try {
    const response = await aiCodeGeneratorController.generateCode(userMessage.value)
    generatedCode.value = response.data || ''
    message.success('代码生成成功')
  } catch (error) {
    console.error('生成代码失败:', error)
    message.error('代码生成失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main>
    <div class="ai-code-generator">
      <h1>AI代码生成器</h1>
      <div class="input-section">
        <a-textarea
          v-model:value="userMessage"
          placeholder="请输入您的代码需求，例如：写一个计算斐波那契数列的函数"
          :rows="4"
          :auto-size="{ minRows: 4, maxRows: 8 }"
        />
        <a-button
          type="primary"
          @click="generateCode"
          :loading="loading"
          style="margin-top: 16px"
        >
          生成代码
        </a-button>
      </div>
      <div class="output-section" v-if="generatedCode">
        <h2>生成的代码：</h2>
        <a-card>
          <pre><code>{{ generatedCode }}</code></pre>
        </a-card>
      </div>
    </div>
  </main>
</template>

<style scoped>
.ai-code-generator {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.ai-code-generator h1 {
  color: #1890ff;
  margin-bottom: 20px;
  text-align: center;
}

.input-section {
  margin-bottom: 30px;
}

.output-section {
  margin-top: 30px;
}

.output-section h2 {
  color: #333;
  margin-bottom: 15px;
}

pre {
  background-color: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
}

code {
  font-family: 'Courier New', monospace;
}
</style>