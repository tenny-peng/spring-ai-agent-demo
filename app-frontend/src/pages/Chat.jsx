// src/pages/Chat.jsx
import { useState, useEffect } from 'react';
import { Layout, message, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import SessionList from '../components/SessionList';
import MessageList from '../components/MessageList';
import MessageInput from '../components/MessageInput';
import client from '../api/client';

const { Sider, Content } = Layout;

function Chat() {
  const navigate = useNavigate();
  const username = localStorage.getItem('username');

  const [sessions, setSessions] = useState([]);
  const [currentSessionId, setCurrentSessionId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(false);

  // 加载会话列表
  const loadSessions = async () => {
    try {
      const response = await client.get('/sessions');
      setSessions(response.data);  // 假设返回数组
      if (response.data.length > 0 && !currentSessionId) {
        setCurrentSessionId(response.data[0].id);
      }
    } catch (error) {
      message.error('加载会话列表失败');
    }
  };

  // 加载历史消息
  const loadMessages = async (sessionId) => {
    try {
      const response = await client.get(`/sessions/${sessionId}/messages`);
      setMessages(response.data);  // 假设返回 [{ role, content }, ...]
    } catch (error) {
      message.error('加载历史消息失败');
    }
  };

  // 创建新会话
  const handleNewSession = async () => {
    try {
      const response = await client.post('/sessions', { title: '新对话' });
      setSessions([response.data, ...sessions]);
      setCurrentSessionId(response.data.id);
      setMessages([]);
      message.success('新会话创建成功');
    } catch (error) {
      message.error('创建会话失败');
    }
  };

  // 删除会话
  const handleDeleteSession = async (sessionId) => {
    try {
      await client.delete(`/sessions/${sessionId}`);
      // 更新前端状态...
      message.success('删除成功');
    } catch (error) {
      message.error('删除失败');
    }
  };

  // 发送消息
  const handleSendMessage = async (content) => {
    if (!currentSessionId) {
      message.warning('请先创建一个会话');
      return;
    }

    const userMessage = { role: 'user', content };
    setMessages(prev => [...prev, userMessage]);
    setLoading(true);

    try {
      const response = await client.post('/chat', {
        sessionId: currentSessionId,
        message: content
      });

      const aiMessage = { role: 'assistant', content: response.data.reply };
      setMessages(prev => [...prev, aiMessage]);
    } catch (error) {
      message.error('发送失败，请重试');
    } finally {
      setLoading(false);
    }
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

  useEffect(() => {
    if (currentSessionId) {
      loadMessages(currentSessionId);
    }
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
        />
      </Sider>
      <Layout>
        <Content style={{ display: 'flex', flexDirection: 'column' }}>
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
              <Button onClick={handleLogout}>退出登录</Button>
            </div>
          </div>

          <MessageList messages={messages} loading={loading} />
          <MessageInput onSend={handleSendMessage} loading={loading} />
        </Content>
      </Layout>
    </Layout>
  );
}

export default Chat;