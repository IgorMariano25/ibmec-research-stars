import { httpClient } from './httpClient';
import type { Course, CourseRequest } from './types';

export const courseService = {
  async list(): Promise<Course[]> {
    const { data } = await httpClient.get<Course[]>('/courses');
    return data;
  },
  async create(payload: CourseRequest): Promise<Course> {
    const { data } = await httpClient.post<Course>('/courses', payload);
    return data;
  },
  async update(id: number, payload: Partial<CourseRequest>): Promise<Course> {
    const { data } = await httpClient.patch<Course>(`/courses/${id}`, payload);
    return data;
  },
  async remove(id: number): Promise<void> {
    await httpClient.delete(`/courses/${id}`);
  },
};
