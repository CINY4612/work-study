// 옛 이름의 함수가 삭제되어 정의가 없는 상황을 흉내 낸다 (타입만 선언, 런타임에는 없음).
declare function selectServiceIdsByLoginId(id: string): string[];

// 모든 에러를 삼키는 버전: 리네임 누락도 null로 숨겨진다.
export function resolveSwallowAll(id: string, log: (m: string) => void): string[] | null {
  try {
    return selectServiceIdsByLoginId(id);
  } catch (e) {
    log(String(e));
    return null;
  }
}

// 코드 실수(ReferenceError/TypeError)는 다시 던지는 버전.
export function resolveStrict(id: string, log: (m: string) => void): string[] | null {
  try {
    return selectServiceIdsByLoginId(id);
  } catch (e) {
    if (e instanceof ReferenceError || e instanceof TypeError) throw e;
    log(String(e));
    return null;
  }
}
