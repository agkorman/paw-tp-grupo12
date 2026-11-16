import { ApiClientError } from '../../../shared/api/api-error.types';
import type { HttpAuthentication, HttpClient, HttpResponseMetadata } from '../../../shared/api/httpClient';
import { INDEX_MEDIA_TYPE } from '../../../shared/api/resource.types';
import { readOwner, updateOwnerLocale, type OwnerUser } from '../../users';
import { readAuthLinks } from '../api/authLinks';
import { AUTH_HEADERS, SESSION_MEDIA_TYPE } from './auth.contract';
import { storageError, type AuthStorage, type StoredSession } from './authStorage';

export type AuthNotice = 'storageUnavailable' | 'storageClearFailed' | 'sessionEnded' | 'refreshUncertain'
  | 'logoutUnconfirmed' | 'profileUnavailable';
export type AuthSnapshot = Readonly<{
  status: 'restoring' | 'authenticating' | 'anonymous' | 'authenticated' | 'unavailable';
  user?: OwnerUser;
  notices: readonly AuthNotice[];
}>;

interface Credentials extends StoredSession { readonly accessToken?: string }
interface AuthSessionOptions {
  readonly baseUrl: string;
  readonly storage: AuthStorage;
  readonly createClient: (authentication: HttpAuthentication) => HttpClient;
  readonly applyLocale: (locale: 'es' | 'en') => void;
  readonly saveLocalLocale: (locale: 'es' | 'en') => void;
}

function canceled() { return new ApiClientError('canceled', 'Authentication changed while request was pending'); }
const bearer = (token: string) => `Bearer ${token}`;

/** One instance per app/tab. Cross-tab locking and propagation are deliberately pending. */
export function createAuthSession(options: AuthSessionOptions) {
  let snapshot: AuthSnapshot = { status: 'restoring', notices: [] };
  let credentials: Credentials | undefined;
  let rememberMe = false;
  let generation = 0;
  let started: Promise<void> | undefined;
  let rotation: Promise<void> | undefined;
  const listeners = new Set<() => void>();
  const publish = (next: AuthSnapshot) => { snapshot = next; listeners.forEach((listener) => listener()); };

  function stop(notices: AuthNotice[] = []) {
    generation++;
    credentials = undefined;
    rotation = undefined;
    if (!options.storage.clear()) notices.push('storageClearFailed');
    publish({ status: 'anonymous', notices: [...new Set(notices)] });
  }

  async function revoke(session: StoredSession): Promise<boolean> {
    try {
      await client.request({ url: session.sessionUrl, method: 'DELETE', authentication: 'manual',
        headers: { Authorization: bearer(session.refreshToken), Accept: SESSION_MEDIA_TYPE } });
      return true;
    } catch (error) {
      return error instanceof ApiClientError && error.status === 401
        && ['SESSION_EXPIRED', 'SESSION_REVOKED', 'REFRESH_TOKEN_REUSED'].includes(error.code ?? '');
    }
  }

  function issued(response: HttpResponseMetadata): Credentials | undefined {
    const accessToken = response.headers[AUTH_HEADERS.accessToken.toLowerCase()];
    const refreshToken = response.headers[AUTH_HEADERS.refreshToken.toLowerCase()];
    if (!accessToken && !refreshToken) return undefined;
    if (!accessToken || !refreshToken || !/^rt_[A-Za-z0-9_-]{43}$/.test(refreshToken)) {
      throw new ApiClientError('invalid-response', 'Incomplete authentication credentials');
    }
    return { accessToken, refreshToken, rememberMe, ...readAuthLinks(response.headers.link, options.baseUrl) };
  }

  async function loadProfile(epoch: number): Promise<void> {
    if (epoch !== generation || !credentials?.accessToken) throw canceled();
    const current = credentials;
    try {
      const user = await readOwner(client, current.userUrl, bearer(current.accessToken!));
      if (epoch !== generation) throw canceled();
      options.applyLocale(user.preferredLocale);
      publish({ status: 'authenticated', user, notices: [] });
    } catch (error) {
      if (epoch === generation) publish({ status: 'unavailable', notices: ['profileUnavailable'] });
      throw error;
    }
  }

  const authentication: HttpAuthentication = {
    getAuthorization: () => credentials?.accessToken ? bearer(credentials.accessToken) : undefined,
    captureContext: () => generation,
    async onResponse(response, request) {
      const replacement = issued(response);
      if (request.context !== generation) {
        // A login/rotation may finish after logout. Never resurrect it; revoke if we received its links.
        if (replacement) await revoke(replacement);
        throw canceled();
      }
      if (replacement) {
        if (credentials && credentials.sessionUrl !== replacement.sessionUrl) {
          await revoke(replacement);
          throw new ApiClientError('invalid-response', 'Authentication session changed unexpectedly');
        }
        credentials = replacement;
        try { options.storage.save(replacement); }
        catch {
          stop(['storageUnavailable']);
          await revoke(replacement);
          throw storageError();
        }
      }
    },
    async recoverExpiredToken({ authorization, retry, context, signal }) {
      if (signal?.aborted) throw new ApiClientError('canceled', 'Request canceled');
      if (context !== generation || !credentials?.accessToken) throw canceled();
      const epoch = generation;
      if (rotation) await rotation;
      if (epoch !== generation || !credentials?.accessToken) throw canceled();
      if (authorization !== bearer(credentials.accessToken)) return retry(bearer(credentials.accessToken));

      const previousRefresh = credentials.refreshToken;
      let release!: () => void;
      const waiting = new Promise<void>((resolve) => { release = resolve; });
      rotation = waiting;
      try {
        // Exactly one original request spends the refresh. Waiters replay their own request with the new access.
        return await retry(bearer(previousRefresh));
      } finally {
        if (epoch === generation) {
          if (credentials?.refreshToken === previousRefresh) stop(['refreshUncertain']);
          else if (credentials?.accessToken) {
            // Roles and profile preferences are read from the user resource after every rotation.
            try { await loadProfile(epoch); } catch { /* loadProfile publishes a recoverable state. */ }
          }
        }
        if (rotation === waiting) rotation = undefined;
        release();
      }
    },
    onFailure(error, request) {
      if (request.context !== generation || error.status !== 401 || error.code === 'TOKEN_EXPIRED' || !credentials) return;
      if (request.authorization === bearer(credentials.refreshToken)
          || (credentials.accessToken && request.authorization === bearer(credentials.accessToken))) stop(['sessionEnded']);
    },
  };
  const client = options.createClient(authentication);

  async function exchange(authorization: string, epoch: number, previousRefresh?: string): Promise<void> {
    try {
      await client.request({ url: options.baseUrl, authentication: 'manual', headers: {
        Accept: INDEX_MEDIA_TYPE, Authorization: authorization,
        ...(previousRefresh ? {} : { [AUTH_HEADERS.rememberMe]: String(rememberMe) }),
      } });
    } catch (error) {
      if (epoch !== generation) throw error;
      // Credentials may have rotated successfully even when the index returned an error.
      if (!credentials?.accessToken || credentials.refreshToken === previousRefresh) {
        stop(previousRefresh ? ['refreshUncertain'] : []);
        throw error;
      }
    }
    if (epoch !== generation) throw canceled();
    if (!credentials?.accessToken || credentials.refreshToken === previousRefresh) {
      stop(previousRefresh ? ['refreshUncertain'] : []);
      throw new ApiClientError('invalid-response', 'Authentication response did not issue new credentials');
    }
    await loadProfile(epoch);
  }

  async function restore() {
    const epoch = generation;
    try {
      credentials = options.storage.read();
      if (!credentials) { publish({ status: 'anonymous', notices: [] }); return; }
      rememberMe = credentials.rememberMe;
      await exchange(bearer(credentials.refreshToken), epoch, credentials.refreshToken);
    } catch (error) {
      if (epoch === generation && error instanceof ApiClientError && error.code === 'AUTH_STORAGE_UNAVAILABLE') {
        stop(['storageUnavailable']);
      }
      // State carries startup failures. React startup must not create unhandled rejections.
    }
  }

  return {
    client,
    getSnapshot: () => snapshot,
    subscribe(listener: () => void) { listeners.add(listener); return () => { listeners.delete(listener); }; },
    start() { return started ??= snapshot.status === 'restoring' ? restore() : Promise.resolve(); },
    async login(email: string, password: string, remember: boolean) {
      stop();
      if (snapshot.notices.includes('storageClearFailed')) {
        publish({ ...snapshot, notices: [...snapshot.notices, 'storageUnavailable'] });
        throw storageError();
      }
      const epoch = generation;
      rememberMe = remember;
      publish({ status: 'authenticating', notices: [] });
      // UTF-8 Basic credentials stay in this invocation/request only, never in state or storage.
      const bytes = new TextEncoder().encode(`${email}:${password}`);
      const basic = btoa(Array.from(bytes, (byte) => String.fromCharCode(byte)).join(''));
      await exchange(`Basic ${basic}`, epoch);
    },
    async logout() {
      const current = credentials;
      stop();
      const epoch = generation;
      if (current && !await revoke(current) && epoch === generation) {
        publish({ ...snapshot, notices: [...snapshot.notices, 'logoutUnconfirmed'] });
      }
    },
    async retryProfile() {
      const epoch = generation;
      // The index can renew an access that expired while the profile was unavailable.
      await client.request({ url: options.baseUrl, headers: { Accept: INDEX_MEDIA_TYPE } });
      await loadProfile(epoch);
    },
    async changeLocale(locale: 'es' | 'en') {
      const epoch = generation;
      if (credentials?.accessToken) {
        const user = await updateOwnerLocale(client, credentials.userUrl, locale);
        if (epoch !== generation) throw canceled();
        publish({ status: 'authenticated', user, notices: [] });
      }
      if (epoch !== generation) throw canceled();
      options.saveLocalLocale(locale);
      options.applyLocale(locale);
    },
  };
}

export type AuthSession = ReturnType<typeof createAuthSession>;
