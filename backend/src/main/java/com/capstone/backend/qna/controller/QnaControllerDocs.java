package com.capstone.backend.qna.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.entity.type.Species;
import com.capstone.backend.qna.dto.QnaAnswerRequest;
import com.capstone.backend.qna.dto.QnaAnswerResponse;
import com.capstone.backend.qna.dto.QnaMyAnswerResponse;
import com.capstone.backend.qna.dto.QnaPostRequest;
import com.capstone.backend.qna.dto.QnaPostResponse;
import com.capstone.backend.qna.dto.QnaPostSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Q&A", description = "Q&A 게시판 API")
public interface QnaControllerDocs {

    @Operation(summary = "게시글 목록 조회",
            description = "species(DOG/CAT), answered(true/false) 필터 적용 가능. 미입력 시 전체 조회.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    List<QnaPostSummaryResponse> getPosts(
            @Parameter(hidden = true) UserPrincipal principal,
            @Parameter(description = "반려동물 종류 필터 (DOG / CAT)") Species species,
            @Parameter(description = "답변 여부 필터 (true=완료 / false=대기)") Boolean answered
    );

    @Operation(summary = "게시글 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글 없음")
    })
    QnaPostResponse getPost(
            @Parameter(hidden = true) UserPrincipal principal,
            Long postId
    );

    @Operation(summary = "내 답변 목록 조회", description = "로그인한 사용자가 작성한 답변 목록을 최신순으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    List<QnaMyAnswerResponse> getMyAnswers(
            @Parameter(hidden = true) UserPrincipal principal
    );

    @Operation(summary = "게시글 작성")
    @ApiResponse(responseCode = "201", description = "작성 성공")
    QnaPostResponse createPost(
            @Parameter(hidden = true) UserPrincipal principal,
            QnaPostRequest request
    );

    @Operation(summary = "게시글 수정", description = "본인 게시글만 수정 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    QnaPostResponse updatePost(
            @Parameter(hidden = true) UserPrincipal principal,
            Long postId,
            QnaPostRequest request
    );

    @Operation(summary = "게시글 삭제", description = "본인 게시글만 삭제 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    void deletePost(
            @Parameter(hidden = true) UserPrincipal principal,
            Long postId
    );

    @Operation(summary = "답변 작성", description = "ROLE_VET 계정은 수의사 답변으로 표시됩니다.")
    @ApiResponse(responseCode = "201", description = "작성 성공")
    QnaAnswerResponse createAnswer(
            @Parameter(hidden = true) UserPrincipal principal,
            Long postId,
            QnaAnswerRequest request
    );

    @Operation(summary = "답변 삭제", description = "본인 답변만 삭제 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    void deleteAnswer(
            @Parameter(hidden = true) UserPrincipal principal,
            Long postId,
            Long answerId
    );
}
