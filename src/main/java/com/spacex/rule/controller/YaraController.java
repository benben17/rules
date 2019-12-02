package com.spacex.rule.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.Lists;
import com.spacex.rule.bean.RequestJson;
import com.spacex.rule.bean.ResponseJson;
import com.spacex.rule.common.ErrorCodeEnum;
import com.spacex.rule.config.Url;
import com.spacex.rule.repository.YaraRepository;
import com.spacex.rule.bean.YaraBean;
import com.spacex.rule.util.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/" + DataType.TYPE_YARA)
public class YaraController {
    private Logger log = LoggerFactory.getLogger(YaraController.class);

    @Autowired
    private YaraRepository yaraRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

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

    @RequestMapping(value = "/search/yara/{big_type}/{page}/{rows}", method = RequestMethod.GET)
    public JsonResult searchID(@PathVariable("big_type") String big_type,
                               @PathVariable("page") int page,
                               @PathVariable("rows") int rows) {


        if (page < 1 || rows < 1) {
            return JsonResult.fail(ErrorCodeEnum.PARAM_ERROR);
        }
        Pageable pageable = PageRequest.of(page - 1, rows);
//        big_type = big_type.toLowerCase();
        QueryBuilder queryBuilder = QueryBuilders.wildcardQuery("big_type", big_type+"*");

//        System.out.println(queryBuilder);
        Iterable<YaraBean> listIt = yaraRepository.search(queryBuilder, pageable);

        List<YaraBean> list = Lists.newArrayList(listIt);

        //TODO 获取数据总数
        Iterable<YaraBean> listAllIt = yaraRepository.search(queryBuilder);
        List<YaraBean> listAll = Lists.newArrayList(listAllIt);

        return JsonResult.success(JsonUtils.list2Json(listAll.size(), rows, list));

    }

    @RequestMapping(value = "/search/id/{id}", method = RequestMethod.GET)
    public JsonResult searchID(@PathVariable("id") String id) {
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("id", id);
        Iterable<YaraBean> listIt = yaraRepository.search(queryBuilder);
        List<YaraBean> list = Lists.newArrayList(listIt);

        return JsonResult.success(JsonUtils.list2Json(list.size(), list));
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    public JsonResult delete(@RequestBody List<String> ids) {
        HashMap<String, String> idMap = new HashMap<>();
        String success_ids = "";
        String failed_ids = "";

        if (ids.size() == 0) {
            return JsonResult.fail(ErrorCodeEnum.PARAM_ERROR);
        } else {
            for (String id : ids) {
                if (yaraRepository.existsById(id)) {
                    yaraRepository.deleteById(id);
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
        Pageable pageable = PageRequest.of(page - 1, rows);
        Iterable<YaraBean> userES = yaraRepository.findAll(pageable);
        List<YaraBean> list = new ArrayList<>();
        userES.forEach(list::add);

        //TODO 获取数据总数
        Iterable<YaraBean> listAllIt = yaraRepository.findAll();
        List<YaraBean> listAll = Lists.newArrayList(listAllIt);

        return JsonResult.success(JsonUtils.list2Json(listAll.size(), rows, list));
    }

    @RequestMapping(value = "/search/time/{start}/{end}/{page}/{rows}", method = RequestMethod.GET)
    public JsonResult searchByTime(@PathVariable("start") Long start,
                                   @PathVariable("end") Long end,
                                   @PathVariable("page") int page,
                                   @PathVariable("rows") int rows
    ) {
        if (page < 1 || rows < 1) {
            return JsonResult.fail(ErrorCodeEnum.PARAM_ERROR);
        }

        String startTime = StringUtil.getDateStr(start);
        String endTime = StringUtil.getDateStr(end);
        Pageable pageable = PageRequest.of(page - 1, rows);

        QueryBuilder queryBuilder = QueryBuilders.rangeQuery("create_time.keyword")
                .from(startTime)
                .to(endTime)
                .includeLower(true)
                .includeUpper(true);
        Iterable<YaraBean> listIt = yaraRepository.search(queryBuilder, pageable);
        List<YaraBean> list = new ArrayList<>();
        listIt.forEach(list::add);

        //TODO 获取数据总数
        Iterable<YaraBean> listAllIt = yaraRepository.findAll();
        List<YaraBean> listAll = Lists.newArrayList(listAllIt);

        return JsonResult.success(JsonUtils.list2Json(listAll.size(), rows, list));
    }


    @RequestMapping(value = "/file/upload", method = RequestMethod.POST)
    public JsonResult upload(@RequestParam MultipartFile file) {
        String tmpFileName = "/tmp/" + file.getOriginalFilename();

        try {
            File tmpFile = new File(tmpFileName);
            file.transferTo(tmpFile);
            String fileContent = FileUtils.fileToString(tmpFileName);
            if (fileContent.isEmpty()) {
                return JsonResult.fail(ErrorCodeEnum.UPLOAD_FILE_EMPTY);
            }
            tmpFile.delete();
            return saveData(null, fileContent);

        } catch (IOException ex) {
            log.error("upload file failed!", ex);
            return JsonResult.fail(ErrorCodeEnum.SYSTEM_ERROR);
        }
    }


    private JsonResult saveData(@Nullable String id, String data) {
        //TODO 验证json串数据是否合法
        if (JsonUtils.isValidJson(data)) {
            //TODO 合法Json串
            YaraBean yara = YaraBean.parseJson(data);
            if (yara != null) {

                //TODO 调用第三方接口，验证数据，返回200，继续操作，返回400或其他，直接返回结果，不再进行后续操作
                RequestJson requestJson = new RequestJson(yara.getBig_type(),yara.getRules());
                Map<String,String> params = new HashMap<>();
                params.put("big_type",yara.getBig_type());
                params.put("rules",yara.getRules());

                //String responseStr = HttpUtil.httpPostWithJson(Url.YARA_VALIDATE_URL,requestJson.toString());
                String responseStr = HttpUtil.httpGet(Url.YARA_VALIDATE_URL,params);

                if (responseStr == null || !JsonUtils.isValidJson(responseStr)) {
                    //TODO 请求失败
                    return JsonResult.fail(ErrorCodeEnum.YARA_VALIDATE_FAIL);
                } else {
                    //TODO 请求成功，获取返回码
                    ResponseJson responseJson = JsonUtils.json2Object(responseStr, ResponseJson.class);
                    if (responseJson.getCode() != 200) {
                        return JsonResult.fail(responseJson.getCode(),responseJson.getMsg());
                    }
                }

                //TODO 验证md5值是否已存在，若已存在，返回添加失败；若不存在，正常添加
                String md5 = checkMD5(yara.getBig_type(), yara.getRules());
                if (md5 == null) {
//                    System.out.println(data);
                    return JsonResult.fail(ErrorCodeEnum.DATA_ERROR);
                }

                if (id != null) {
                    yara.setId(id);
                    yara.setCreate_time(StringUtil.getCurrentDate());

                }
                // author为空时，默认值为cntic
                if (yara.getAuthor() == null || yara.getAuthor().isEmpty() || yara.getAuthor() == "") {
                    yara.setAuthor("cntic");
                }

                yara.setMd5(md5);
                YaraBean bean = yaraRepository.save(yara);
                return JsonResult.success(bean.getId());
            } else {
                return JsonResult.fail(ErrorCodeEnum.JSON_ERROR);
            }
        } else {
            //TODO 非法Json串
            log.error(data);
            return JsonResult.fail(ErrorCodeEnum.JSON_ERROR);
        }
    }


    private String checkMD5(String big_type, String rules) {
        String md5 = DigestUtils.md5DigestAsHex((big_type + rules).getBytes());
        QueryBuilder queryBuilder = QueryBuilders.termQuery("md5", md5);
        Iterable<YaraBean> listIt = yaraRepository.search(queryBuilder);
        List<YaraBean> allList = new ArrayList<>();
        listIt.forEach(allList::add);
        if (allList.size() > 0) {
            return null;
        } else {
            return md5;
        }
    }

    @RequestMapping(value = "/search/agg", method = RequestMethod.GET)
    public JsonResult aggregationSearch() {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("aggSearch").field("big_type").showTermDocCountError(true));
        NativeSearchQuery nativeSearchQuery = nativeSearchQueryBuilder.build();

        AggregatedPage<YaraBean> result = elasticsearchTemplate.queryForPage(nativeSearchQuery, YaraBean.class);

        Aggregations aggregations = result.getAggregations();
        StringTerms terms = aggregations.get("aggSearch");
        terms.getDocCountError();
        List<StringTerms.Bucket> buckets = terms.getBuckets();
        List<Map<String,String>> resultList = new ArrayList<>();
        for (StringTerms.Bucket bucket:buckets) {
            Map<String,String> map = new HashMap<>();
            map.put("key",bucket.getKeyAsString());
            map.put("doc_count",bucket.getDocCount()+"");
            resultList.add(map);
        }
        return JsonResult.success(JsonUtils.list2Json(resultList.size(),resultList));
    }

}