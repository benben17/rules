package com.spacex.rule.controller;

import com.google.common.collect.Lists;
import com.spacex.rule.bean.YaraBean;
import com.spacex.rule.bean.YaraBigTypeBean;
import com.spacex.rule.repository.YaraBigTypeRepository;
import com.spacex.rule.repository.YaraRepository;
import com.spacex.rule.service.YaraBigTypeService;
import com.spacex.rule.util.DataType;
import com.spacex.rule.util.JsonResult;
import com.spacex.rule.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/yarabigtype")
public class YaraBigTypeController {

    private Logger log = LoggerFactory.getLogger(YaraController.class);

    @Autowired
    private YaraBigTypeRepository yaraBigTypeRepository;

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public JsonResult documentCount() {
        return JsonResult.success(yaraBigTypeRepository.count());
    }

    @RequestMapping(value = "/bigtype/list", method = RequestMethod.GET)
    public JsonResult bigtypeList() {
        Iterable<YaraBigTypeBean> yaraBigTypeList = yaraBigTypeRepository.findAll();

        return JsonResult.success(yaraBigTypeList);
    }


}
