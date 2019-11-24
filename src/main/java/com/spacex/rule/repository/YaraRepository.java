package com.spacex.rule.repository;

import com.spacex.rule.bean.YaraBean;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public interface YaraRepository extends ElasticsearchRepository<YaraBean,String> {


}
