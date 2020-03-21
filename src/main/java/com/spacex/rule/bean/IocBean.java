package com.spacex.rule.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = IocBean.INDEX_NAME,type= IocBean.TYPE)
public class IocBean implements Serializable {

    public static final String INDEX_NAME = "cntic_ioc_stock";
    public static final String TYPE="ioc_data";

    @Id
    private String id = UUID.randomUUID().toString();

    @JsonProperty("campaign")
    private ArrayList<String> campaign;
    @JsonProperty("kill_chain_phases")
    private ArrayList<String> kill_chain_phases;
    @JsonProperty("apt")
    private String apt;
    @JsonProperty("tag")
    private ArrayList<String> tag;
    @JsonProperty("malware")
    private ArrayList<String> malware;
    @JsonProperty("severity")
    private String severity;
    @JsonProperty("attack-pattern")
    private ArrayList<String> attack_pattern;
    @JsonProperty("intrusion-set")
    private ArrayList<String> intrusion_set;
    @JsonProperty("platform")
    private ArrayList<String> platform;
    @JsonProperty("score")
    private String score;
    @JsonProperty("type")
    private String type;
    @JsonProperty("threat-actor")
    private ArrayList<String> threat_actor;
    @JsonProperty("update_time")
    private String update_time;
    @JsonProperty("description")
    private String description;
    @JsonProperty("extend")
    private String extend;
    @JsonProperty("targeted")
    private String targeted;
    @JsonProperty("tool")
    private ArrayList<String> tool;
    @JsonProperty("malicious_family")
    private ArrayList<String> malicious_family;
    @JsonProperty("ioc_list")
    private ArrayList<String> ioc_list;
    @JsonProperty("analysis_result")
    private String analysis_result;
    @JsonProperty("indicator_name")
    private String indicator_name;
    @JsonProperty("author")
    private String author;
    @JsonProperty("current_status")
    private String current_status;
    @JsonProperty("create_time")
    private String create_time;
    @JsonProperty("action_suggestion")
    private String action_suggestion;
    @JsonProperty("malicious_type")
    private String malicious_type;

    public static IocBean parseJson(String json) {
        IocBean source = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            source = mapper.readValue(json, IocBean.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return source;
    }
}
