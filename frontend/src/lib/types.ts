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

export type LeaveType = 'PAID' | 'HALF_AM' | 'HALF_PM' | 'SPECIAL' | 'COMPENSATORY';

export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface LeaveRequestCreate {
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  reason: string;
}

export interface LeaveRequestResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  days: number;
  reason: string;
  status: ApprovalStatus;
  createdAt: string;
}

export interface LeaveBalanceResponse {
  fiscalYear: number;
  totalDays: number;
  usedDays: number;
  remainingDays: number;
}

export interface PagedLeaveRequests {
  content: LeaveRequestResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface MonthlySummaryResponse {
  employeeId: number;
  employeeName: string;
  departmentName: string;
  yearMonth: string;
  totalWorkMinutes: number;
  overtimeMinutes: number;
  nightMinutes: number;
  holidayWorkMinutes: number;
  paidLeaveDays: number;
  workingDays: number;
}

export type PagedMonthlySummaries = PagedResponse<MonthlySummaryResponse>;
