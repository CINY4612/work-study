package dev.workhard;

public class Registry {

    // 같은 타입(long)이 연달아 있어 호출 순서를 바꿔도 컴파일된다
    public static class Legacy {
        public String register(long subsId, long centerId) {
            return "subs=" + subsId + ",center=" + centerId;
        }
    }

    // ID마다 타입을 분리해 순서 실수를 컴파일 단계에서 막는다
    public record CenterId(long value) {}

    public record SubsId(long value) {}

    public static class Typed {
        public String register(CenterId centerId, SubsId subsId) {
            return "subs=" + subsId.value() + ",center=" + centerId.value();
        }
    }
}
