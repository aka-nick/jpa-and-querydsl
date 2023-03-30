package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberDto {

    private String username;
    private int age;

    public MemberDto() {
    }

    @QueryProjection // DTO도 Q파일로 만들어주게 된다
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
