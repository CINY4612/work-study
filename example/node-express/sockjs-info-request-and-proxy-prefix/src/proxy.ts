import type { RequestListener } from 'node:http';

// nginx의 `location <prefix>/ { proxy_pass http://up[/]; }` 동작을 흉내 낸다.
// withUri=true  -> proxy_pass 끝에 / 가 있는 경우: prefix를 떼고 넘긴다
// withUri=false -> 끝에 / 가 없는 경우: 원래 URI를 그대로 넘긴다
export function proxyPass(prefix: string, upstream: RequestListener, withUri: boolean): RequestListener {
  return (req, res) => {
    if (!req.url?.startsWith(prefix + '/')) {
      res.statusCode = 404;
      res.end();
      return;
    }
    if (withUri) req.url = req.url.slice(prefix.length);
    upstream(req, res);
  };
}
