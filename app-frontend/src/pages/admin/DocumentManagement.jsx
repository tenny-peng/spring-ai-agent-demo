import { useState, useEffect, useCallback } from 'react';
import { Card, Table, Button, Upload, message, Modal, Space, Drawer } from 'antd';
import { UploadOutlined, DownloadOutlined, DeleteOutlined } from '@ant-design/icons';
import { adminApi } from '../../api/admin';

function DocumentManagement() {
  const [data, setData] = useState({ records: [], total: 0 });
  const [loading, setLoading] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailChunks, setDetailChunks] = useState([]);
  const [detailLoading, setDetailLoading] = useState(false);
  const [params, setParams] = useState({ page: 1, size: 20 });

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const res = await adminApi.getDocuments(params);
      setData(res.data);
    } finally {
      setLoading(false);
    }
  }, [params]);

  useEffect(() => { loadData(); }, [loadData]);

  const handleUpload = async (file) => {
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await adminApi.uploadDocument(formData);
      if (res.code === 0) {
          message.success('上传成功');
          setParams(p => ({ ...p, page: 1 }));
      } else {
          message.error(res.message || '上传失败');
      }
    } catch (error) {
      message.error(error.response?.data?.message || '上传失败');
    } finally {
      setUploading(false);
    }
    return false; // 阻止自动上传
  };

  const handleDelete = (id, filename) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除"${filename}"吗？关联的向量数据将一并移除。`,
      onOk: async () => {
        try {
          await adminApi.deleteDocument(id);
          message.success('删除成功');
          loadData();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

const handleDetail = async (id) => {
  setDetailOpen(true);
  setDetailLoading(true);
  try {
    const res = await adminApi.getDocumentDetail(id);
    setDetailChunks(res.data);
  } finally {
    setDetailLoading(false);
  }
};

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '文件名', dataIndex: 'filename', ellipsis: true },
    { title: '类型', dataIndex: 'fileType', width: 80 },
    { title: '切片数', dataIndex: 'chunkCount', width: 80 },
    { title: '状态', dataIndex: 'status', width: 100,
      render: (v) => v === 'COMPLETED' ? '已完成' : v === 'FAILED' ? '导入失败' : '导入中',
    },
    { title: '上传者', dataIndex: 'uploadedByName', width: 120 },
    { title: '上传时间', dataIndex: 'createdAt', width: 180 },
    { title: '操作', width: 180,
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => handleDetail(record.id)}>详情</Button>
          <Button type="link" danger icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id, record.filename)}>删除</Button>
        </Space>
      ),
    },
  ];

  return (
    <Card title="文档管理">
      <Space style={{ marginBottom: 16 }}>
        <Upload
          accept=".csv"
          showUploadList={false}
          beforeUpload={handleUpload}
          disabled={uploading}
        >
          <Button type="primary" icon={<UploadOutlined />} loading={uploading}>
            上传 CSV
          </Button>
        </Upload>
        <Button icon={<DownloadOutlined />} href="http://localhost:8080/api/admin/document/template">
          下载模板
        </Button>
      </Space>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={data.records}
        loading={loading}
        pagination={{
          current: params.page,
          pageSize: params.size,
          total: data.total,
          onChange: (page, size) => setParams(p => ({ ...p, page, size })),
        }}
      />
      <Drawer
        title="文档详情"
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
        size={600}
        loading={detailLoading}
      >
        {detailChunks.map(chunk => (
          <Card
            key={chunk.chunkIndex}
            size="small"
            title={`#${chunk.chunkIndex + 1}`}
            style={{ marginBottom: 8 }}
          >
            <pre style={{ whiteSpace: 'pre-wrap', margin: 0, fontSize: 13 }}>
              {chunk.content}
            </pre>
          </Card>
        ))}
      </Drawer>
    </Card>
  );
}

export default DocumentManagement;