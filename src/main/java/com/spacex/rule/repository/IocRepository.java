package com.spacex.rule.repository;

import com.spacex.rule.bean.IocBean;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

@Component
public interface IocRepository extends ElasticsearchRepository<IocBean,String> {
}
