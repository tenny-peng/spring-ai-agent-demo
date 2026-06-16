// src/pages/Login.jsx
import { useState } from 'react';
import { Form, Input, Button, Card, message } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import client from '../api/client';

function Login() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  // 处理登录提交
  const onFinish = async (values) => {
    console.log('登录表单数据：', values);
    setLoading(true);

    try {
      const response = await client.post('/auth/login', {
        username: values.username,
        password: values.password
      });

      if (response.code === 0) {
          const loginData = response.data;
          localStorage.setItem('token', loginData.token);
          localStorage.setItem('username', loginData.username);
          localStorage.setItem('role', loginData.role);
          message.success('登录成功！');
          navigate('/');
      } else {
          message.error(response.message || '登录失败');
      }
    } catch (error) {
      message.error(error.response?.data?.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      background: '#f0f2f5'
    }}>
      <Card title="智能聊天助手 - 登录" style={{ width: 400 }}>
        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名（测试：test）"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码（测试：123456）"
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block>
              登录
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center' }}>
            <a href="/register">还没有账号？立即注册</a>
          </div>
        </Form>
      </Card>
    </div>
  );
}

export default Login;