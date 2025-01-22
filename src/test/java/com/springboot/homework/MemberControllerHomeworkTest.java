package com.springboot.homework;
import com.jayway.jsonpath.JsonPath;
import com.springboot.member.dto.MemberDto;
import com.google.gson.Gson;
import com.springboot.member.dto.MemberPatchDto;
import com.springboot.member.entity.Member;
import com.springboot.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerHomeworkTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @Test
    void postMemberTest() throws Exception {
        // given
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com", "홍길동", "010-1234-5678");
        String content = gson.toJson(post);

        // when
        ResultActions actions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        // then
        actions
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", is(startsWith("/v11/members/"))));
    }

    @Test
    void patchMemberTest() throws Exception {
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com", "홍길동", "010-1234-5678");
        String content = gson.toJson(post);

        ResultActions actions = mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        // TODO MemberController의 patchMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        //given

        String location = actions.andReturn().getResponse().getHeader("Location");

        //PATCH 요청으로 데이터 수정
        MemberDto.Patch patch = new MemberDto.Patch();
        patch.setName("김러키");
        patch.setPhone("010-4444-5555");

        String patchContent = gson.toJson(patch);

        ResultActions patchActions = mockMvc.perform(
                patch(location)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchContent)
        );

        //then
        patchActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("김러키"))
                .andExpect(jsonPath("$.data.phone").value("010-4444-5555"));

    }

    @Test
    void getMemberTest() throws Exception {
        // given: MemberController의 getMember()를 테스트하기 위해서 postMember()를 이용해 테스트 데이터를 생성 후, DB에 저장
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com","홍길동","010-1111-1111");
        String postContent = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );
        long memberId;
        String location = postActions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when / then
        mockMvc.perform(
                        get("/v11/members/" + memberId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(post.getEmail()))
                .andExpect(jsonPath("$.data.name").value(post.getName()))
                .andExpect(jsonPath("$.data.phone").value(post.getPhone()));
    }

    @Test
    void getMembersTest() throws Exception {
        // TODO MemberController의 getMembers() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        // List 형태로 담겨져 있는 데이터를 가져와야 합니다.
        // 준비
        MemberDto.Post post1 = new MemberDto.Post("asd123@gmail.com", "홍길동", "010-1111-2222");
        MemberDto.Post post2 = new MemberDto.Post("zxc44@gmail.com", "김기범", "010-4444-6666");
        MemberDto.Post post3 = new MemberDto.Post("hong4@gmail.com", "홍이임", "010-6666-7777");

        mockMvc.perform(
                post("/v11/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(post1))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/v11/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(post2))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/v11/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(gson.toJson(post3))
        ).andExpect(status().isCreated());

        // 실행 -> get 요청으로 위에 여러 데이터를 가져와야 합니다.

        ResultActions getActions = mockMvc.perform(
                get("/v11/members")
                        .param("page","1")
                        .param("size","10")
                  .accept(MediaType.APPLICATION_JSON)

        );

        // 검증
        getActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("홍이임"))
                .andExpect(jsonPath("$.data[0].phone").value("010-6666-7777"))
                .andExpect(jsonPath("$.data[0].email").value("hong4@gmail.com"))
                .andExpect(jsonPath("$.data[1].email").value("zxc44@gmail.com"))
                .andExpect(jsonPath("$.data[1].name").value("김기범"))
                .andExpect(jsonPath("$.data[1].phone").value("010-4444-6666"))
                .andExpect(jsonPath("$.data[2].email").value("asd123@gmail.com"))
                .andExpect(jsonPath("$.data[2].name").value("홍길동"))
                .andExpect(jsonPath("$.data[2].phone").value("010-1111-2222"));

    }

    @Test
    void deleteMemberTest() throws Exception {
        // TODO MemberController의 deleteMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.

        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com", "홍길동", "010-1234-5678");
        String content = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        postActions.andExpect(status().isCreated());

        //실행
        long memberId;
        String location = postActions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        ResultActions deleteAction = mockMvc.perform(
                delete("/v11/members/" + memberId)
                        .accept(MediaType.APPLICATION_JSON)

        );

        //검증
        deleteAction.andExpect(status().isNoContent());

//        mockMvc.perform(get("/v11/members"+memberId)
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());

    }


}
