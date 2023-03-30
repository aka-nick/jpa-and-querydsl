package study.querydsl.repository;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import javax.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

@Transactional
@SpringBootTest
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() {
        // given
        Member member1 = new Member("member1", 10);
        memberJpaRepository.save(member1);

        Member find = memberJpaRepository.findById(member1.getId()).get();
        assertThat(member1).isEqualTo(find);

        List<Member> all = memberJpaRepository.findAll();
        assertThat(all.size()).isEqualTo(1);
        assertThat(all).containsExactly(member1);

        List<Member> find2 = memberJpaRepository.findByUsername("member1");
        assertThat(find2).containsExactly(member1);
    }
}