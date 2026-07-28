package com.capstone.backend.qna.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.entity.type.Species;
import com.capstone.backend.qna.dto.QnaAnswerRequest;
import com.capstone.backend.qna.dto.QnaAnswerResponse;
import com.capstone.backend.qna.dto.QnaMyAnswerResponse;
import com.capstone.backend.qna.dto.QnaPostRequest;
import com.capstone.backend.qna.dto.QnaPostResponse;
import com.capstone.backend.qna.dto.QnaPostSummaryResponse;
import com.capstone.backend.qna.service.QnaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
@Tag(name = "Q&A", description = "Q&A 게시판 API")
@SecurityRequirement(name = "bearerAuth")
public class QnaController implements QnaControllerDocs {

    private final QnaService qnaService;

    @GetMapping
    @Override
    public List<QnaPostSummaryResponse> getPosts(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Species species,
            @RequestParam(required = false) Boolean answered
    ) {
        return qnaService.getPosts(requireUserId(principal), species, answered);
    }

    @GetMapping("/{postId}")
    @Override
    public QnaPostResponse getPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId
    ) {
        return qnaService.getPost(requireUserId(principal), postId);
    }

    @GetMapping("/my-answers")
    @Override
    public List<QnaMyAnswerResponse> getMyAnswers(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return qnaService.getMyAnswers(requireUserId(principal));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public QnaPostResponse createPost(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody QnaPostRequest request
    ) {
        return qnaService.createPost(requireUserId(principal), request);
    }

    @PutMapping("/{postId}")
    @Override
    public QnaPostResponse updatePost(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @RequestBody QnaPostRequest request
    ) {
        return qnaService.updatePost(requireUserId(principal), postId, request);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void deletePost(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId
    ) {
        qnaService.deletePost(requireUserId(principal), postId);
    }

    @PostMapping("/{postId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    @Override
    public QnaAnswerResponse createAnswer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @RequestBody QnaAnswerRequest request
    ) {
        return qnaService.createAnswer(requireUserId(principal), postId, request);
    }

    @DeleteMapping("/{postId}/answers/{answerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Override
    public void deleteAnswer(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId,
            @PathVariable Long answerId
    ) {
        qnaService.deleteAnswer(requireUserId(principal), postId, answerId);
    }

    private Long requireUserId(UserPrincipal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        return principal.id();
    }
}
