package study.querydsl;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
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
//        QMember m = new QMember("m");
//        QMember m = QMember.member;

        Member find = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(find.getUsername()).isEqualTo("member1");
        assertThat(find.getAge()).isEqualTo(10);
    }

    @Test
    void search() {
        Member find = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(find.getUsername()).isEqualTo("member1");
        assertThat(find.getAge()).isEqualTo(10);
    }

    @Test
    void searchAndParam() {
        Member find = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"), member.age.eq(10)) // and 대신에 `Predicate... conds` 을 넘겨줘도 된다.
                .fetchOne();

        assertThat(find.getUsername()).isEqualTo("member1");
        assertThat(find.getAge()).isEqualTo(10);
    }

    @Test
    void resultFetchTest() {
        List<Member> fetch = queryFactory.selectFrom(member)
                .fetch();

        Member member1 = queryFactory.selectFrom(member)
                .fetchOne();

        Member member2 = queryFactory.selectFrom(member) // 2,3은 동일하다
                .fetchFirst();
        Member member3 = queryFactory.selectFrom(member)
                .limit(1).fetchOne();

        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();

        results.getTotal();
        List<Member> content = results.getResults();
        results.getLimit();
        results.getOffset();

        queryFactory
                .selectFrom(member)
                .fetchCount(); // fetch().size()를 쓸 것.

    }

    @Test
    void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.goe(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = fetch.get(0);
        Member member6 = fetch.get(1);
        Member memberNull = fetch.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();

    }


    @Test
    void paging1() {
        List<Member> result = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);

    }

    @Test
    void paging2() {
        QueryResults<Member> result = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(result.getTotal()).isEqualTo(4);
        assertThat(result.getLimit()).isEqualTo(2);
        assertThat(result.getOffset()).isEqualTo(1);
        assertThat(result.getResults().size()).isEqualTo(2);
    }

    @Test
    void aggregation() {
        List<Tuple> result = queryFactory.select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    @Test
    void groupBy() {
        List<Tuple> fetch = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        assertThat(fetch.size()).isEqualTo(2);
        assertThat(fetch.get(0).get(team.name)).isEqualTo("teamA");
        assertThat(fetch.get(1).get(team.name)).isEqualTo("teamB");
        assertThat(fetch.get(0).get(member.age.avg())).isEqualTo(15);
        assertThat(fetch.get(1).get(member.age.avg())).isEqualTo(35);

    }

    @Test
    void join() {
        List<Member> results = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(results.size()).isEqualTo(2);
        assertThat(results.get(0).getUsername()).isEqualTo("member1");
        assertThat(results.get(1).getUsername()).isEqualTo("member2");

    }

    @Test
    void thetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getUsername()).isEqualTo("teamA");
        assertThat(result.get(1).getUsername()).isEqualTo("teamB");
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }


}
