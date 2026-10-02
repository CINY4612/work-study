package dev.workhard;

public class Bank {

    public void transfer(Account from, Account to, long amount) {
        // 방향과 무관하게 id가 작은 계좌를 먼저 잡는다 -> 대기 순환이 생길 수 없다
        Account first = from.id() < to.id() ? from : to;
        Account second = first == from ? to : from;

        first.lock().lock();
        try {
            second.lock().lock();
            try {
                from.add(-amount);
                to.add(amount);
            } finally {
                second.lock().unlock();
            }
        } finally {
            first.lock().unlock();
        }
    }
}
