package org.hango.cloud.common.infra.gateway.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.hango.cloud.common.infra.base.controller.AbstractController;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.meta.PageResult;
import org.hango.cloud.common.infra.base.meta.Result;
import org.hango.cloud.common.infra.gateway.dto.GatewayDto;
import org.hango.cloud.common.infra.gateway.service.IGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc 网关信息表
 * @date 2022/10/25
 */
@RestController
@RequestMapping(value = BaseConst.HANGO_DASHBOARD_PREFIX, params = {"Version=2022-10-30"})
public class GatewayController extends AbstractController {

    public static final Logger logger = LoggerFactory.getLogger(GatewayController.class);

    @Autowired
    private IGatewayService gatewayService;

    /**
     * 创建网关信息
     *
     * @param gatewayDto
     * @return
     */
    @PostMapping(params = {"Action=CreateGateway"})
    public Object createGateway(@RequestBody @Validated GatewayDto gatewayDto) {
        logger.info("创建网关信息! gatewayDto = {}", JSON.toJSONString(gatewayDto));
        ErrorCode errorCode = gatewayService.checkCreateParam(gatewayDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        long id = gatewayService.create(gatewayDto);
        return apiReturn(new Result(id));
    }

    /**
     * 修改网关信息
     *
     * @param gatewayDto
     * @return
     */
    @PostMapping(params = {"Action=UpdateGateway"})
    public Object updateGateway(@RequestBody GatewayDto gatewayDto) {
        logger.info("修改网关信息! gatewayDto = {}", JSON.toJSONString(gatewayDto));
        ErrorCode errorCode = gatewayService.checkUpdateParam(gatewayDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        gatewayService.update(gatewayDto);
        return apiReturn(new Result());
    }

    /**
     * 删除网关信息
     *
     * @param id
     */
    @GetMapping(params = {"Action=DeleteGateway"})
    public Object deleteGateway(@RequestParam(name = "Id") long id) {
        logger.info("删除网关信息! Id = {}", id);
        GatewayDto gatewayDto = gatewayService.get(id);
        if (gatewayDto == null) {
            return apiReturn(new Result());
        }
        ErrorCode errorCode = gatewayService.checkDeleteParam(gatewayDto);
        if (!CommonErrorCode.SUCCESS.equals(errorCode)) {
            return apiReturn(Result.err(errorCode));
        }
        gatewayService.delete(gatewayDto);
        return apiReturn(new Result());
    }

    /**
     * 查询所有网关信息
     *
     * @return
     */
    @GetMapping(params = {"Action=DescribeAllGateway"})
    public Object findAll() {
        logger.info("查询所有网关信息! ");
        List<? extends GatewayDto> gatewayDtoList = gatewayService.findAll();
        return apiReturn(new Result<>(gatewayDtoList));
    }

    /**
     * 通过Id获取网关信息信息
     *
     * @param id
     * @return
     */
    @GetMapping(params = {"Action=DescribeGatewayById"})
    public Object get(@RequestParam(name = "Id") long id) {
        logger.info("通过Id获取网关信息! id = {}", id);
        GatewayDto gatewayDto = gatewayService.get(id);
        return apiReturn(new Result<>(gatewayDto));
    }

    /**
     * 通过网关名称分页获取网关信息信息
     *
     * @param name   网关名称
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping(params = {"Action=DescribeGatewayByNamePaged"})
    public Object findByNamePaged(@RequestParam(name = "Name", required = false) String name,
                                  @RequestParam(name = "Offset", defaultValue = "0", required = false) long offset,
                                  @RequestParam(name = "Limit", defaultValue = "20", required = false) long limit) {
        logger.info("通过网关名称获取网关信息! Name = {} , offset = {} , limit = {}", name, offset, limit);
        List<? extends GatewayDto> gatewayDtoList = gatewayService.findByName(name, offset, limit);
        long gatewayCount = gatewayService.countByName(name);
        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put(RESULT, gatewayDtoList);
        resultMap.put(TOTAL, gatewayCount);
        return apiReturn(new PageResult(gatewayDtoList, gatewayCount));
    }

}