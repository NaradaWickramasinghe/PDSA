package com.nibm.intelligenttravelmanagementsystem.intelligentdecision.service.ranking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@ConfigurationProperties(prefix = "module4.ranking.weights")
public class RankingWeights {

    @Builder.Default
    private double tree = 0.30;

    @Builder.Default
    private double knn = 0.25;

    @Builder.Default
    private double preference = 0.25;

    @Builder.Default
    private double budget = 0.10;

    @Builder.Default
    private double duration = 0.10;

    /**
     * Normalizes weights so their sum equals 1.0.
     */
    public RankingWeights getNormalized() {
        double total = tree + knn + preference + budget + duration;
        if (total <= 0.0) {
            return RankingWeights.builder()
                    .tree(0.30)
                    .knn(0.25)
                    .preference(0.25)
                    .budget(0.10)
                    .duration(0.10)
                    .build();
        }
        return RankingWeights.builder()
                .tree(tree / total)
                .knn(knn / total)
                .preference(preference / total)
                .budget(budget / total)
                .duration(duration / total)
                .build();
    }
}
