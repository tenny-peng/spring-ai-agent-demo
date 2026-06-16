// src/pages/admin/AdminLayout.jsx
import { Layout, Menu } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { TeamOutlined, FileTextOutlined, ArrowLeftOutlined } from '@ant-design/icons';

const { Header, Sider, Content } = Layout;

function AdminLayout() {
  const navigate = useNavigate();
  const location = useLocation();

  const menuItems = [
    { key: '/admin/users', icon: <TeamOutlined />, label: '用户管理' },
    { key: '/admin/documents', icon: <FileTextOutlined />, label: '文档管理' },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
        <span style={{ color: '#fff', fontSize: 18, fontWeight: 600 }}>管理后台</span>
        <span
          style={{ color: '#fff', cursor: 'pointer', marginLeft: 'auto' }}
          onClick={() => navigate('/')}
        >
          <ArrowLeftOutlined /> 返回首页
        </span>
      </Header>
      <Layout>
        <Sider theme="light" width={200}>
          <Menu
            mode="inline"
            selectedKeys={[location.pathname]}
            items={menuItems}
            onClick={({ key }) => navigate(key)}
            style={{ height: '100%' }}
          />
        </Sider>
        <Content style={{ padding: 24 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}

export default AdminLayout;