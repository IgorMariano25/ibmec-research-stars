import { httpClient } from './httpClient';
import type {
  Page,
  Professor,
  ProfessorPublicationsResponse,
  ProfessorStatus,
  Publication,
} from './types';

export interface ListProfessorsParams {
  page?: number;
  size?: number;
  sort?: string;
  status?: ProfessorStatus;
  q?: string;
}

type ProfessorPublicationsApiResponse =
  | Publication[]
  | Page<Publication>
  | ProfessorPublicationsResponse;

function normalizeProfessorPublications(data: ProfessorPublicationsApiResponse): Publication[] {
  if (Array.isArray(data)) {
    return data;
  }
  if ('publications' in data && Array.isArray(data.publications)) {
    return data.publications;
  }
  if ('content' in data && Array.isArray(data.content)) {
    return data.content;
  }
  throw new Error('Unexpected professor publications response format');
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
    const { data } = await httpClient.get<ProfessorPublicationsApiResponse>(
      `/professors/${id}/publications`,
    );
    return normalizeProfessorPublications(data);
  },
  async getMe(): Promise<Professor> {
    const { data } = await httpClient.get<Professor>('/professors/me');
    return data;
  },
  async approve(id: number): Promise<Professor> {
    const { data } = await httpClient.post<Professor>(`/professors/${id}/approve`);
    return data;
  },
  async requestCourseChange(courseIds: number[]): Promise<Professor> {
    const { data } = await httpClient.post<Professor>('/professors/me/course-change-request', {
      courseIds,
    });
    return data;
  },
  async approveCourseChange(id: number): Promise<Professor> {
    const { data } = await httpClient.post<Professor>(
      `/professors/${id}/course-change-request/approve`,
    );
    return data;
  },
  async rejectCourseChange(id: number): Promise<Professor> {
    const { data } = await httpClient.post<Professor>(
      `/professors/${id}/course-change-request/reject`,
    );
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
