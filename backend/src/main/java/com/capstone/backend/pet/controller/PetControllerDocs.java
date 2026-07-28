package com.capstone.backend.pet.controller;

import com.capstone.backend.auth.security.UserPrincipal;
import com.capstone.backend.pet.dto.PetRequest;
import com.capstone.backend.pet.dto.PetResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;

public interface PetControllerDocs {

    @Operation(
            summary = "내 반려동물 목록 조회",
            description = "현재 로그인한 사용자의 반려동물 목록을 최신 등록순으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PetResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    List<PetResponse> getPets(
            @Parameter(hidden = true) UserPrincipal principal
    );

    @Operation(
            summary = "내 반려동물 단건 조회",
            description = "현재 로그인한 사용자가 소유한 특정 반려동물 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = PetResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없거나 접근 권한이 없음")
    })
    PetResponse getPet(
            @Parameter(hidden = true) UserPrincipal principal,
            @Parameter(description = "반려동물 ID", example = "1") Long petId
    );

    @Operation(
            summary = "반려동물 등록",
            description = "현재 로그인한 사용자에게 반려동물을 새로 등록합니다."
    )
    @RequestBody(
            required = true,
            description = "반려동물 등록 정보",
            content = @Content(
                    schema = @Schema(implementation = PetRequest.class),
                    examples = @ExampleObject(
                            name = "pet-create",
                            value = """
                                    {
                                      "name": "Coco",
                                      "breed": "Poodle",
                                      "species": "DOG",
                                      "birthDate": "2021-05-10"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = PetResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    PetResponse createPet(
            @Parameter(hidden = true) UserPrincipal principal,
            PetRequest request
    );

    @Operation(
            summary = "반려동물 수정",
            description = "현재 로그인한 사용자가 소유한 반려동물 정보를 수정합니다."
    )
    @RequestBody(
            required = true,
            description = "수정할 반려동물 정보",
            content = @Content(
                    schema = @Schema(implementation = PetRequest.class),
                    examples = @ExampleObject(
                            name = "pet-update",
                            value = """
                                    {
                                      "name": "Coco",
                                      "breed": "Bichon Frise",
                                      "species": "DOG",
                                      "birthDate": "2021-05-10"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = PetResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없거나 접근 권한이 없음")
    })
    PetResponse updatePet(
            @Parameter(hidden = true) UserPrincipal principal,
            @Parameter(description = "반려동물 ID", example = "1") Long petId,
            PetRequest request
    );

    @Operation(
            summary = "반려동물 삭제",
            description = "현재 로그인한 사용자가 소유한 반려동물을 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없거나 접근 권한이 없음")
    })
    void deletePet(
            @Parameter(hidden = true) UserPrincipal principal,
            @Parameter(description = "반려동물 ID", example = "1") Long petId
    );
}
