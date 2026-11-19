package dev.workhard.specinherit;

// shape: 종류 공통 값 / sheet: 항목마다 다른 고유 값(상속하지 않는다)
public record Spec(String shape, String sheet) {
}
