export type Role = 'ADMIN' | 'PROFESSOR';
export type ProfessorStatus = 'PENDING' | 'APPROVED';
export type PublicationStatus = 'PENDING' | 'VALIDATED' | 'REJECTED';
export type PublicationType =
  | 'JOURNAL_ARTICLE'
  | 'CONFERENCE_PAPER'
  | 'BOOK_CHAPTER'
  | 'BOOK'
  | 'EXPANDED_ABSTRACT'
  | 'SIMPLE_ABSTRACT'
  | 'PROCEEDINGS_WORK'
  | 'OTHER';

export interface Course {
  id: number;
  name: string;
  code: string;
}

export interface Professor {
  id: number;
  name: string;
  email: string;
  lattesUrl: string;
  status: ProfessorStatus;
  courses: Course[];
  createdAt: string;
}

export interface Publication {
  id: number;
  title: string;
  link: string;
  publicationDate: string;
  publicationType: PublicationType;
  abntReference: string;
  status: PublicationStatus;
  professorId: number;
  professorName?: string;
  validatedBy?: string | null;
  validatedAt?: string | null;
  createdAt: string;
}

export interface RankingEntry {
  rank: number;
  professorId: number;
  name: string;
  validatedPublicationsLast3Years: number;
}

export interface MyRanking {
  rank: number;
  validatedPublicationsLast3Years: number;
}

export interface CourseCompliance {
  courseId: number;
  courseName: string;
  courseCode: string;
  compliantProfessors: number;
  totalApprovedProfessors: number;
  compliancePercentage: number;
}

export interface RegisterRequest {
  name: string;
  email: string;
  lattesUrl: string;
  password: string;
  courseIds: number[];
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  role?: Role;
  name?: string;
  email?: string;
  professorId?: number;
}

export interface PublicationRequest {
  title: string;
  link: string;
  publicationDate: string;
  publicationType: PublicationType;
  abntReference: string;
}

export interface CourseRequest {
  name: string;
  code: string;
}

export interface ApiFieldError {
  field: string;
  message: string;
}

export interface ApiError {
  timestamp: string;
  status: number;
  message: string;
  fieldErrors?: ApiFieldError[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
