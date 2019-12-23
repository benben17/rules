package com.spacex.rule.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = YaraBigTypeBean.INDEX_NAME, type = YaraBigTypeBean.TYPE)
public class YaraBigTypeBean implements Serializable {

    public static final String INDEX_NAME = "yara_bigtype";
    public static final String TYPE = "yara_bigtype_data";

    @Id
    private String id = UUID.randomUUID().toString();

    @JsonProperty("big_type")
    @Field(type = FieldType.Keyword)
//    @Field(type = FieldType.Text)
    private String big_type;

    @JsonProperty("create_time")
    @Field(type = FieldType.Date, format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern ="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date create_time;


    public static YaraBigTypeBean parseJson(String json) {
        YaraBigTypeBean source = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            source = mapper.readValue(json, YaraBigTypeBean.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return source;
    }
}
