// 리다이렉트를 따라가지 않고 Location별 횟수를 센다
export async function countByLocation(
  base: string,
  path: string,
  times: number,
): Promise<Record<string, number>> {
  const counts: Record<string, number> = {};
  for (let i = 0; i < times; i++) {
    const res = await fetch(base + path, { redirect: 'manual' }); // 핵심: manual
    const loc = res.headers.get('location') ?? 'none';
    counts[loc] = (counts[loc] ?? 0) + 1;
    await res.text(); // 본문 소비
  }
  return counts;
}
