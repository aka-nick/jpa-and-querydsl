package study.querydsl.repository;

import java.util.List;
import study.querydsl.dto.MemberSearchCond;
import study.querydsl.dto.MemberTeamDto;

public interface MemberRepositoryCustom {

    List<MemberTeamDto> search(MemberSearchCond cond);

}
