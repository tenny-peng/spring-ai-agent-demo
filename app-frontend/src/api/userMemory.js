import client from './client';

export const userMemoryApi = {
    getMemories: () => client.get('/userMemory/getMemories'),

    add: (body) => client.post('/userMemory/add', body),

    delete: (id) => client.delete(`/userMemory/delete/${id}`)
}