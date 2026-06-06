import { httpClient } from './httpClient';
import type { Page, Professor, ProfessorStatus, Publication } from './types';

export interface ListProfessorsParams {
  page?: number;
  size?: number;
  sort?: string;
  status?: ProfessorStatus;
  q?: string;
}

export const professorService = {
  async list(params: ListProfessorsParams = {}): Promise<Page<Professor>> {
    const { data } = await httpClient.get<Page<Professor>>('/professors', { params });
    return data;
  },
  async getById(id: number): Promise<Professor> {
    const { data } = await httpClient.get<Professor>(`/professors/${id}`);
    return data;
  },
  async getPublications(id: number): Promise<Publication[]> {
    const { data } = await httpClient.get<Publication[] | Page<Publication>>(
      `/professors/${id}/publications`,
    );
    return Array.isArray(data) ? data : data.content;
  },
  async getMe(): Promise<Professor> {
    const { data } = await httpClient.get<Professor>('/professors/me');
    return data;
  },
  async approve(id: number): Promise<Professor> {
    const { data } = await httpClient.post<Professor>(`/professors/${id}/approve`);
    return data;
  },
  async update(id: number, payload: Partial<Professor> & { courseIds?: number[] }): Promise<Professor> {
    const { data } = await httpClient.patch<Professor>(`/professors/${id}`, payload);
    return data;
  },
  async remove(id: number): Promise<void> {
    await httpClient.delete(`/professors/${id}`);
  },
};
