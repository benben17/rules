package com.spacex.rule.repository;

import com.spacex.rule.bean.YaraBean;
import com.spacex.rule.bean.YaraBigTypeBean;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface YaraBigTypeRepository extends ElasticsearchRepository<YaraBigTypeBean,String> {


}
