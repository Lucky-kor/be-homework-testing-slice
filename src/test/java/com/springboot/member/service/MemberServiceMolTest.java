package com.springboot.member.service;

import com.springboot.exception.BusinessLogicException;
import com.springboot.member.entity.Member;
import com.springboot.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceMolTest {
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    public void createMemberTest() {
        // given
        Member member = new Member("mandu@cat.com", "만두", "010-2020-3030");

        given(memberRepository.findByEmail(Mockito.anyString())).willReturn(Optional.of(member)); // Mockito.anyString() <- Matchers

        // when , then
        assertThrows(BusinessLogicException.class, () -> memberService.createMember(member));
        // () -> memberService.createMember(member): memberService.createMember(member) 메서드를 호출하는 람다 표현식
        //메서드를 호출할 때 BusinessLogicException 예외가 발생하는지 확인합니다.

    }

}