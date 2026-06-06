import { httpClient } from './httpClient';
import type {
  Page,
  Publication,
  PublicationRequest,
  PublicationStatus,
} from './types';

export interface ListPublicationsParams {
  page?: number;
  size?: number;
  sort?: string;
  status?: PublicationStatus;
  professorId?: number;
  q?: string;
}

export const publicationService = {
  async list(params: ListPublicationsParams = {}): Promise<Page<Publication>> {
    const { data } = await httpClient.get<Page<Publication>>('/publications', { params });
    return data;
  },
  async listMine(): Promise<Publication[]> {
    const { data } = await httpClient.get<Publication[] | Page<Publication>>('/publications/me');
    return Array.isArray(data) ? data : data.content;
  },
  async getById(id: number): Promise<Publication> {
    const { data } = await httpClient.get<Publication>(`/publications/${id}`);
    return data;
  },
  async create(payload: PublicationRequest): Promise<Publication> {
    const { data } = await httpClient.post<Publication>('/publications', payload);
    return data;
  },
  async update(id: number, payload: Partial<PublicationRequest>): Promise<Publication> {
    const { data } = await httpClient.patch<Publication>(`/publications/${id}`, payload);
    return data;
  },
  async validate(id: number): Promise<Publication> {
    const { data } = await httpClient.post<Publication>(`/publications/${id}/validate`);
    return data;
  },
  async reject(id: number): Promise<Publication> {
    const { data } = await httpClient.post<Publication>(`/publications/${id}/reject`);
    return data;
  },
  async remove(id: number): Promise<void> {
    await httpClient.delete(`/publications/${id}`);
  },
};
