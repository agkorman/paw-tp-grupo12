import { createContext, useContext, type ReactNode } from 'react';
import type { HttpClient } from './httpClient';

const HttpClientContext = createContext<HttpClient | undefined>(undefined);

export function HttpClientProvider({ client, children }: { client: HttpClient; children: ReactNode }) {
  return <HttpClientContext.Provider value={client}>{children}</HttpClientContext.Provider>;
}

export function useHttpClient() {
  const client = useContext(HttpClientContext);
  if (!client) throw new Error('HttpClientProvider is required');
  return client;
}
