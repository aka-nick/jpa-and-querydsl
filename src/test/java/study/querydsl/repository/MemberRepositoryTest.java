package study.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

@Transactional
@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void basicTest() { // 순수 jpa 활용 구현
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);

        Member find = memberRepository.findById(member1.getId()).get();
        assertThat(member1).isEqualTo(find);

        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(1);
        assertThat(all).containsExactly(member1);

        List<Member> find2 = memberRepository.findByUsername("member1");
        assertThat(find2).containsExactly(member1);
    }

}