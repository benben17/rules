package com.spacex.rule.controller;

import com.google.common.collect.Lists;
import com.spacex.rule.common.ErrorCodeEnum;
import com.spacex.rule.repository.YaraRepository;
import com.spacex.rule.bean.YaraBean;
import com.spacex.rule.util.*;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/" + DataType.TYPE_YARA)
public class YaraController {
    private Logger log = LoggerFactory.getLogger(YaraController.class);

    @Autowired
    private YaraRepository yaraRepository;

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
        QueryBuilder queryBuilder = QueryBuilders.wildcardQuery("big_type", big_type + "*");

//        System.out.println(queryBuilder);
        Iterable<YaraBean> listIt = yaraRepository.search(queryBuilder, pageable);

        List<YaraBean> list = Lists.newArrayList(listIt);

        //TODO 获取数据总数
        Iterable<YaraBean> listAllIt = yaraRepository.search(queryBuilder);
        List<YaraBean> listAll = Lists.newArrayList(listAllIt);

        return JsonResult.success(JsonUtils.list2Json(listAll.size(), rows, list));

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

                //TODO 验证md5值是否已存在，若已存在，返回添加失败；若不存在，正常添加
                String md5 = checkMD5(yara.getBig_type(), yara.getRules());
                if (md5 == null) {
                    System.out.println(data);
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

    private boolean checkParam(int page, int rows) {
        if (page < 1 || rows < 1) {
            return false;
        } else {
            return true;
        }
    }
}