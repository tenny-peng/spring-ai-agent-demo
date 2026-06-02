<template>
  <div class="chat-wrapper">
    <div class="chat" ref="chatRef">
      <div
        v-for="(msg, i) in messages"
        :key="i"
        :class="['msg', msg.role]"
      >
        {{ msg.content }}
        <span v-if="msg.role === 'assistant' && i === messages.length - 1 && streaming" class="cursor"></span>
      </div>
      <div v-if="loading" class="loading">思考中…</div>
    </div>
    <div class="input-bar">
      <input
        v-model="inputText"
        type="text"
        placeholder="输入你的问题…"
        :disabled="streaming"
        @keydown.enter="send"
      />
      <button :disabled="!inputText.trim() || streaming" @click="send">发送</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'

interface Message {
  role: 'user' | 'assistant'
  content: string
}

const inputText = ref('')
const messages = ref<Message[]>([])
const loading = ref(false)
const streaming = ref(false)
const chatRef = ref<HTMLElement | null>(null)

let es: EventSource | null = null

function scrollBottom() {
  nextTick(() => {
    if (chatRef.value) {
      chatRef.value.scrollTop = chatRef.value.scrollHeight
    }
  })
}

function send() {
  const text = inputText.value.trim()
  if (!text || streaming.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true
  streaming.value = true
  scrollBottom()

  const assistantMsg: Message = { role: 'assistant', content: '' }
  messages.value.push(assistantMsg)

  es = new EventSource('/chat/stream?query=' + encodeURIComponent(text))
  es.onmessage = (e) => {
    loading.value = false
    assistantMsg.content += e.data
    scrollBottom()
  }
  es.onerror = () => {
    es?.close()
    es = null
    streaming.value = false
    loading.value = false
    scrollBottom()
  }
  es.onopen = () => {
    loading.value = false
  }
}
</script>

<style scoped>
.chat-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  scroll-behavior: smooth;
}

.loading {
  text-align: center;
  color: #999;
  font-size: 14px;
  padding: 8px 0;
}

.msg {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  font-size: 15px;
  word-break: break-word;
  white-space: pre-wrap;
}

.msg.user {
  background: #e3f2fd;
  color: #1565c0;
  align-self: flex-end;
  border-bottom-right-radius: 4px;
}

.msg.assistant {
  background: #fff;
  color: #333;
  align-self: flex-start;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.cursor {
  display: inline-block;
  width: 2px;
  height: 1em;
  background: #666;
  margin-left: 2px;
  animation: blink 0.8s step-end infinite;
  vertical-align: text-bottom;
}

@keyframes blink {
  50% { opacity: 0; }
}

.input-bar {
  display: flex;
  gap: 10px;
  padding: 16px 0;
  flex-shrink: 0;
}

.input-bar input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 24px;
  font-size: 15px;
  outline: none;
  transition: border 0.2s;
}

.input-bar input:focus {
  border-color: #1976d2;
}

.input-bar button {
  padding: 12px 24px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.2s;
  flex-shrink: 0;
}

.input-bar button:hover {
  background: #1565c0;
}

.input-bar button:disabled {
  background: #90caf9;
  cursor: not-allowed;
}
</style>
