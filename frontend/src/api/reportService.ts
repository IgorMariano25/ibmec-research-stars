import { httpClient } from './httpClient';
import type { CourseCompliance, MyRanking, RankingEntry } from './types';

export const reportService = {
  async getCourseCompliance(): Promise<CourseCompliance[]> {
    const { data } = await httpClient.get<CourseCompliance[]>('/reports/course-compliance');
    return data;
  },
};

export const rankingService = {
  async getAll(): Promise<RankingEntry[]> {
    const { data } = await httpClient.get<RankingEntry[]>('/rankings');
    return data;
  },
  async getMine(): Promise<MyRanking> {
    const { data } = await httpClient.get<MyRanking>('/rankings/me');
    return data;
  },
};
