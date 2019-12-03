package com.spacex.rule.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = YaraBean.INDEX_NAME, type = YaraBean.TYPE)
public class YaraBean implements Serializable {

    public static final String INDEX_NAME = "cntic_yara_rules";
    public static final String TYPE = "yara_rules";

    @Id
    private String id = UUID.randomUUID().toString();

    @JsonProperty("big_type")
    @Field(type = FieldType.Keyword)
    private String big_type;
    @JsonProperty("md5")
    private String md5;
    @JsonProperty("create_time")
    @Field(type = FieldType.Date, format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date create_time;
    @JsonProperty("rules")
    private String rules;
    @JsonProperty("author")
    private String author;


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
