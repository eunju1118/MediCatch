import { useState, useRef, useEffect } from 'react'
import { chatApi } from '../../services/api'
import styles from './AiChatPage.module.css'

function AiChatPage() {
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      content: '안녕하세요! Medicatch AI 건강 어시스턴트입니다. 건강검진 결과나 진료 기록에 대해 궁금한 점을 물어보세요.',
    },
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = async () => {
    if (!input.trim() || loading) return

    const userMsg = { role: 'user', content: input.trim() }
    setMessages((prev) => [...prev, userMsg])
    setInput('')
    setLoading(true)

    try {
      const history = messages.map(({ role, content }) => ({ role, content }))
      const res = await chatApi.sendMessage({ message: userMsg.content, history })
      setMessages((prev) => [...prev, { role: 'assistant', content: res.reply }])
    } catch {
      setMessages((prev) => [
        ...prev,
        { role: 'assistant', content: '죄송합니다. 일시적인 오류가 발생했습니다. 다시 시도해 주세요.' },
      ])
    } finally {
      setLoading(false)
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
      <h1 className={styles.title}>AI 건강 채팅</h1>
      <p className={styles.subtitle}>GPT-4o 기반 개인화 건강 상담</p>

      <div className={styles.chatBox}>
        <div className={styles.messages}>
          {messages.map((msg, i) => (
            <div
              key={i}
              className={`${styles.bubble} ${msg.role === 'user' ? styles.user : styles.assistant}`}
            >
              {msg.role === 'assistant' && (
                <span className={styles.avatar}>AI</span>
              )}
              <p className={styles.text}>{msg.content}</p>
            </div>
          ))}
          {loading && (
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
