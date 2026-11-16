import { createContext, useContext, useEffect, useSyncExternalStore, type ReactNode } from 'react';
import type { AuthSession } from '../model/authSession';

const AuthContext = createContext<AuthSession | undefined>(undefined);

export function AuthProvider({ session, children }: { session: AuthSession; children: ReactNode }) {
  useEffect(() => { void session.start(); }, [session]);
  return <AuthContext.Provider value={session}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const session = useContext(AuthContext);
  if (!session) throw new Error('AuthProvider is required');
  const state = useSyncExternalStore(session.subscribe, session.getSnapshot);
  return { ...state, login: session.login, logout: session.logout, retryProfile: session.retryProfile,
    changeLocale: session.changeLocale };
}
