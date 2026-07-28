package com.capstone.backend.qna.service;

import com.capstone.backend.entity.QnaAnswer;
import com.capstone.backend.entity.QnaPost;
import com.capstone.backend.entity.User;
import com.capstone.backend.entity.type.Role;
import com.capstone.backend.entity.type.Species;
import com.capstone.backend.entity.type.VetVerificationStatus;
import com.capstone.backend.qna.dto.QnaAnswerRequest;
import com.capstone.backend.qna.dto.QnaAnswerResponse;
import com.capstone.backend.qna.dto.QnaMyAnswerResponse;
import com.capstone.backend.qna.dto.QnaPostRequest;
import com.capstone.backend.qna.dto.QnaPostResponse;
import com.capstone.backend.qna.dto.QnaPostSummaryResponse;
import com.capstone.backend.repository.QnaAnswerRepository;
import com.capstone.backend.repository.QnaPostRepository;
import com.capstone.backend.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaPostRepository qnaPostRepository;
    private final QnaAnswerRepository qnaAnswerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<QnaPostSummaryResponse> getPosts(Long currentUserId, Species species, Boolean answered) {
        List<QnaPost> posts = qnaPostRepository.findAllByFilter(species, answered);
        Set<Long> userIds = posts.stream().map(QnaPost::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return posts.stream()
                .map(p -> QnaPostSummaryResponse.from(p, currentUserId, userMap.get(p.getUserId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public QnaPostResponse getPost(Long currentUserId, Long postId) {
        QnaPost post = findPost(postId);
        Map<Long, User> userMap = buildUserMap(post);
        return QnaPostResponse.from(post, currentUserId, userMap.get(post.getUserId()), userMap);
    }

    @Transactional(readOnly = true)
    public List<QnaMyAnswerResponse> getMyAnswers(Long userId) {
        User user = findUser(userId);
        validateApprovedVet(user);
        return qnaAnswerRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(QnaMyAnswerResponse::from)
                .toList();
    }

    @Transactional
    public QnaPostResponse createPost(Long userId, QnaPostRequest request) {
        validatePostRequest(request);
        QnaPost post = QnaPost.builder()
                .userId(userId)
                .title(request.title().trim())
                .content(request.content().trim())
                .species(request.species())
                .build();
        QnaPost saved = qnaPostRepository.save(post);
        Map<Long, User> userMap = buildUserMap(saved);
        return QnaPostResponse.from(saved, userId, userMap.get(userId), userMap);
    }

    @Transactional
    public QnaPostResponse updatePost(Long userId, Long postId, QnaPostRequest request) {
        validatePostRequest(request);
        QnaPost post = findOwnedPost(userId, postId);
        post.update(request.title().trim(), request.content().trim(), request.species());
        QnaPost saved = qnaPostRepository.save(post);
        Map<Long, User> userMap = buildUserMap(saved);
        return QnaPostResponse.from(saved, userId, userMap.get(userId), userMap);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        QnaPost post = findOwnedPost(userId, postId);
        qnaPostRepository.delete(post);
    }

    @Transactional
    public QnaAnswerResponse createAnswer(Long userId, Long postId, QnaAnswerRequest request) {
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content is required");
        }

        QnaPost post = findPost(postId);
        User user = findUser(userId);
        validateApprovedVet(user);

        QnaAnswer answer = QnaAnswer.builder()
                .post(post)
                .userId(userId)
                .authorName(user.getName())
                .content(request.content().trim())
                .vet(true)
                .build();

        QnaAnswer saved = qnaAnswerRepository.save(answer);
        post.markAnswered();
        qnaPostRepository.save(post);

        return QnaAnswerResponse.from(saved, userId, user);
    }

    @Transactional
    public void deleteAnswer(Long userId, Long postId, Long answerId) {
        QnaAnswer answer = qnaAnswerRepository.findById(answerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found"));

        if (!answer.getPost().getId().equals(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Answer not found");
        }
        if (!answer.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }

        qnaAnswerRepository.delete(answer);
    }

    private Map<Long, User> buildUserMap(QnaPost post) {
        Set<Long> ids = Stream.concat(
                Stream.of(post.getUserId()),
                post.getAnswers().stream().map(QnaAnswer::getUserId)
        ).collect(Collectors.toSet());
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    private QnaPost findPost(Long postId) {
        return qnaPostRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private QnaPost findOwnedPost(Long userId, Long postId) {
        QnaPost post = findPost(postId);
        if (!post.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized");
        }
        return post;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void validateApprovedVet(User user) {
        if (user.getRole() != Role.ROLE_VET || user.getVetVerificationStatus() != VetVerificationStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only approved vets can access");
        }
    }

    private void validatePostRequest(QnaPostRequest request) {
        if (request == null
                || request.title() == null || request.title().isBlank()
                || request.content() == null || request.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title and content are required");
        }
    }
}
