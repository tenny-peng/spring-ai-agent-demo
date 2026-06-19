// src/components/MessageList.jsx
import { useEffect, useRef } from 'react';
import { Avatar, Spin } from 'antd';
import { UserOutlined, RobotOutlined } from '@ant-design/icons';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

const markdownComponents = {
  p: ({ children }) => <p style={{ margin: '0 0 8px 0', lineHeight: 1.7 }}>{children}</p>,
  h1: ({ children }) => <h1 style={{ fontSize: '1.4em', margin: '12px 0 8px 0' }}>{children}</h1>,
  h2: ({ children }) => <h2 style={{ fontSize: '1.25em', margin: '10px 0 6px 0' }}>{children}</h2>,
  h3: ({ children }) => <h3 style={{ fontSize: '1.1em', margin: '8px 0 4px 0' }}>{children}</h3>,
  h4: ({ children }) => <h4 style={{ fontSize: '1em', margin: '8px 0 4px 0' }}>{children}</h4>,
  h5: ({ children }) => <h5 style={{ fontSize: '0.95em', margin: '6px 0 4px 0' }}>{children}</h5>,
  h6: ({ children }) => <h6 style={{ fontSize: '0.9em', margin: '6px 0 4px 0' }}>{children}</h6>,
  ul: ({ children }) => <ul style={{ margin: '4px 0', paddingLeft: 24 }}>{children}</ul>,
  ol: ({ children }) => <ol style={{ margin: '4px 0', paddingLeft: 24 }}>{children}</ol>,
  li: ({ children }) => <li style={{ margin: '2px 0', lineHeight: 1.6 }}>{children}</li>,
  code: ({ className, children, ...props }) => {
    const isInline = !className;
    return isInline ? (
      <code
        style={{
          background: '#f0f0f0',
          padding: '2px 6px',
          borderRadius: 4,
          fontSize: '0.88em',
          wordBreak: 'break-all',
        }}
        {...props}
      >
        {children}
      </code>
    ) : (
      <pre
        style={{
          background: '#f5f5f5',
          padding: 12,
          borderRadius: 8,
          overflow: 'auto',
          fontSize: '0.88em',
          lineHeight: 1.5,
          margin: '8px 0',
          wordBreak: 'break-word',
          whiteSpace: 'pre-wrap',
        }}
      >
        <code {...props}>{children}</code>
      </pre>
    );
  },
  a: ({ children, href, ...props }) => (
    <a href={href} target="_blank" rel="noopener noreferrer" style={{ color: '#1677ff' }} {...props}>
      {children}
    </a>
  ),
  blockquote: ({ children }) => (
    <blockquote
      style={{
        margin: '8px 0',
        padding: '4px 12px',
        borderLeft: '4px solid #d9d9d9',
        background: '#fafafa',
        borderRadius: 4,
      }}
    >
      {children}
    </blockquote>
  ),
  table: ({ children }) => (
    <div style={{ overflow: 'auto', margin: '8px 0' }}>
      <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: '0.9em' }}>
        {children}
      </table>
    </div>
  ),
  th: ({ children }) => (
    <th style={{ border: '1px solid #d9d9d9', padding: '6px 10px', background: '#fafafa', textAlign: 'left' }}>
      {children}
    </th>
  ),
  td: ({ children }) => (
    <td style={{ border: '1px solid #d9d9d9', padding: '6px 10px' }}>
      {children}
    </td>
  ),
  hr: () => <hr style={{ margin: '12px 0', border: 'none', borderTop: '1px solid #d9d9d9' }} />,
};

function MessageList({ messages, loading }) {
  const messagesEndRef = useRef(null);

  // 自动滚动到底部
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  return (
    <div style={{ flex: 1, overflow: 'auto', padding: '20px', background: '#f5f5f5' }}>
      {messages.map((msg, index) => (
        <div
          key={index}
          style={{
            display: 'flex',
            justifyContent: msg.role === 'USER' ? 'flex-end' : 'flex-start',
            marginBottom: '16px',
          }}
        >
          <div style={{ display: 'flex', maxWidth: '70%', alignItems: 'flex-start' }}>
            {msg.role !== 'USER' && (
              <Avatar icon={<RobotOutlined />} style={{ marginRight: '8px', background: '#52c41a', flexShrink: 0 }} />
            )}
            <div
              style={{
                padding: '10px 15px',
                borderRadius: '12px',
                background: msg.role === 'USER' ? '#1677ff' : '#ffffff',
                color: msg.role === 'USER' ? '#ffffff' : '#000000',
                wordBreak: 'break-word',
                fontSize: '14px',
                minWidth: 0
              }}
            >
              {msg.role === 'USER' ? (
                  msg.content
                ) : (
                  <ReactMarkdown remarkPlugins={[remarkGfm]} components={markdownComponents}>
                    {msg.content}
                  </ReactMarkdown>
                )}
            </div>
            {msg.role === 'USER' && (
              <Avatar icon={<UserOutlined />} style={{ marginLeft: '8px', background: '#1677ff' }} />
            )}
          </div>
        </div>
      ))}
      {loading && messages.every(m => m.role === 'USER' || !m.content) && (
        <div style={{ textAlign: 'center' }}>
          <Spin description="AI 正在思考..." />
        </div>
      )}
      <div ref={messagesEndRef} />
    </div>
  );
}

export default MessageList;