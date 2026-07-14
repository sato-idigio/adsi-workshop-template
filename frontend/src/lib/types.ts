export interface DepartmentResponse {
  id: number;
  name: string;
  level: number;
  parentId: number | null;
}

export interface DepartmentTreeResponse {
  id: number;
  name: string;
  level: number;
  children: DepartmentTreeResponse[];
}

export interface EmployeeResponse {
  id: number;
  employeeNumber: string;
  lastName: string;
  firstName: string;
  email: string;
  department: DepartmentResponse;
  position: string | null;
  role: 'EMPLOYEE' | 'ADMIN';
  hireDate: string;
}

export interface LoginRequest {
  employeeNumber: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  employee: EmployeeResponse;
}

export interface EmployeeCreateRequest {
  employeeNumber: string;
  lastName: string;
  firstName: string;
  email: string;
  password: string;
  departmentId: number;
  position?: string;
  role: 'EMPLOYEE' | 'ADMIN';
  hireDate: string;
}

export interface EmployeeUpdateRequest {
  lastName: string;
  firstName: string;
  email: string;
  departmentId: number;
  position?: string;
  role: 'EMPLOYEE' | 'ADMIN';
  hireDate: string;
  version: number;
}

export interface DepartmentCreateRequest {
  name: string;
  parentId?: number | null;
}

export interface DepartmentUpdateRequest {
  name: string;
  parentId?: number | null;
  version: number;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ErrorResponse {
  message: string;
  code: string;
}

export interface AttendanceRecordResponse {
  id: number;
  employeeId: number;
  date: string;
  clockIn: string | null;
  clockOut: string | null;
  workMinutes: number | null;
  overtimeMinutes: number | null;
  nightMinutes: number | null;
}

export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface CorrectionRequestCreate {
  attendanceRecordId: number;
  requestedClockIn?: string | null;
  requestedClockOut?: string | null;
  reason: string;
}

export interface CorrectionRequestResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  attendanceRecordId: number;
  date: string;
  currentClockIn: string | null;
  currentClockOut: string | null;
  requestedClockIn: string | null;
  requestedClockOut: string | null;
  reason: string;
  status: ApprovalStatus;
  createdAt: string;
}

export interface PagedCorrectionRequests {
  content: CorrectionRequestResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
