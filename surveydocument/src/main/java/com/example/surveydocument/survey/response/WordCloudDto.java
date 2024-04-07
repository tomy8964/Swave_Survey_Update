package com.example.surveydocument.survey.response;

import com.example.surveydocument.survey.domain.WordCloud;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WordCloudDto {
    private Long id;
    private String title;
    private int count;

    public WordCloudDto(WordCloud wordCloud) {
        this.id = wordCloud.getId();
        this.title = wordCloud.getTitle();
        this.count = wordCloud.getCount();
    }

    public static WordCloudDto fromWordCloud(WordCloud wordCloud) {
        return new WordCloudDto(wordCloud);
    }
}
