package com.spacex.rule.service;

import com.spacex.rule.bean.YaraBigTypeBean;

public interface YaraBigTypeService {
    long count();

    YaraBigTypeBean save(YaraBigTypeBean yaraBigTypeBean);

    Iterable<YaraBigTypeBean> getAll();
}
