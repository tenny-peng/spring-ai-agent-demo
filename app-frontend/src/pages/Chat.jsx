// src/pages/Chat.jsx
import { useState, useEffect } from 'react';
import { Layout, message, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import SessionList from '../components/SessionList';
import MessageList from '../components/MessageList';
import MessageInput from '../components/MessageInput';
import UserMemoryDrawer from '../components/UserMemoryDrawer'
import client from '../api/client';

const { Sider, Content } = Layout;

function Chat() {
  const navigate = useNavigate();
  const username = localStorage.getItem('username');

  const [sessions, setSessions] = useState([]);
  const [currentSessionId, setCurrentSessionId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isStreaming, setIsStreaming] = useState(false);
  const [memoryDrawerOpen, setMemoryDrawerOpen] = useState(false);
  const [webSearchEnabled, setWebSearchEnabled] = useState(() => {
      return localStorage.getItem('webSearchEnabled') === 'true';
    });
  const handleWebSearchChange = (checked) => {
    setWebSearchEnabled(checked);
    localStorage.setItem('webSearchEnabled', checked);
  };

  // 加载会话列表
  const loadSessions = async () => {
    try {
      const response = await client.get('/conversation/list');
      const sessionsData = response.data;
      setSessions(sessionsData);
      if (sessionsData.length > 0 && !currentSessionId) {
        setCurrentSessionId(sessionsData[0].conversationId);
      }
    } catch (error) {
      message.error('加载会话列表失败');
    }
  };

  // 加载历史消息
  const loadMessages = async (conversationId) => {
    try {
      const response = await client.get(`/conversation/messages/${conversationId}`);
      const messagesData = response.data?.messages || [];
      const formattedMessages = messagesData.map(msg => ({
        role: msg.role,
        content: msg.content
      }));
      setMessages(formattedMessages);
    } catch (error) {
      console.error('加载历史消息失败:', error);
      setMessages([]);
    }
  };

    const handleNewSession = () => {
      const tempId = createTempSession();
      setMessages([]);
    };

    const createTempSession = () => {
      const tempId = `temp_${Date.now()}_${Math.random().toString(36).substr(2, 6)}`;
      const tempSession = {
        conversationId: tempId,
        title: '新对话',
        updatedAt: new Date().toISOString(),
        isTemp: true
      };
      setSessions(prev => [tempSession, ...prev]);
      setCurrentSessionId(tempId);
      return tempId;
    };

  // 重命名会话
  const handleRenameSession = async (conversationId, newTitle) => {
    const session = sessions.find(s => s.conversationId === conversationId);
    if (session?.isTemp) return;

    try {
      await client.post('/conversation/rename', { conversationId, newTitle });
      setSessions(prev => prev.map(s =>
        s.conversationId === conversationId ? { ...s, title: newTitle } : s
      ));
      message.success('重命名成功');
    } catch (error) {
      message.error('重命名失败');
    }
  };

  // 删除会话
  const handleDeleteSession = async (conversationId) => {
      // 临时会话直接删除
      const session = sessions.find(s => s.conversationId === conversationId);
      if (session?.isTemp) {
        const newSessions = sessions.filter(s => s.conversationId !== conversationId);
        setSessions(newSessions);
        if (currentSessionId === conversationId) {
          setCurrentSessionId(newSessions[0]?.conversationId || null);
          if (newSessions.length === 0) setMessages([]);
        }
        message.success('删除成功');
        return;
      }

      // 真实会话调用后端删除
      try {
        await client.delete(`/conversation/delete/${conversationId}`);
        const newSessions = sessions.filter(s => s.conversationId !== conversationId);
        setSessions(newSessions);
        if (currentSessionId === conversationId) {
          setCurrentSessionId(newSessions[0]?.conversationId || null);
          if (newSessions.length === 0) setMessages([]);
        }
        message.success('删除成功');
      } catch (error) {
        message.error('删除失败');
      }
    };

  // 发送消息
  const handleSendMessage = async (content) => {
    let targetId = currentSessionId;
    if (!targetId) {
        targetId = createTempSession();
    }

    const userMessage = { role: 'USER', content };
    setMessages(prev => [...prev, userMessage]);
    setLoading(true);

    try {
      const currentSession = sessions.find(s => s.conversationId === currentSessionId);
      let conversationIdToUse = currentSessionId;

      // 如果是临时会话，用临时 ID 发送（后端会自动创建并返回新 ID）
      await sendMessageWithSSE(conversationIdToUse, content, webSearchEnabled);
      await loadSessions();
    } catch (error) {
      console.error('发送失败:', error);
      message.error('发送失败，请重试');
      // 移除刚添加的用户消息和空的 AI 消息
      setMessages(prev => prev.slice(0, -2));
    } finally {
      setLoading(false);
    }
  };

    // SSE 流式发送消息（使用 fetch + ReadableStream）
    const sendMessageWithSSE = async (conversationId, message, webSearchEnabled) => {
        setIsStreaming(true);
      const token = localStorage.getItem('token');
      const baseURL = client.defaults.baseURL;

      // 添加占位的 AI 消息
      let aiMessageIndex = null;
      setMessages(prev => {
        aiMessageIndex = prev.length;
        return [...prev, { role: 'assistant', content: '' }];
      });

      try {
        const response = await fetch(`${baseURL}/conversation/chat`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
            'Accept': 'text/event-stream'
          },
          body: JSON.stringify({
            message: message,
            conversationId: conversationId,
            webSearchEnabled: webSearchEnabled
          })
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const newConversationId = response.headers.get('X-Conversation-Id');
        if(newConversationId){
            setCurrentSessionId(newConversationId);
            setSessions(prev => prev.map(s =>
                s.conversationId === conversationId
                  ? { ...s, conversationId: newConversationId, isTemp: false }
                  : s
              ));
          startPollingTitle(newConversationId);
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';
        let currentDataParts = [];

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          const chunk = decoder.decode(value, { stream: true });
            buffer += chunk;

          const lines = buffer.split('\n');
          buffer = lines.pop() || '';

          for (const line of lines) {
            if (line.startsWith('data:')) {
              currentDataParts.push(line.slice(5));
            }else if (line === '' && currentDataParts.length > 0){
                const reconstructedData = currentDataParts.join('\n');
                setMessages(prev => {
                    const newMessages = [...prev];
                    if (newMessages[aiMessageIndex]) {
                      newMessages[aiMessageIndex].content += reconstructedData;
                    }
                    return newMessages;
                });
                currentDataParts = [];
            }
          }
        }
        // 流结束时可能还有未 flush 的数据（最后事件无尾随 \n\n）
        if (currentDataParts.length > 0) {
          const reconstructedData = currentDataParts.join('\n');
          setMessages(prev => {
            const newMessages = [...prev];
            if (newMessages[aiMessageIndex]) {
              newMessages[aiMessageIndex].content += reconstructedData;
            }
            return newMessages;
          });
        }
      } catch (error) {
        console.error('SSE 错误:', error);
        throw error;
      } finally {
         setIsStreaming(false);
       }
    };

    const startPollingTitle = (conversationId) => {
      let retries = 0;
      const maxRetries = 30;  // 最多30次
      const interval = 1000;   // 每秒检查一次

      const timer = setInterval(async () => {
        try {
          // ⭐ 只获取这一个会话的信息
          const response = await client.get(`/conversation/getOne/${conversationId}`);
          const session = response.data;

          // 检查标题是否已更新（不再是"新对话"）
          if (session && session.title && session.title !== '新对话') {
            // 更新会话列表中的标题
            setSessions(prev => prev.map(s =>
              s.conversationId === conversationId
                ? { ...s, title: session.title }
                : s
            ));
            clearInterval(timer);  // 停止轮询
          } else if (++retries >= maxRetries) {
            clearInterval(timer);
          }
        } catch (error) {
          if (++retries >= maxRetries) {
            clearInterval(timer);
          }
        }
      }, interval);
    };

  // 退出登录
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    message.success('已退出登录');
    navigate('/login');
  };

  useEffect(() => {
    loadSessions();
  }, []);

    // 切换会话时加载历史消息
    useEffect(() => {
      if (!currentSessionId) return;

      if (isStreaming) return;

      // 判断是否是临时会话（以 temp_ 开头）
      if (currentSessionId.startsWith('temp_')) {
        setMessages([]);  // 临时会话清空消息，不请求后端
        return;
      }

      // 真实会话才加载历史消息
      loadMessages(currentSessionId);
    }, [currentSessionId]);

  return (
    <Layout style={{ height: '100vh' }}>
      <Sider width={300} theme="light" style={{ borderRight: '1px solid #f0f0f0' }}>
        <SessionList
          sessions={sessions}
          currentSessionId={currentSessionId}
          onSelectSession={setCurrentSessionId}
          onNewSession={handleNewSession}
          onDeleteSession={handleDeleteSession}
          onRenameSession={handleRenameSession}
        />
      </Sider>
      <Layout>
        <Content style={{ display: 'flex', flexDirection: 'column',minWidth: 0  }}>
          <div style={{
            padding: '16px 24px',
            borderBottom: '1px solid #f0f0f0',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}>
            <h3 style={{ margin: 0 }}>智能聊天助手</h3>
            <div>
              <span style={{ marginRight: 16 }}>欢迎，{username || '用户'}！</span>
              <Button type="link" onClick={() => setMemoryDrawerOpen(true)} style={{ marginRight: 8 }}>
                我的记忆
              </Button>
              {localStorage.getItem('role') === 'ADMIN' && (
                  <Button type="link" onClick={() => navigate('/admin')} style={{ marginRight: 8 }}>
                    进入管理后台
                  </Button>
                )}
              <Button onClick={handleLogout}>退出登录</Button>
            </div>
          </div>

          <MessageList messages={messages} loading={loading} />
          <MessageInput
            onSend={handleSendMessage}
            loading={loading}
            webSearchEnabled={webSearchEnabled}
            onWebSearchChange={handleWebSearchChange}
          />
        </Content>
      </Layout>
      <UserMemoryDrawer open={memoryDrawerOpen} onClose={() => setMemoryDrawerOpen(false)} />
    </Layout>
  );
}

export default Chat;