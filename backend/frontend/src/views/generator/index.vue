<template>
  <div class="generator-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>密码生成器</span>
        </div>
      </template>

      <div class="generator-content">
        <!-- 生成的密码显示 -->
        <div class="password-display">
          <el-input
            v-model="generatedPassword"
            readonly
            size="large"
            class="password-input"
          >
            <template #append>
              <el-button @click="copyPassword">
                <el-icon><CopyDocument /></el-icon>
                复制
              </el-button>
            </template>
          </el-input>

          <div class="password-strength">
            <span class="strength-label">密码强度：</span>
            <el-rate
              v-model="passwordStrength"
              disabled
              :max="5"
              :colors="['#f56c6c', '#e6a23c', '#67c23a']"
            />
            <span class="strength-text">{{ strengthText }}</span>
          </div>
        </div>

        <!-- 密码配置 -->
        <div class="password-config">
          <el-form label-width="120px">
            <el-form-item label="密码长度">
              <el-slider
                v-model="passwordLength"
                :min="8"
                :max="64"
                :step="1"
                show-input
              />
            </el-form-item>

            <el-form-item label="包含字符">
              <el-checkbox v-model="config.uppercase">大写字母 (A-Z)</el-checkbox>
              <el-checkbox v-model="config.lowercase">小写字母 (a-z)</el-checkbox>
              <el-checkbox v-model="config.numbers">数字 (0-9)</el-checkbox>
              <el-checkbox v-model="config.symbols">特殊字符 (!@#$...)</el-checkbox>
            </el-form-item>

            <el-form-item label="排除字符">
              <el-input
                v-model="config.excludeChars"
                placeholder="输入要排除的字符"
              />
            </el-form-item>

            <el-form-item label="自定义字符">
              <el-input
                v-model="config.customChars"
                placeholder="输入自定义包含的字符"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" size="large" @click="generatePassword">
                <el-icon><Refresh /></el-icon>
                生成密码
              </el-button>
              <el-button size="large" @click="resetConfig">
                重置配置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 密码历史 -->
        <div class="password-history">
          <h3>生成历史</h3>
          <div class="history-list">
            <div
              v-for="(item, index) in passwordHistory"
              :key="index"
              class="history-item"
            >
              <div class="history-password">
                <span>{{ item.password }}</span>
                <el-button
                  type="primary"
                  link
                  size="small"
                  @click="copyText(item.password)"
                >
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
              </div>
              <div class="history-meta">
                <span class="history-time">{{ item.time }}</span>
                <el-rate
                  v-model="item.strength"
                  disabled
                  :max="5"
                  :colors="['#f56c6c', '#e6a23c', '#67c23a']"
                  size="small"
                />
              </div>
            </div>
          </div>
          <div v-if="passwordHistory.length === 0" class="empty-history">
            暂无生成历史
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'

const generatedPassword = ref('')
const passwordLength = ref(16)
const passwordStrength = ref(0)

const config = reactive({
    uppercase: true,
    lowercase: true,
    numbers: true,
    symbols: true,
    excludeChars: '',
    customChars: ''
})

const passwordHistory = ref([])

// 强度文本
const strengthText = computed(() => {
    const texts = ['非常弱', '弱', '中等', '强', '非常强']
    return texts[passwordStrength.value - 1] || '未生成'
})

// 生成密码
const generatePassword = () => {
    let charset = ''

    if (config.uppercase) charset += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
    if (config.lowercase) charset += 'abcdefghijklmnopqrstuvwxyz'
    if (config.numbers) charset += '0123456789'
    if (config.symbols) charset += '!@#$%^&*()_+-=[]{}|;:,.<>?'
    if (config.customChars) charset += config.customChars

    // 排除字符
    if (config.excludeChars) {
        charset = charset.split('').filter(c => !config.excludeChars.includes(c)).join('')
    }

    if (charset.length === 0) {
        ElMessage.warning('请至少选择一种字符类型')
        return
    }

    let password = ''
    const array = new Uint32Array(passwordLength.value)
    crypto.getRandomValues(array)

    for (let i = 0; i < passwordLength.value; i++) {
        password += charset[array[i] % charset.length]
    }

    generatedPassword.value = password
    passwordStrength.value = calculateStrength(password)

    // 添加到历史记录
    passwordHistory.value.unshift({
        password,
        strength: passwordStrength.value,
        time: new Date().toLocaleString('zh-CN')
    })

    // 只保留最近10条记录
    if (passwordHistory.value.length > 10) {
        passwordHistory.value.pop()
    }
}

// 计算密码强度
const calculateStrength = (password) => {
    let score = 0

    // 长度检查
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.length >= 16) score++

    // 字符类型检查
    if (/[a-z]/.test(password)) score++
    if (/[A-Z]/.test(password)) score++
    if (/[0-9]/.test(password)) score++
    if (/[^a-zA-Z0-9]/.test(password)) score++

    // 复杂度检查
    const uniqueChars = new Set(password).size
    if (uniqueChars >= password.length * 0.7) score++

    return Math.min(5, Math.ceil(score / 2))
}

// 复制密码
const copyPassword = async () => {
    if (!generatedPassword.value) {
        ElMessage.warning('请先生成密码')
        return
    }
    await copyText(generatedPassword.value)
}

// 复制文本
const copyText = async (text) => {
    try {
        await navigator.clipboard.writeText(text)
        ElMessage.success('已复制到剪贴板')
    } catch {
        ElMessage.error('复制失败')
    }
}

// 重置配置
const resetConfig = () => {
    passwordLength.value = 16
    Object.assign(config, {
        uppercase: true,
        lowercase: true,
        numbers: true,
        symbols: true,
        excludeChars: '',
        customChars: ''
    })
    generatedPassword.value = ''
    passwordStrength.value = 0
}

// 初始化时生成一个密码
generatePassword()
</script>

<style scoped>
.generator-container {
    padding: 20px 0;
}

.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.generator-content {
    max-width: 800px;
    margin: 0 auto;
}

.password-display {
    margin-bottom: 30px;
    padding: 20px;
    background: #f5f7fa;
    border-radius: 8px;
}

.password-input {
    font-family: monospace;
    font-size: 18px;
}

.password-strength {
    margin-top: 15px;
    display: flex;
    align-items: center;
    gap: 10px;
}

.strength-label {
    font-size: 14px;
    color: #606266;
}

.strength-text {
    font-size: 14px;
    color: #909399;
}

.password-config {
    margin-bottom: 30px;
}

.password-history {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
}

.password-history h3 {
    margin-bottom: 15px;
    color: #303133;
}

.history-list {
    max-height: 300px;
    overflow-y: auto;
}

.history-item {
    padding: 10px;
    border-bottom: 1px solid #ebeef5;
}

.history-item:last-child {
    border-bottom: none;
}

.history-password {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-family: monospace;
    font-size: 14px;
    color: #303133;
}

.history-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 5px;
}

.history-time {
    font-size: 12px;
    color: #909399;
}

.empty-history {
    text-align: center;
    color: #909399;
    padding: 20px;
}
</style>