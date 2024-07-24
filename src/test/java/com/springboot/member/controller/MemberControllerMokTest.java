package com.springboot.member.controller;

import com.google.gson.Gson;
import com.springboot.member.dto.MemberDto;
import com.springboot.member.entity.Member;
import com.springboot.member.mapper.MemberMapper;
import com.springboot.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//  Mockito: 단위 테스트에서 의존성을 모킹하여 테스트할 수 있는 프레임워크입니다. 단위 테스트는 특정 메서드나 클래스의 동작을 검증하는 데 집중
@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerMokTest {
    @Autowired
    private MockMvc mockMvc;
    // MockMvc: 실제 8080서버를 띄우지 않고도 HTTP 요청을 보내고 응답을 검증할 수 있는 도구.
    // Mock 객체는 실제 객체의 동작을 모방하는 객체

    @Autowired
    private Gson gson;
    // Gson: Java 객체를 JSON 문자열로 변환하거나, JSON 문자열을 Java 객체로 변환하는 라이브러리.

    @MockBean
    private MemberService memberService;

    @Autowired
    private MemberMapper mapper;

    @Test
    public void postMemberTest() throws Exception{
        // given: 테스트에 필요한 초기 데이터를 설정
        MemberDto.Post post = new MemberDto.Post("mandu@cat.com", "만두",
                "010-2232-4243");
        Member member = mapper.memberPostToMember(post);
        member.setMemberId(1L);

       /* Mock 객체인 memberService가 createMember 메서드를 호출할 때,
         어떤 Member 객체나 매개변수를 받아도 미리 설정한 member 객체를 반환하도록 설정.*/
        // given(...).willReturn(...) 특정 메서드가 호출될 때 어떤 행동을 취해야 하는지를 정의한다.
        given(memberService.createMember(Mockito.any(Member.class)))
                .willReturn(member); // 예상된 결과를 반환하게 설정

        String content = gson.toJson(post);
        // JSON(JavaScript Object Notation) 데이터 교환 형식
        /* 가독성: JSON은 읽기 쉽고 이해하기 쉬운 형식. 객체 안에 객체나 배열을 포함할 수 있음.
        간결함: JSON은 태그가 없기 때문에 데이터의 크기가 작고 전송 속도가 빠르다.
        언어 독립성: 거의 모든 프로그래밍 언어에서 JSON을 쉽게 생성하고 파싱할 수 있다.
        웹 표준: JSON은 웹에서 데이터를 교환하는 데 널리 사용됩.
        구조화된 데이터: JSON은 복잡한 데이터 구조를 표현하는 데 적합.
        웹 API와의 호환성: 많은 웹 API가 JSON 형식을 사용.
        프론트엔드와의 통합: JavaScript는 JSON을 네이티브로 지원.
        데이터베이스와의 통합: MongoDB와 같은 NoSQL 데이터베이스는 JSON 형식을 기본으로 사용.
        상호 운용성: JSON은 다양한 시스템과 언어 간에 데이터를 교환하는 데 이상적. Python 클라이언트가 JSON 형식의 데이터를 Java 서버로 전송가능*/

        // when: 실제 테스트 동작을 수행하는 부분
        // MockMvc를 사용하여 /v11/members 엔드포인트에 POST 요청을 보낸다.
        ResultActions actions = mockMvc.perform(
                post("/v11/members")
                        .accept(MediaType.APPLICATION_JSON) //  클라이언트가 서버로부터 JSON 응답을 기대함.
                        .contentType(MediaType.APPLICATION_JSON) // 요청 본문이 JSON 형식임을 서버에 알림.
                        .content(content) // 실제 JSON 데이터를 요청 본문에 포함하여 서버로 전송.
        );

        // then: 테스트 결과를 검증하는 부분
        actions.andExpect(status().isCreated()) // 기대 결과값이 isCreated 인지 검증
                .andExpect(header().string("Location", is(startsWith("/v11/members/"))));
    }

}