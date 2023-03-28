package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
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
        Member member4 = new Member("member4", 40, teamB);
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

    @Test
    void joinOnFiltering() {
        List<Member> membersOfTeamA = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        assertThat(membersOfTeamA.size()).isEqualTo(4); // member 2, null 2
    }

    @Test
    void joinOnTheta() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple.get(team) = " + tuple.get(team));
        }
        assertThat(result.size()).isEqualTo(4); // (member 2 + null 2) ^ 2
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    void fetchJoinNo() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne(); // member만 실객체고 연관된 team객체는 프록시.

        assertThat(emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam())).isFalse();
    }

    @Test
    void fetchJoin() {
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin() // 한번에 연관 엔티티를 조회(fetch join)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam())).isTrue();

    }

    @Test
    void subquery() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .select(member)
                .from(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    @Test
    void subqueryGoe() {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void subqueryIn() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result.size()).isEqualTo(3);
        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    @Test
    void subquerySelect() {
        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    void caseSimple() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("늙은이"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    void caseComplex() {
        List<String> result = queryFactory
                .select(
                        new CaseBuilder()
                                .when(member.age.between(0, 20)).then("0~20살")
                                .when(member.age.between(21, 30)).then("21~30살")
                                .otherwise("기타")
                )
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }

    }
    /*
    DB에서는 값을 바꾸고 계산하는 건 좋지 않다.
    DB에서는 필터링, 그룹핑해서 가져오는 것만 하고, 조작은 어플리케이션에서 해야 한다.
    */





}
