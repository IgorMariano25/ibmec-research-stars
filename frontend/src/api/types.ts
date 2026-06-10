export type Role = 'ADMIN' | 'PROFESSOR';
export type ProfessorStatus = 'PENDING' | 'APPROVED';
export type PublicationStatus = 'PENDING' | 'VALIDATED' | 'REJECTED';
export type ProfessorCourseChangeStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUPERSEDED';
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
  userId?: number;
  name: string;
  email: string;
  lattesUrl: string;
  matricula?: string | null;
  status: ProfessorStatus;
  courses: Course[];
  pendingCourseChangeRequest?: ProfessorCourseChangeRequest | null;
  createdAt: string;
}

export interface ProfessorCourseChangeRequest {
  id: number;
  professorId: number;
  requestedCourses: Course[];
  status: ProfessorCourseChangeStatus;
  requestedByUserId: number;
  reviewedByUserId?: number | null;
  requestedAt?: string | null;
  reviewedAt?: string | null;
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

export interface ProfessorPublicationsResponse {
  professorId: number;
  publications: Publication[];
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

export interface ProfessorCompliance {
  professorId: number;
  professorName: string;
  validatedPublications: number;
  compliant: boolean;
}

export interface CourseCompliance {
  courseId: number;
  courseName: string;
  courseCode: string;
  compliantProfessors?: number;
  totalCompliantProfessors?: number;
  totalApprovedProfessors: number;
  compliancePercentage: number;
  professorCompliance?: ProfessorCompliance[];
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
