export interface Allocation {
  serverId: string;
  baseUrl: string;
}

export interface Input {
  userId: string;
  entryHint?: string; // 진입 주소에 실려 온 힌트
  selfId: string; // 현재 서버 id
  frontDoorUrl: string; // 폴백용 정문 주소
  allocate: (userId: string) => Promise<Allocation>; // 서버 간 배정 조회
  timeoutMs?: number;
}

// 지정 시간 안에 끝나지 않으면 reject
async function withTimeout<T>(p: Promise<T>, ms: number): Promise<T> {
  let timer: NodeJS.Timeout | undefined;
  const timeout = new Promise<never>((_, reject) => {
    timer = setTimeout(() => reject(new Error('timeout')), ms);
  });
  try {
    return await Promise.race([p, timeout]);
  } finally {
    clearTimeout(timer); // 타이머가 남아 프로세스가 안 끝나는 것을 막는다
  }
}

export async function resolveLoginRedirect(i: Input): Promise<string> {
  // 이미 이 사용자에게 맞춘 진입이면 이동 없음
  if (i.entryHint === i.userId) return '/index.html';
  try {
    const a = await withTimeout(i.allocate(i.userId), i.timeoutMs ?? 3000);
    // 내가 배정된 서버면 이동 없음
    if (a.serverId === i.selfId) return '/index.html';
    // 정문을 거치지 않고 배정 서버로 바로 이동 (리다이렉트 1번)
    return `${a.baseUrl}/main?hint=${encodeURIComponent(i.userId)}`;
  } catch {
    // 배정기 장애/지연: 정문이 대신 배정하도록 넘긴다 (리다이렉트 2번)
    return `${i.frontDoorUrl}/main?hint=${encodeURIComponent(i.userId)}`;
  }
}
