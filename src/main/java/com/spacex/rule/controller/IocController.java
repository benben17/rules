package com.spacex.rule.controller;

import com.google.common.collect.Lists;
import com.spacex.rule.common.ErrorCodeEnum;
import com.spacex.rule.repository.IocRepository;
import com.spacex.rule.bean.IocBean;
import com.spacex.rule.util.DataType;
import com.spacex.rule.util.JsonResult;
import com.spacex.rule.util.JsonUtils;
import com.spacex.rule.util.StringUtil;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/" + DataType.TYPE_IOC)
public class IocController {

    @Autowired
    private IocRepository iocRepository;
    @Autowired
    private ElasticsearchTemplate esTemplate;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public JsonResult create(@RequestBody Object data) {
        String dataStr = JsonUtils.object2Json(data);
        return saveData(null, dataStr);
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
    public JsonResult update(@PathVariable("id") String id, @RequestBody Object data) {
        String dataStr = JsonUtils.object2Json(data);
        return saveData(id, dataStr);
    }

    @RequestMapping(value = "/search/id/{id}", method = RequestMethod.GET)
    public JsonResult searchID(@PathVariable("id") String id) {
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("id", id);
        Iterable<IocBean> listIt = iocRepository.search(queryBuilder);
        List<IocBean> list = Lists.newArrayList(listIt);

        return JsonResult.success(JsonUtils.list2Json(list.size(), list));
    }

    @RequestMapping(value = "/search/ioc/{ioc}/{page}/{rows}", method = RequestMethod.GET)
    public JsonResult searchIoc(@PathVariable("ioc") String ioc,
                                @PathVariable("page") int page,
                                @PathVariable("rows") int rows) {
        if (page < 1 || rows < 1) {
            return JsonResult.fail(ErrorCodeEnum.PARAM_ERROR);
        }
        Pageable pageable = PageRequest.of(page - 1, rows);
        QueryBuilder queryBuilder = QueryBuilders.wildcardQuery("ioc_list", ioc + "*");
        System.out.println(queryBuilder);
        Iterable<IocBean> listIt = iocRepository.search(queryBuilder, pageable);
        List<IocBean> list = Lists.newArrayList(listIt);

        //TODO 获取数据总数
        Iterable<IocBean> listAllIt = iocRepository.search(queryBuilder);
        List<IocBean> listAll = Lists.newArrayList(listAllIt);

        return JsonResult.success(JsonUtils.list2Json(listAll.size(), rows, list));
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public JsonResult delete(@RequestBody List<String> ids) {

        Map<String, String> idMap = new HashMap<>();
        String success_ids = "";
        String failed_ids = "";

        if (ids.size() == 0) {
            return JsonResult.fail(ErrorCodeEnum.PARAM_ERROR);
        } else {
            for (String id : ids) {
                if (iocRepository.existsById(id)) {
                    iocRepository.deleteById(id);
                    success_ids += id + ",";
                } else {
                    failed_ids += id + ",";
                }
            }
        }
        idMap.put("success_id", success_ids);
        idMap.put("failed_id", failed_ids);
        return JsonResult.success(idMap);
    }

    @RequestMapping(value = "/search/all/{page}/{rows}", method = RequestMethod.GET)
    public JsonResult searchAllByPage(@PathVariable("page") int page, @PathVariable("rows") int rows) {
        if (page < 1 || rows < 1) {
            return JsonResult.fail(ErrorCodeEnum.PARAM_ERROR);
        }
        PageRequest pageRequest = PageRequest.of(page-1,rows,new Sort(Sort.Direction.DESC, "create_time.keyword"));
        Iterable<IocBean> userES = iocRepository.findAll(pageRequest);
        List<IocBean> list = new ArrayList<>();
        userES.forEach(list::add);

        //TODO 获取数据总数
        Iterable<IocBean> listAllIt = iocRepository.findAll();
        List<IocBean> listAll = Lists.newArrayList(listAllIt);

        return JsonResult.success(JsonUtils.list2Json(listAll.size(), rows, list));
    }

    @RequestMapping(value = "/search/time/{start}/{end}/{page}/{rows}", method = RequestMethod.GET)
    public JsonResult searchByTime(@PathVariable("start") Long start,
                                   @PathVariable("end") Long end,
                                   @PathVariable("page") int page,
                                   @PathVariable("rows") int rows) {
        if (page < 1 || rows < 1) {
            return JsonResult.fail(ErrorCodeEnum.PARAM_ERROR);
        }

        String startTime = StringUtil.getDateStr(start);
        String endTime = StringUtil.getDateStr(end);
        System.out.println(startTime + "    " + endTime);
        Pageable pageable = PageRequest.of(page - 1, rows);

        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("create_time.keyword")
                .from(startTime)
                .to(endTime)
                .includeLower(true)
                .includeUpper(true);
        Iterable<IocBean> listIt = iocRepository.search(queryBuilder, pageable);
        List<IocBean> list = new ArrayList<>();
        listIt.forEach(list::add);

        //TODO 获取数据总数
        Iterable<IocBean> listAllIt = iocRepository.findAll();
        List<IocBean> listAll = Lists.newArrayList(listAllIt);

        return JsonResult.success(JsonUtils.list2Json(listAll.size(), rows, list));
    }

    @RequestMapping(method = RequestMethod.GET)
    public JsonResult searchAll() {
        Iterable<IocBean> userES = iocRepository.findAll();
        List<IocBean> allList = new ArrayList<>();
        userES.forEach(allList::add);
        return JsonResult.success(JsonUtils.list2Json(allList.size(), allList));
    }

    private JsonResult saveData(@Nullable String id, String data) {
        //TODO 验证json串数据是否合法
        if (JsonUtils.isValidJson(data)) {
            //TODO 合法Json串
            IocBean source = IocBean.parseJson(data);
            String time = StringUtil.getCurrentDate();
            if (source != null) {
                if (id != null) {
                    source.setId(id);
                    source.setUpdate_time(time);
                    source.setCreate_time(source.getCreate_time());
                } else {
                    source.setCreate_time(time);
                    source.setUpdate_time(time);
                }
                String dataStr = JsonUtils.object2Json(source);
                IndexQuery query = new IndexQuery();
                query.setId(source.getId());
                query.setSource(dataStr);
                query.setIndexName(IocBean.INDEX_NAME);
                query.setType(IocBean.TYPE);
                esTemplate.index(query);

                return JsonResult.success(query.getId());
            } else {
                return JsonResult.fail(ErrorCodeEnum.JSON_ERROR);
            }
        } else {
            //TODO 非法Json串
            return JsonResult.fail(ErrorCodeEnum.JSON_ERROR);
        }
    }
}