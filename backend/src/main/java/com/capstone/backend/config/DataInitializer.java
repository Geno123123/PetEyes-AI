package com.capstone.backend.config;

import com.capstone.backend.entity.Dataset;
import com.capstone.backend.entity.Disease;
import com.capstone.backend.entity.type.Species;
import com.capstone.backend.repository.DatasetRepository;
import com.capstone.backend.repository.DiseaseRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DatasetRepository datasetRepository;
    private final DiseaseRepository diseaseRepository;

    @Value("${app.s3.public-base-url}")
    private String s3PublicBaseUrl;

    @Override
    public void run(String... args) {
        IMG = s3PublicBaseUrl + "/uploads/diseases";
        initDiseases();
        initDatasets();
    }

    private String IMG;

    private void initDiseases() {
        if (diseaseRepository.count() > 0) return;

        List<Disease> diseases = List.of(
                Disease.builder()
                        .diseaseName("안검염")
                        .species(Species.DOG)
                        .category("눈꺼풀질환")
                        .diseaseImageUrl(IMG + "/안검염/main.png")
                        .mildExampleImageUrl(IMG + "/안검염/mild.jpg")
                        .cautionExampleImageUrl(IMG + "/안검염/caution.jpg")
                        .dangerExampleImageUrl(IMG + "/안검염/danger.jpg")
                        .description("눈꺼풀 가장자리와 속눈썹 부위에 발생하는 만성 염증성 질환입니다. 세균 감염, 피부 기름샘 이상, 알레르기 등이 복합적으로 작용하며, 완전 완치보다 관리 중심의 치료가 필요합니다.")
                        .symptoms("눈가 충혈 및 부어오름, 노란 눈곱, 눈 가려움증, 속눈썹 빠짐, 눈꺼풀에 딱지 형성")
                        .cause("세균(포도상구균) 감염, 마이봄샘 기능 이상, 지루성 피부염, 알레르기 반응")
                        .careGuide("따뜻한 수건으로 하루 2회 온찜질(5~10분)하고, 전용 눈꺼풀 세정제로 딱지를 제거하세요. 눈을 비비지 않도록 주의하세요.")
                        .prevention("눈 주위를 항상 청결하게 유지하고, 면역력 관리를 위해 영양 균형 잡힌 식사를 제공하세요.")
                        .treatment("항생제 안약 또는 안연고 처방, 심한 경우 경구 항생제 투여, 스테로이드 안약 병행")
                        .relatedDiseases("결막염, 각막염, 마이봄샘염")
                        .build(),

                Disease.builder()
                        .diseaseName("안검종양")
                        .species(Species.DOG)
                        .category("눈꺼풀질환")
                        .description("눈꺼풀에 발생하는 양성 또는 악성 종양입니다. 노령견에게 흔하며, 마이봄샘 선종이 가장 흔한 양성 종양입니다. 악성인 경우 전이 가능성이 있습니다.")
                        .symptoms("눈꺼풀에 만져지는 덩어리, 눈꺼풀 궤양 및 출혈, 해당 부위 털 빠짐, 눈꺼풀 변형")
                        .cause("노화, 유전적 소인, 자외선 노출, 면역계 이상")
                        .careGuide("종양 크기가 눈동자의 1/3 이상을 가리거나 각막을 자극한다면 즉시 병원을 방문하세요. 정기적으로 크기 변화를 관찰하세요.")
                        .prevention("정기적인 안과 검진으로 조기 발견이 최선의 예방입니다.")
                        .treatment("외과적 절제 수술(양성), 냉동 요법, 악성의 경우 항암 치료 병행")
                        .relatedDiseases("마이봄샘 선종, 편평세포암")
                        .diseaseImageUrl(IMG + "/안검종양/main.png")
                        .mildExampleImageUrl(IMG + "/안검종양/mild.jpg")
                        .cautionExampleImageUrl(IMG + "/안검종양/caution.jpg")
                        .dangerExampleImageUrl(IMG + "/안검종양/danger.jpg")
                        .build(),

                Disease.builder()
                        .diseaseName("안검내반증")
                        .species(Species.DOG)
                        .category("눈꺼풀질환")
                        .description("눈꺼풀이 안쪽으로 말려 들어가 속눈썹이 각막을 지속적으로 찌르는 상태입니다. 샤페이, 차우차우, 불독 등 피부 주름이 많은 견종에서 흔하게 발생합니다.")
                        .symptoms("심한 눈물 흘림, 눈을 자주 깜빡이거나 발로 비빔, 각막 혼탁, 눈 주위 피부 짓무름")
                        .cause("선천성(유전, 견종 특성), 후천성(눈꺼풀 근육 경련, 만성 눈 질환으로 인한 2차 발생)")
                        .careGuide("속눈썹이 각막을 지속적으로 자극하면 각막궤양으로 악화되므로 즉시 병원 방문이 필요합니다. 자가 치료는 불가합니다.")
                        .prevention("취약 견종은 어릴 때부터 정기 안과 검진을 받는 것이 좋습니다.")
                        .treatment("교정 수술(눈꺼풀 성형술)이 근본 치료, 수술 전 임시 처치로 안약 점안")
                        .relatedDiseases("각막궤양, 색소침착성각막염")
                        .diseaseImageUrl(IMG + "/안검내반증/main.png")
                        .mildExampleImageUrl(IMG + "/안검내반증/mild.jpg")
                        .cautionExampleImageUrl(IMG + "/안검내반증/caution.jpg")
                        .dangerExampleImageUrl(IMG + "/안검내반증/danger.jpg")
                        .build(),

                Disease.builder()
                        .diseaseName("유루증")
                        .species(Species.DOG)
                        .category("눈물계질환")
                        .description("눈물이 과도하게 생성되거나 비루관(눈물 배출 통로)이 막혀 눈물이 뺨으로 계속 흘러내리는 질환입니다. 말티즈, 푸들 등 소형견에서 특히 흔합니다.")
                        .symptoms("눈 아래 털의 갈색 또는 적갈색 착색, 눈물 자국으로 인한 피부 냄새, 눈물 자국 주위 피부염")
                        .cause("비루관 폐쇄, 눈꺼풀 이상, 속눈썹 자극, 알레르기, 눈물 과분비")
                        .careGuide("눈 주위를 매일 부드러운 거즈로 닦아 청결하게 유지하세요. 착색된 털은 미용 가위로 정리하고, 사료 성분의 알레르기를 점검하세요.")
                        .prevention("정수 급여, 알레르기 유발 식품 제한, 눈 주위 정기적 청결 관리")
                        .treatment("비루관 개통술, 원인 질환 치료, 항생제 연고(2차 피부염 예방)")
                        .relatedDiseases("안검내반증, 결막염, 알레르기성 피부염")
                        .diseaseImageUrl(IMG + "/유루증/main.png")
                        .mildExampleImageUrl(IMG + "/유루증/mild.jpg")
                        .cautionExampleImageUrl(IMG + "/유루증/caution.jpg")
                        .dangerExampleImageUrl(IMG + "/유루증/danger.jpg")
                        .build(),

                Disease.builder()
                        .diseaseName("색소침착성각막염")
                        .species(Species.DOG)
                        .category("각막질환")
                        .description("각막에 멜라닌 색소가 침착되어 검게 변하는 상태입니다. 만성적인 각막 자극이 지속될 때 발생하며, 퍼그에서 가장 흔하게 나타납니다. 심할 경우 시력 손실로 이어집니다.")
                        .symptoms("각막의 검은 반점 또는 전체적인 흑화, 시야 가려짐으로 인한 행동 변화, 눈 충혈")
                        .cause("만성 각막 자극(안검내반증, 속눈썹 이상), 안구 건조증, 면역 매개성 각막염, 퍼그 색소침착성각막염(유전)")
                        .careGuide("원인 질환을 먼저 치료해야 색소 진행을 막을 수 있습니다. 이미 침착된 색소는 완전히 제거되지 않을 수 있습니다.")
                        .prevention("원인이 되는 만성 자극(안검내반증, 건성안 등)을 조기에 치료하는 것이 예방의 핵심입니다.")
                        .treatment("면역억제제 안약(사이클로스포린), 원인 질환 수술 교정, 중증 시 레이저 제거 시술")
                        .relatedDiseases("안검내반증, 안구건조증, 각막궤양")
                        .diseaseImageUrl(IMG + "/색소침착성각막염/main.png")
                        .mildExampleImageUrl(IMG + "/색소침착성각막염/mild.jpg")
                        .cautionExampleImageUrl(IMG + "/색소침착성각막염/caution.jpg")
                        .dangerExampleImageUrl(IMG + "/색소침착성각막염/danger.jpg")
                        .build(),

                Disease.builder()
                        .diseaseName("핵경화")
                        .species(Species.DOG)
                        .category("수정체질환")
                        .description("노화로 인해 수정체 중심부(핵)가 딱딱해지고 밀도가 높아지면서 뿌옇게 보이는 현상입니다. 7세 이상의 노령견에서 정상적으로 나타나며, 백내장과 달리 시력에 큰 영향을 주지 않습니다.")
                        .symptoms("수정체 중앙의 푸르스름하거나 회색빛 혼탁, 근거리 시력 약화(원거리는 유지), 야간 시력 저하")
                        .cause("정상적인 노화 과정으로 수정체 섬유가 압축되어 발생")
                        .careGuide("특별한 치료는 불필요하지만, 백내장으로 진행되는지 확인하기 위해 6개월~1년마다 정기 검진을 받으세요.")
                        .prevention("노화로 인한 자연 현상이므로 예방은 어렵습니다. 항산화 성분이 풍부한 사료가 도움이 될 수 있습니다.")
                        .treatment("치료 불필요, 정기 관찰로 백내장 진행 모니터링")
                        .relatedDiseases("백내장")
                        .diseaseImageUrl(IMG + "/핵경화/main.png")
                        .mildExampleImageUrl(IMG + "/핵경화/mild.jpg")
                        .cautionExampleImageUrl(IMG + "/핵경화/caution.jpg")
                        .dangerExampleImageUrl(IMG + "/핵경화/danger.jpg")
                        .build(),

                Disease.builder()
                        .diseaseName("궤양성각막질환")
                        .species(Species.DOG)
                        .category("각막질환")
                        .description("각막 표면이 손상되어 궤양(패임)이 생긴 상태입니다. 통증이 매우 심하며, 방치 시 안구 천공까지 진행될 수 있는 위험한 질환입니다.")
                        .symptoms("눈을 제대로 못 뜸(눈꺼풀 경련), 심한 눈물 흘림, 안구 충혈, 각막 혼탁 및 하얀 반점, 눈 비빔")
                        .cause("외상(긁힘, 이물질), 안검내반증, 속눈썹 이상, 건성안, 세균/바이러스 감염, 화학물질 노출")
                        .careGuide("즉시 넥카라를 착용시켜 눈 비빔을 방지하고, 빠른 시간 내에 병원을 방문하세요. 자가 치료는 위험합니다.")
                        .prevention("이물질 접촉 주의, 안검내반증 등 선행 질환 조기 치료, 눈 주위 청결 유지")
                        .treatment("항생제 안약, 각막 보호 안약, 심한 경우 각막 격자 절개술 또는 결막 피판술")
                        .relatedDiseases("안검내반증, 안구건조증, 세균성 각막염")
                        .diseaseImageUrl(IMG + "/궤양성각막질환/main.png")
                        .mildExampleImageUrl(IMG + "/궤양성각막질환/mild.jpg")
                        .cautionExampleImageUrl(IMG + "/궤양성각막질환/caution.jpg")
                        .dangerExampleImageUrl(IMG + "/궤양성각막질환/danger.jpg")
                        .build(),

                Disease.builder()
                        .diseaseName("비궤양성각막질환")
                        .species(Species.DOG)
                        .category("각막질환")
                        .description("각막에 상처(궤양) 없이 염증, 부종, 혈관 신생 등이 발생하는 질환군을 통칭합니다. 면역 매개성 각막염, 간질성 각막염 등이 포함됩니다.")
                        .symptoms("각막 혼탁(하얗게 뿌옇게 보임), 각막 혈관 신생(붉은 혈관이 각막으로 침투), 눈 충혈")
                        .cause("면역 매개성 반응(판누스), 바이러스 감염, 대사 질환(지질 침착), 만성 자극")
                        .careGuide("꾸준한 안약 점안이 중요합니다. 임의로 투약을 중단하지 마세요. 자외선 차단을 위해 야외 활동 시 주의가 필요합니다.")
                        .prevention("면역력 관리, 자외선 노출 최소화, 정기 안과 검진")
                        .treatment("면역억제제 안약(사이클로스포린, 타크로리무스), 스테로이드 안약, 원인에 따른 맞춤 치료")
                        .relatedDiseases("판누스(혈관신생성각막염), 색소침착성각막염")
                        .diseaseImageUrl(IMG + "/비궤양성각막질환/main.png")
                        .mildExampleImageUrl(IMG + "/비궤양성각막질환/mild.jpg")
                        .cautionExampleImageUrl(IMG + "/비궤양성각막질환/caution.jpg")
                        .dangerExampleImageUrl(IMG + "/비궤양성각막질환/danger.jpg")
                        .build(),

                Disease.builder()
                        .diseaseName("백내장")
                        .species(Species.DOG)
                        .category("수정체질환")
                        .description("수정체 단백질이 변성되어 하얗게 혼탁해지는 대표적인 안과 질환입니다. 진행 정도에 따라 초기(incipient), 비성숙(immature), 성숙(mature) 단계로 구분됩니다. 성숙 백내장은 실명으로 이어집니다.")
                        .symptoms("동공이 하얗게 보임, 어두운 곳에서 잘 못 봄, 방향 감각 상실, 벽이나 가구에 부딪힘, 낯선 환경에서 불안해함")
                        .cause("유전(코커스패니얼, 푸들 등), 당뇨병, 노화, 외상, 영양 결핍, 자외선 노출")
                        .careGuide("초기에는 항산화 안약으로 진행을 늦출 수 있습니다. 성숙 단계로 진행되면 수술이 유일한 치료입니다. 당뇨가 있다면 혈당 조절이 필수입니다.")
                        .prevention("당뇨 관리, 정기 안과 검진(특히 취약 견종), 항산화 영양제 급여")
                        .treatment("초기: 항산화 안약, 진행성: 초음파 수정체 유화술(백내장 수술) 후 인공수정체 삽입")
                        .relatedDiseases("핵경화, 당뇨병성 백내장, 녹내장(합병증)")
                        .diseaseImageUrl(IMG + "/백내장/main.png")
                        .mildExampleImageUrl(IMG + "/백내장/mild.jpg")
                        .cautionExampleImageUrl(IMG + "/백내장/caution.jpg")
                        .dangerExampleImageUrl(IMG + "/백내장/danger.jpg")
                        .build(),

                Disease.builder()
                        .diseaseName("결막염")
                        .species(Species.DOG)
                        .category("결막질환")
                        .description("눈을 감싸는 결막(눈꺼풀 안쪽과 안구 표면을 덮는 얇은 막)에 염증이 생긴 상태입니다. 원인이 매우 다양하며, 반려동물에서 가장 흔하게 발생하는 안과 질환 중 하나입니다.")
                        .symptoms("결막 충혈 및 부종, 눈곱(투명~노란색), 눈 주위 붉어짐, 눈물 흘림, 눈 비빔 및 가려움증")
                        .cause("세균 감염, 바이러스 감염, 알레르기(꽃가루, 먼지, 사료), 이물질, 건성안, 다른 안과 질환의 2차 증상")
                        .careGuide("생리식염수로 눈 주위를 부드럽게 닦아주세요. 원인에 따라 치료가 달라지므로 임의로 안약을 사용하지 마세요.")
                        .prevention("눈 주위 청결 유지, 알레르기 유발 환경 개선, 정기 건강검진")
                        .treatment("세균성: 항생제 안약, 바이러스성: 항바이러스제, 알레르기성: 항히스타민제 또는 스테로이드 안약")
                        .relatedDiseases("안검염, 안구건조증, 각막염")
                        .diseaseImageUrl(IMG + "/결막염/danger.jpg")
                        .mildExampleImageUrl(IMG + "/결막염/mild.jpg")
                        .cautionExampleImageUrl(IMG + "/결막염/caution.jpg")
                        .dangerExampleImageUrl(IMG + "/결막염/danger.jpg")
                        .build()
        );

        diseaseRepository.saveAll(diseases);
        log.info("질병 데이터 초기화 완료 ({}개)", diseases.size());
    }

    private void initDatasets() {
        if (datasetRepository.count() > 0) return;

        datasetRepository.saveAll(List.of(
                Dataset.builder().diseaseName("안검염").resultValue("POSITIVE").s3Url("https://your-s3-bucket/samples/blepharitis.jpg").build(),
                Dataset.builder().diseaseName("백내장").resultValue("IMMATURE").s3Url("https://your-s3-bucket/samples/cataract_immature.jpg").build(),
                Dataset.builder().diseaseName("비궤양성각막질환").resultValue("UPPER").s3Url("https://your-s3-bucket/samples/non_ulcerative.jpg").build()
        ));
        log.info("데이터셋 초기화 완료");
    }
}
