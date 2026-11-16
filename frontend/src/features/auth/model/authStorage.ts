import { ApiClientError, isRecord } from '../../../shared/api/api-error.types';
import { resolveApiUrl } from '../../../shared/api/apiUrl';

export interface StoredSession {
  readonly refreshToken: string;
  readonly userUrl: string;
  readonly sessionUrl: string;
  readonly rememberMe: boolean;
}

export interface AuthStorage {
  read(): StoredSession | undefined;
  save(session: StoredSession): void;
  /** Attempts both stores even when one is unavailable. */
  clear(): boolean;
}

export function storageError(): ApiClientError {
  return new ApiClientError('configuration', 'Authentication storage unavailable', { code: 'AUTH_STORAGE_UNAVAILABLE' });
}

export function createAuthStorage(baseUrl: string, stores: { local: () => Storage; session: () => Storage }): AuthStorage {
  const key = `paw.auth:${baseUrl.replace(/\/$/, '')}`;
  const clear = () => {
    let cleared = true;
    for (const getStore of [stores.local, stores.session]) {
      try { getStore().removeItem(key); } catch { cleared = false; }
    }
    return cleared;
  };

  return {
    clear,
    read() {
      try {
        const local = stores.local().getItem(key);
        const session = stores.session().getItem(key);
        if (local !== null && session !== null) throw storageError();
        const raw = local ?? session;
        if (raw === null) return undefined;
        const data: unknown = JSON.parse(raw);
        if (!isRecord(data) || data.version !== 1 || typeof data.refreshToken !== 'string'
            || !/^rt_[A-Za-z0-9_-]{43}$/.test(data.refreshToken) || typeof data.userUrl !== 'string'
            || typeof data.sessionUrl !== 'string' || data.rememberMe !== Boolean(local)) throw storageError();
        return { refreshToken: data.refreshToken, rememberMe: Boolean(local),
          userUrl: resolveApiUrl(data.userUrl, baseUrl), sessionUrl: resolveApiUrl(data.sessionUrl, baseUrl) };
      } catch { throw storageError(); }
    },
    save(session) {
      try {
        const target = session.rememberMe ? stores.local() : stores.session();
        const other = session.rememberMe ? stores.session() : stores.local();
        other.removeItem(key);
        // Explicit allowlist: access tokens, passwords and profile data are never serialized.
        target.setItem(key, JSON.stringify({ version: 1, refreshToken: session.refreshToken,
          userUrl: session.userUrl, sessionUrl: session.sessionUrl, rememberMe: session.rememberMe }));
      } catch { throw storageError(); }
    },
  };
}
