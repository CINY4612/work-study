type Callback = (err?: Error | null) => void;

// DB 삭제를 흉내 낸다: 삭제 후 완료 콜백을 바로 부른다 (autoCommit 상황)
function deleteRow(sessions: Map<string, string>, sid: string, done: Callback): void {
  sessions.delete(sid);
  done(null);
}

// 콜백을 무조건 호출하는 스토어: destroy(sid)만 부르면 TypeError
export class NaiveStore {
  readonly sessions = new Map<string, string>();
  destroy(sid: string, callback?: Callback): void {
    deleteRow(this.sessions, sid, (err) => (callback as Callback)(err || null));
  }
}

// 콜백이 있을 때만 호출하는 스토어
export class SafeStore {
  readonly sessions = new Map<string, string>();
  destroy(sid: string, callback?: Callback): void {
    deleteRow(this.sessions, sid, (err) => callback && callback(err || null));
  }
}
