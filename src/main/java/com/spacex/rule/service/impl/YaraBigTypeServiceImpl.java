package com.spacex.rule.service.impl;

import com.spacex.rule.bean.YaraBigTypeBean;
import com.spacex.rule.repository.YaraBigTypeRepository;
import com.spacex.rule.service.YaraBigTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YaraBigTypeServiceImpl implements YaraBigTypeService {

    @Autowired
    private YaraBigTypeRepository yaraBigTypeRepository;

    @Override
    public long count() {
        return yaraBigTypeRepository.count();
    }

    @Override
    public YaraBigTypeBean save(YaraBigTypeBean yaraBigTypeBean) {
        return yaraBigTypeRepository.save(yaraBigTypeBean);
    }

    @Override
    public Iterable<YaraBigTypeBean> getAll() {
        return yaraBigTypeRepository.findAll();
    }
}
