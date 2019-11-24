package com.spacex.rule.repository;

import com.spacex.rule.bean.YaraBean;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

@Component
public interface YaraRepository extends ElasticsearchRepository<YaraBean,String> {
}
