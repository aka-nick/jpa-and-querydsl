package study.querydsl.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    void entityTest() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member3", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        // when
        List<Member> resultMember = em.createQuery("select m from Member m join m.team t ", Member.class).getResultList();
        List<Team> resultTeam = em.createQuery("select t from Team t ", Team.class).getResultList();

        // then
        assertThat(resultMember.size()).isEqualTo(4);
        assertThat(resultTeam.size()).isEqualTo(2);

    }
}