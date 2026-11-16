import axios, { AxiosHeaders } from 'axios';
import { ApiClientError, isRecord, readFieldErrors, type HttpHeaders } from './api-error.types';
import { getApiBaseUrl, resolveApiUrl } from './apiUrl';

export const DEFAULT_TIMEOUT_MS = 30_000;

export interface HttpResponseMetadata {
  readonly status: number;
  readonly headers: HttpHeaders;
  readonly url: string;
}

export interface HttpResponse<T> extends HttpResponseMetadata {
  readonly data: T;
}

export interface HttpRequest {
  readonly url: string;
  readonly method?: 'GET' | 'HEAD' | 'POST' | 'PUT' | 'PATCH' | 'DELETE' | 'OPTIONS';
  readonly headers?: HttpHeaders;
  readonly params?: Readonly<Record<string, string | number | boolean | undefined>>;
  readonly data?: unknown;
  readonly timeoutMs?: number;
  readonly signal?: AbortSignal;
  readonly responseType?: 'json' | 'text' | 'blob' | 'arraybuffer';
  /** Explicit Basic or refresh credentials use manual; anonymous requests use none. */
  readonly authentication?: 'automatic' | 'manual' | 'none';
}

/** Injected by app composition; shared infrastructure never imports an auth feature. */
export interface HttpAuthentication {
  getAuthorization(): string | undefined;
  /** Opaque identity generation captured before sending; interpreted only by the auth feature. */
  captureContext?(): unknown;
  /** Called before decoding/rejecting the response, including 4xx/5xx responses. */
  onResponse?(response: HttpResponseMetadata, request: { readonly context: unknown; readonly authorization?: string }): void | Promise<void>;
  onFailure?(error: ApiClientError, request: { readonly context: unknown; readonly authorization?: string }): void;
  /** The auth feature owns refresh storage and coordination, including across tabs. */
  recoverExpiredToken?<T>(context: {
    readonly error: ApiClientError;
    readonly authorization: string;
    readonly signal?: AbortSignal;
    readonly context: unknown;
    readonly retry: (authorization: string) => Promise<HttpResponse<T>>;
  }): Promise<HttpResponse<T>>;
}

export interface HttpClientOptions {
  readonly baseUrl?: string | (() => string);
  readonly authentication?: HttpAuthentication;
}

const secretHeaders = new Set(['authorization', 'proxy-authorization', 'set-cookie', 'x-access-token', 'x-refresh-token']);

function diagnosticHeaders(headers: HttpHeaders): HttpHeaders {
  return Object.fromEntries(Object.entries(headers).filter(([name]) => !secretHeaders.has(name)));
}

function transportError(error: unknown): ApiClientError {
  if (error instanceof ApiClientError) return error;
  if (axios.isCancel(error)) return new ApiClientError('canceled', 'Request canceled');
  if (axios.isAxiosError(error)) {
    if (error.code === 'ETIMEDOUT' || error.code === 'ECONNABORTED') {
      return new ApiClientError('timeout', 'Request timed out');
    }
    if (error.code === 'ERR_NETWORK') return new ApiClientError('network', 'Network request failed');
  }
  // Never retain Axios config/request/cause: they can contain passwords or bearer tokens.
  return new ApiClientError('configuration', 'Unable to execute HTTP request');
}

function httpError(response: HttpResponse<unknown>): ApiClientError {
  const body = isRecord(response.data) ? response.data : {};
  return new ApiClientError('http', typeof body.message === 'string' ? body.message : `HTTP ${response.status}`, {
    status: response.status,
    code: typeof body.code === 'string' ? body.code : undefined,
    fieldErrors: readFieldErrors(body.fieldErrors),
    headers: diagnosticHeaders(response.headers),
    body: response.data,
  });
}

export function createHttpClient(options: HttpClientOptions = {}) {
  const transport = axios.create({
    adapter: 'fetch',
    timeout: DEFAULT_TIMEOUT_MS,
    withCredentials: false,
    withXSRFToken: false,
    // A redirect may leave the API path; do not forward credentials to its destination.
    fetchOptions: { redirect: 'error' },
    validateStatus: () => true,
    // Keep decoding under our control so malformed JSON doesn't masquerade as valid data.
    transformResponse: [(data: unknown) => data],
  });

  async function request<T>(input: HttpRequest): Promise<HttpResponse<T>> {
    try {
      const configuredBase = options.baseUrl ?? getApiBaseUrl;
      const url = resolveApiUrl(input.url, typeof configuredBase === 'function' ? configuredBase() : configuredBase);
      const timeout = input.timeoutMs ?? DEFAULT_TIMEOUT_MS;
      if (!Number.isFinite(timeout) || timeout <= 0) {
        throw new ApiClientError('configuration', 'timeoutMs must be a positive finite number');
      }
      const mode = input.authentication ?? 'automatic';
      const context = options.authentication?.captureContext?.();
      const headers = new AxiosHeaders(input.headers);
      if (mode !== 'manual' && headers.has('Authorization')) {
        throw new ApiClientError('configuration', 'Explicit Authorization requires manual authentication');
      }
      const authorization = mode === 'automatic' ? options.authentication?.getAuthorization() : undefined;
      if (authorization) headers.set('Authorization', authorization);
      // Axios defaults POST/PUT/PATCH to a form content type even without a body.
      if (input.data === undefined) headers.setContentType(false);
      if (typeof FormData !== 'undefined' && input.data instanceof FormData) headers.setContentType(false);

      const execute = async (requestHeaders: AxiosHeaders): Promise<HttpResponse<T>> => {
        const raw = await transport.request({
          url, method: input.method ?? 'GET', headers: requestHeaders,
          params: input.params, data: input.data, timeout, signal: input.signal,
          responseType: input.responseType === 'blob' || input.responseType === 'arraybuffer' ? input.responseType : 'text',
        });
        const metadata: HttpResponseMetadata = {
          status: raw.status, url: transport.getUri({ url, params: input.params }),
          headers: Object.fromEntries(Object.entries(raw.headers)
            .filter(([, value]) => value !== undefined && value !== null && value !== false)
            .map(([key, value]) => [key.toLowerCase(), String(value)])),
        };
        await options.authentication?.onResponse?.(metadata, {
          context, authorization: requestHeaders.get('Authorization')?.toString(),
        });
        const success = (raw.status >= 200 && raw.status < 300) || raw.status === 304;
        let data: unknown = raw.data;
        const jsonError = !success && /(?:\/json|\+json)(?:;|$)/i.test(metadata.headers['content-type'] ?? '');
        if (jsonError && data instanceof Blob) data = await data.text();
        if (jsonError && data instanceof ArrayBuffer) data = new TextDecoder().decode(data);
        if (input.method === 'HEAD' || raw.status === 204 || raw.status === 304 || data === '') {
          data = undefined;
        } else if (typeof data === 'string' && ((input.responseType ?? 'json') === 'json' || jsonError)) {
          try {
            data = JSON.parse(data);
          } catch {
            if (success) throw new ApiClientError('invalid-response', 'Response is not valid JSON', {
              status: raw.status, headers: diagnosticHeaders(metadata.headers),
            });
            // Non-JSON proxy/server failures still retain their original HTTP status and body.
          }
        }
        const result = { ...metadata, data: data as T };
        if (!success) {
          const error = httpError(result);
          options.authentication?.onFailure?.(error, { context, authorization: requestHeaders.get('Authorization')?.toString() });
          throw error;
        }
        return result;
      };

      try {
        return await execute(headers);
      } catch (error) {
        const recover = options.authentication?.recoverExpiredToken;
        if (mode !== 'automatic' || !authorization || !recover || !(error instanceof ApiClientError)
            || error.status !== 401 || error.code !== 'TOKEN_EXPIRED') throw error;
        let retried = false;
        return await recover<T>({ error, authorization, signal: input.signal, context, retry: (credential) => {
          if (retried) return Promise.reject(new ApiClientError('configuration', 'Request already retried'));
          retried = true;
          const retryHeaders = new AxiosHeaders(headers);
          retryHeaders.set('Authorization', credential);
          return execute(retryHeaders);
        } });
      }
    } catch (error) {
      throw transportError(error);
    }
  }

  return { request };
}

export type HttpClient = ReturnType<typeof createHttpClient>;
