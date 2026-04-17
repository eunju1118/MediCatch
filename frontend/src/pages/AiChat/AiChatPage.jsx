import { useState, useRef, useEffect, useCallback } from 'react'
import { chatApi, createChatStream } from '../../services/api'
import styles from './AiChatPage.module.css'

const INITIAL_MESSAGE = {
  role: 'assistant',
  content: '안녕하세요! Medicatch AI 건강 어시스턴트입니다. 건강검진 결과나 진료 기록에 대해 궁금한 점을 물어보세요.',
}

function AiChatPage() {
  const [messages, setMessages] = useState([INITIAL_MESSAGE])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const [useStream, setUseStream] = useState(true)
  const bottomRef = useRef(null)
  const abortRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  useEffect(() => {
    return () => abortRef.current?.()
  }, [])

  const appendAssistantChunk = useCallback((chunk) => {
    setMessages((prev) => {
      const last = prev[prev.length - 1]
      if (last?.role === 'assistant' && last._streaming) {
        return [...prev.slice(0, -1), { ...last, content: last.content + chunk }]
      }
      return [...prev, { role: 'assistant', content: chunk, _streaming: true }]
    })
  }, [])

  const finalizeStreaming = useCallback(() => {
    setMessages((prev) => {
      const last = prev[prev.length - 1]
      if (last?._streaming) {
        const { _streaming, ...rest } = last
        return [...prev.slice(0, -1), rest]
      }
      return prev
    })
    setLoading(false)
  }, [])

  const handleSend = async () => {
    if (!input.trim() || loading) return

    const userMsg = { role: 'user', content: input.trim() }
    const history = messages
      .filter((m) => !m._streaming)
      .map(({ role, content }) => ({ role, content }))

    setMessages((prev) => [...prev, userMsg])
    setInput('')
    setLoading(true)

    if (useStream) {
      abortRef.current = createChatStream(
        userMsg.content,
        appendAssistantChunk,
        finalizeStreaming,
        (err) => {
          console.error('SSE error, falling back to sync:', err)
          // fallback to sync
          chatApi
            .sendMessage({ message: userMsg.content, conversationHistory: history })
            .then((res) => {
              setMessages((prev) => [...prev, { role: 'assistant', content: res.data?.message ?? res.message ?? '응답을 받았습니다.' }])
            })
            .catch(() => {
              setMessages((prev) => [...prev, { role: 'assistant', content: '죄송합니다. 일시적인 오류가 발생했습니다.' }])
            })
            .finally(() => setLoading(false))
        }
      )
    } else {
      try {
        const res = await chatApi.sendMessage({ message: userMsg.content, conversationHistory: history })
        const content = res.data?.message ?? res.message ?? '응답을 받았습니다.'
        setMessages((prev) => [...prev, { role: 'assistant', content }])
      } catch {
        setMessages((prev) => [...prev, { role: 'assistant', content: '죄송합니다. 일시적인 오류가 발생했습니다. 다시 시도해 주세요.' }])
      } finally {
        setLoading(false)
      }
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.header}>
        <div>
          <h1 className={styles.title}>AI 건강 채팅</h1>
          <p className={styles.subtitle}>GPT-4o 기반 개인화 건강 상담</p>
        </div>
        <label className={styles.streamToggle}>
          <input
            type="checkbox"
            checked={useStream}
            onChange={(e) => setUseStream(e.target.checked)}
          />
          스트리밍 모드
        </label>
      </div>

      <div className={styles.chatBox}>
        <div className={styles.messages}>
          {messages.map((msg, i) => (
            <div
              key={i}
              className={`${styles.bubble} ${msg.role === 'user' ? styles.user : styles.assistant}`}
            >
              {msg.role === 'assistant' && <span className={styles.avatar}>AI</span>}
              <p className={styles.text}>
                {msg.content}
                {msg._streaming && <span className={styles.cursor}>|</span>}
              </p>
            </div>
          ))}
          {loading && !messages[messages.length - 1]?._streaming && (
            <div className={`${styles.bubble} ${styles.assistant}`}>
              <span className={styles.avatar}>AI</span>
              <p className={styles.typing}>답변 작성 중...</p>
            </div>
          )}
          <div ref={bottomRef} />
        </div>

        <div className={styles.inputRow}>
          <textarea
            className={styles.input}
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="건강에 대해 무엇이든 물어보세요... (Enter: 전송, Shift+Enter: 줄바꿈)"
            rows={2}
          />
          <button className={styles.sendBtn} onClick={handleSend} disabled={loading}>
            전송
          </button>
        </div>
      </div>
    </div>
  )
}

export default AiChatPage
