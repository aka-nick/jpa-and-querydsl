package study.querydsl;

import static org.assertj.core.api.Assertions.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    void init() {
        queryFactory = new JPAQueryFactory(em);

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
    }

    @Test
    void startJPQL() {
        Member find = em.createQuery("select m from Member m where m.username = :username",
                        Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(find.getUsername()).isEqualTo("member1");
        assertThat(find.getAge()).isEqualTo(10);
    }

    @Test
    void startQueryDsl() {
        QMember m = new QMember("m");

        Member find = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                .fetchOne();

        assertThat(find.getUsername()).isEqualTo("member1");
        assertThat(find.getAge()).isEqualTo(10);
    }
}
