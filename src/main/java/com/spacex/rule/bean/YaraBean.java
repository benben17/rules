package com.spacex.rule.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = YaraBean.INDEX_NAME,type= YaraBean.TYPE)
public class YaraBean implements Serializable {

    public static final String INDEX_NAME = "yara";
    public static final String TYPE="yara";

    @Id
    private String id = UUID.randomUUID().toString();

    @JsonProperty("big_type")
    private String big_type;
    @JsonProperty("md5")
    private String md5;
    @JsonProperty("create_time")
    private String create_time;
    @JsonProperty("rules")
    private String rules;


    public static YaraBean parseJson(String json) {
        YaraBean source = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            source = mapper.readValue(json, YaraBean.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return source;
    }
}
