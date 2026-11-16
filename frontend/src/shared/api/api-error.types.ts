export interface ApiFieldError {
  readonly field: string;
  readonly code: string;
  readonly params?: Readonly<Record<string, unknown>>;
}

export type HttpHeaders = Readonly<Record<string, string>>;
export type ApiErrorKind = 'http' | 'network' | 'timeout' | 'canceled' | 'invalid-response' | 'configuration';

/** Diagnostic data only. Screens translate codes and decide how to display errors. */
export class ApiClientError extends Error {
  readonly kind: ApiErrorKind;
  readonly status?: number;
  readonly code?: string;
  readonly fieldErrors: readonly ApiFieldError[];
  readonly headers: HttpHeaders;
  readonly body?: unknown;

  constructor(kind: ApiErrorKind, message: string, details: {
    status?: number;
    code?: string;
    fieldErrors?: readonly ApiFieldError[];
    headers?: HttpHeaders;
    body?: unknown;
  } = {}) {
    super(message);
    this.name = 'ApiClientError';
    this.kind = kind;
    this.status = details.status;
    this.code = details.code;
    this.fieldErrors = details.fieldErrors ?? [];
    this.headers = details.headers ?? {};
    this.body = details.body;
  }
}

export function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

export function readFieldErrors(value: unknown): readonly ApiFieldError[] {
  if (!Array.isArray(value)) return [];
  return value.filter((item): item is ApiFieldError => isRecord(item)
    && typeof item.field === 'string' && typeof item.code === 'string'
    && (item.params === undefined || isRecord(item.params)));
}
