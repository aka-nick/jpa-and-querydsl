package study.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

@Transactional
@SpringBootTest
class MemberJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() { // 순수 jpa 활용 구현
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

    @Test
    void basicQueryDslTest() { // querydsl 활용 구현
        Member member1 = new Member("member1", 10);
        memberJpaRepository.save(member1);

        List<Member> all = memberJpaRepository.findAllQueryDsl();
        assertThat(all.size()).isEqualTo(1);
        assertThat(all).containsExactly(member1);

        List<Member> find2 = memberJpaRepository.findByUsernameQueryDsl("member1");
        assertThat(find2).containsExactly(member1);
    }

    @Test
    void searchTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCond cond = new MemberSearchCond();
        cond.setAgeGoe(35);
        cond.setAgeLoe(55);
        cond.setTeamName("teamB");

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(cond);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    void search() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCond cond = new MemberSearchCond();
        cond.setAgeGoe(35);
        cond.setAgeLoe(55);
        cond.setTeamName("teamB");
        System.out.println("cond = " + cond);

        List<MemberTeamDto> result = memberJpaRepository.searchByBuilder(cond);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result).extracting("username").containsExactly("member4");

        cond = new MemberSearchCond();
        cond.setTeamName("teamA");
        cond.setAgeLoe(15);

        result = memberJpaRepository.searchByBuilder(cond);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result).extracting("username").containsExactly("member1");
    }
}