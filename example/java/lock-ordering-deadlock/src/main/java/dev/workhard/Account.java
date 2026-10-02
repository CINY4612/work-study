package dev.workhard;

import java.util.concurrent.locks.ReentrantLock;

public class Account {
    private final long id;
    private final ReentrantLock lock = new ReentrantLock();
    private long balance;

    public Account(long id, long balance) {
        this.id = id;
        this.balance = balance;
    }

    public long id() { return id; }

    public ReentrantLock lock() { return lock; }

    // 호출하는 쪽이 lock을 잡은 상태에서만 부른다
    public long balance() { return balance; }

    public void add(long delta) { balance += delta; }
}
