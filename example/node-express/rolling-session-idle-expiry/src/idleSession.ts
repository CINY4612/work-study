// 만료 시각 계산만 떼어낸 최소 세션 저장소 (시계를 주입해서 테스트한다)
export class SessionStore {
  private expiresAt = new Map<string, number>();

  constructor(
    private ttlMs: number,
    private rolling: boolean,
    private now: () => number,
  ) {}

  login(id: string): void {
    this.expiresAt.set(id, this.now() + this.ttlMs);
  }

  // 요청이 올 때 호출. 만료됐으면 false
  touch(id: string): boolean {
    const exp = this.expiresAt.get(id);
    if (exp === undefined || exp <= this.now()) {
      this.expiresAt.delete(id);
      return false;
    }
    if (this.rolling) this.expiresAt.set(id, this.now() + this.ttlMs); // rolling이면 만료를 뒤로 민다
    return true;
  }
}
