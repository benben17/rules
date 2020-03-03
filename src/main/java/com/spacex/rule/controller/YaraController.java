package com.spacex.rule.controller;

import com.google.common.collect.Lists;
import com.spacex.rule.bean.RequestJson;
import com.spacex.rule.bean.ResponseJson;
import com.spacex.rule.bean.YaraBigTypeBean;
import com.spacex.rule.common.ErrorCodeEnum;
import com.spacex.rule.config.Properties;
import com.spacex.rule.repository.YaraBigTypeRepository;
import com.spacex.rule.repository.YaraRepository;
import com.spacex.rule.bean.YaraBean;
import com.spacex.rule.util.*;
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
import org.springframework.data.domain.Sort;
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
import java.util.*;

@RestController
@RequestMapping("/" + DataType.TYPE_YARA)
public class YaraController {
    private Logger log = LoggerFactory.getLogger(YaraController.class);

    @Autowired
    private YaraRepository yaraRepository;
    @Autowired
    private YaraBigTypeRepository yaraBigTypeRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private Properties properties;

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
        QueryBuilder queryBuilder = QueryBuilders.wildcardQuery("big_type", big_type + "*");

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

                //TODO G11 增加删除验证接口调用
                if (deleteValidate(id) && yaraRepository.existsById(id)) {
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
    public JsonResult searchAllByPage(@PathVariable("page") int page,
                                      @PathVariable("rows") int rows) {
        if (page < 1 || rows < 1) {
            return JsonResult.fail(ErrorCodeEnum.PARAM_ERROR);
        }
        PageRequest pageRequest = PageRequest.of(page - 1, rows, new Sort(Sort.Direction.DESC, "create_time"));
        Iterable<YaraBean> userES = yaraRepository.findAll(pageRequest);
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
                RequestJson requestJson = new RequestJson(yara.getBig_type(), yara.getRules());
                Map<String, String> params = new HashMap<>();
                params.put("big_type", yara.getBig_type());
                params.put("rules", yara.getRules());

                //String responseStr = HttpUtil.httpPostWithJson(Url.YARA_VALIDATE_URL,requestJson.toString());
                String yaraValidateUrl = properties.getYaraValidateUrl();
                System.out.println(yaraValidateUrl);
                String responseStr = HttpUtil.httpGet(yaraValidateUrl, params);

                //测试
                if (responseStr == null || !JsonUtils.isValidJson(responseStr)) {
                    //TODO 请求失败
                    return JsonResult.fail(ErrorCodeEnum.YARA_VALIDATE_FAIL);
                } else {
                    //TODO 请求成功，获取返回码
                    ResponseJson responseJson = JsonUtils.json2Object(responseStr, ResponseJson.class);
                    if (responseJson.getCode() != 200) {
                        return JsonResult.fail(responseJson.getCode(), responseJson.getMsg());
                    }
                }

                //检查big—type
                checkBigType(yara.getBig_type());
                //TODO 验证md5值是否已存在，若已存在，返回添加失败；若不存在，正常添加
                String md5 = checkMD5(yara.getBig_type(), yara.getRules());
                if (md5 == null) {
//                    System.out.println(data);
                    return JsonResult.fail(ErrorCodeEnum.DATA_ERROR);
                }

                if (id != null) {
                    yara.setId(id);
                } else {
//                    yara.setCreate_time(StringUtil.getCurrentDate());
                    yara.setCreate_time(new Date());
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

    private boolean checkBigType(String bigType) {
        if (bigType.isEmpty() || bigType == "null") {
            return false;
        }
        QueryBuilder queryBuilder = QueryBuilders.termQuery("big_type", bigType);
        Iterable<YaraBigTypeBean> listIt = yaraBigTypeRepository.search(queryBuilder);
        List<YaraBigTypeBean> allList = new ArrayList<>();
        listIt.forEach(allList::add);
        System.out.println(allList.size());
        if (allList.size() > 0) {
        } else {
            YaraBigTypeBean yaraBigType = new YaraBigTypeBean();
            yaraBigType.setBig_type(bigType);
            yaraBigType.setCreate_time(new Date());
            yaraBigTypeRepository.save(yaraBigType);

        }
        return true;
    }

    private boolean deleteValidate(String id) {
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        String deleteValidateUrl = properties.getYaraDeleteValidateUrl();
        String responseStr = HttpUtil.httpGet(deleteValidateUrl, params);

        if (responseStr != null && JsonUtils.isValidJson(responseStr)) {
            //TODO 请求成功，获取返回码
            ResponseJson responseJson = JsonUtils.json2Object(responseStr, ResponseJson.class);
            if (responseJson.getCode() != 200) {
                return true;
            }
        }
        return false;
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
        List<Map<String, String>> resultList = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            Map<String, String> map = new HashMap<>();
            map.put("key", bucket.getKeyAsString());
            map.put("doc_count", bucket.getDocCount() + "");
            resultList.add(map);
        }
        return JsonResult.success(JsonUtils.list2Json(resultList.size(), resultList));
    }

}