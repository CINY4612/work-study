package dev.workhard.savemigration;

// 저장 데이터: version은 어떤 규칙으로 저장됐는지 알려 준다
public record SaveData(int version, int shelfCount, int boxCount) {
}
