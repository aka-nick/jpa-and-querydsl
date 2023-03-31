package study.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

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

        List<MemberTeamDto> result = memberRepository.search(cond);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result).extracting("username").containsExactly("member4");

        cond = new MemberSearchCond();
        cond.setTeamName("teamA");
        cond.setAgeLoe(15);

        result = memberRepository.search(cond);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result).extracting("username").containsExactly("member1");
    }

    @Test
    void searchPageSimple() {
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
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = memberRepository.searchPageSimple(cond, pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result).extracting("username").containsExactly("member1", "member2", "member3");

    }


    @Test
    void queryDslPredicateExecutorTest() {

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

        QMember member = QMember.member;
        Iterable<Member> member11 = memberRepository.findAll(
                member.age.between(10, 40).and(member.username.eq("member1")));

        for (Member o : member11) {
            System.out.println("o = " + o);
        }


    }
}