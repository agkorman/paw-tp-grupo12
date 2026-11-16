import { expect, it } from 'vitest';
import { AUTH_RELATIONS } from '../model/auth.contract';
import { readAuthLinks } from './authLinks';

const base = 'https://paw.test/webapp/api';
const user = `</webapp/api/users/1>; rel="${AUTH_RELATIONS.user}"`;
const session = `<${base}/users/1/sessions/2>; rel="${AUTH_RELATIONS.session}"`;

it('reads both auth relations among other links with commas and quoted parameters', () => {
  const header = `</webapp/api/cars?q=a,b>; title="foo, bar; rel=ignored"; rel="next", ${user}, ${session}`;

  const links = readAuthLinks(header, base);

  expect(links).toEqual({ userUrl: `${base}/users/1`, sessionUrl: `${base}/users/1/sessions/2` });
});

it.each([undefined, user, `${user}, ${user}, ${session}`,
  `${user.replace('/webapp/api/users/1', 'https://other.test/users/1')}, ${session}`])(
  'rejects missing, ambiguous or external auth links: %s', (header) => {
    expect(() => readAuthLinks(header, base)).toThrow();
  },
);
