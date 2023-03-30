package study.querydsl.dto;

import lombok.Data;

@Data
public class UserDto {

    private int age;
    private String name;

    public UserDto() {
    }

    public UserDto(int age, String name) {
        this.age = age;
        this.name = name;
    }
}
