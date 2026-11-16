export const INDEX_MEDIA_TYPE = 'application/vnd.paw.index.v1+json';

/** API resource links are followed as supplied; they are not SPA navigation paths. */
export interface ApiLink {
  readonly href: string;
}

export interface ApiIndex {
  readonly _links: Readonly<Record<string, ApiLink>>;
}
