// 서버 응답이 와야 바뀌는 status만 보는 가드 (중복을 못 막는다)
export class NaiveJoiner {
  status: 'READY' | 'JOINED' = 'READY';
  joined: string[] = [];

  join(roomId: string): void {
    if (this.status !== 'READY') return;
    this.joined.push(roomId); // 실제 join 요청 전송 (응답 전까지 status는 READY)
  }

  // 서버 응답이 도착했을 때 status가 바뀐다
  onServerResponse(): void {
    this.status = 'JOINED';
  }
}

// 통과하는 순간 동기적으로 roomId를 기록하는 가드
export class GuardedJoiner {
  private activeRoomId: string | null = null;
  joined: string[] = [];

  join(roomId: string): boolean {
    if (this.activeRoomId === roomId) return false; // 이미 처리 중
    this.activeRoomId = roomId; // 검사와 기록이 같은 동기 구간
    this.joined.push(roomId);
    return true;
  }

  // 룸을 떠나거나 초기화할 때 반드시 해제한다
  reset(): void {
    this.activeRoomId = null;
  }
}
