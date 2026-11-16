import { ApiClientError } from '../../../shared/api/api-error.types';
import { resolveApiUrl } from '../../../shared/api/apiUrl';
import { AUTH_RELATIONS } from '../model/auth.contract';

/** Parses the auth relations, respecting commas inside URI references and quoted parameters. */
export function readAuthLinks(header: string | undefined, baseUrl: string): { userUrl: string; sessionUrl: string } {
  const entries: string[] = [];
  let start = 0, quoted = false, uri = false, escaped = false;
  const value = header ?? '';
  for (let index = 0; index < value.length; index++) {
    const char = value[index];
    if (escaped) { escaped = false; continue; }
    if (quoted && char === '\\') { escaped = true; continue; }
    if (!uri && char === '"') quoted = !quoted;
    if (!quoted && char === '<') uri = true;
    if (!quoted && char === '>') uri = false;
    if (!quoted && !uri && char === ',') { entries.push(value.slice(start, index)); start = index + 1; }
  }
  entries.push(value.slice(start));
  const links = new Map<string, string>();
  for (const entry of entries) {
    const match = /^\s*<([^<>]+)>\s*(.*)$/.exec(entry);
    if (!match) continue;
    // Parameters may have quoted values containing semicolons (e.g. title="foo; rel=bar").
    const params = /;\s*([^\s=;]+)\s*=\s*(?:"((?:[^"\\]|\\.)*)"|([^\s;]+))/g;
    for (const parameter of match[2].matchAll(params)) {
      if (parameter[1].toLowerCase() !== 'rel') continue;
      for (const relation of (parameter[2] ?? parameter[3]).split(/\s+/)) {
        if (relation !== AUTH_RELATIONS.user && relation !== AUTH_RELATIONS.session) continue;
        if (links.has(relation)) throw invalidLinks();
        links.set(relation, resolveApiUrl(match[1], baseUrl));
      }
    }
  }
  const userUrl = links.get(AUTH_RELATIONS.user);
  const sessionUrl = links.get(AUTH_RELATIONS.session);
  if (!userUrl || !sessionUrl || quoted || uri) throw invalidLinks();
  return { userUrl, sessionUrl };
}

function invalidLinks() {
  return new ApiClientError('invalid-response', 'Missing or ambiguous authentication links');
}
