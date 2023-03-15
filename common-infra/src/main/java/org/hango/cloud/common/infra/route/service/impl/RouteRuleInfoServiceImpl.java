package org.hango.cloud.common.infra.route.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.math.NumberUtils;
import org.hango.cloud.common.infra.base.convert.RouteRuleConvert;
import org.hango.cloud.common.infra.base.errorcode.CommonErrorCode;
import org.hango.cloud.common.infra.base.errorcode.ErrorCode;
import org.hango.cloud.common.infra.base.holder.ProjectTraceHolder;
import org.hango.cloud.common.infra.base.mapper.RouteRuleInfoMapper;
import org.hango.cloud.common.infra.base.meta.BaseConst;
import org.hango.cloud.common.infra.base.util.PageUtil;
import org.hango.cloud.common.infra.route.dao.IRouteRuleInfoDao;
import org.hango.cloud.common.infra.route.dto.CopyRuleDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleDto;
import org.hango.cloud.common.infra.route.dto.RouteRuleQueryDto;
import org.hango.cloud.common.infra.route.dto.RouteStringMatchDto;
import org.hango.cloud.common.infra.route.pojo.RouteRuleInfoPO;
import org.hango.cloud.common.infra.route.pojo.RouteRuleQuery;
import org.hango.cloud.common.infra.route.service.IRouteRuleInfoService;
import org.hango.cloud.common.infra.routeproxy.service.IRouteRuleProxyService;
import org.hango.cloud.common.infra.service.dto.ServiceDto;
import org.hango.cloud.common.infra.service.service.IServiceInfoService;
import org.hango.cloud.common.infra.serviceproxy.service.IServiceProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hango.cloud.common.infra.base.convert.RouteRuleConvert.fillMatchMeta;
import static org.hango.cloud.common.infra.base.convert.RouteRuleConvert.fillMatchView;

/**
 * @author xin li
 * @date 2022/9/6 14:14
 */
@Service
public class RouteRuleInfoServiceImpl implements IRouteRuleInfoService {
    private static final Logger logger = LoggerFactory.getLogger(RouteRuleInfoServiceImpl.class);

    @Autowired
    private IServiceInfoService serviceInfoService;
    @Autowired
    private IServiceProxyService serviceProxyService;
    @Autowired
    private IRouteRuleProxyService routeRuleProxyService;

    @Autowired
    private RouteRuleInfoMapper routeRuleInfoMapper;

    @Autowired
    private IRouteRuleInfoDao routeRuleInfoDao;

    @Override
    public long create(RouteRuleDto routeRuleDto) {
        RouteRuleInfoPO routeRuleInfoPO = toMeta(routeRuleDto);
        routeRuleInfoPO.setId(null);
        routeRuleInfoMapper.insert(routeRuleInfoPO);
        return routeRuleInfoPO.getId();
    }

    @Override
    public long update(RouteRuleDto routeRuleDto) {
        RouteRuleInfoPO routeRuleInfoPO = toMeta(routeRuleDto);
        return routeRuleInfoMapper.updateById(routeRuleInfoPO);
    }

    @Override
    public void delete(RouteRuleDto routeRuleDto) {
        if (routeRuleDto.getId() == null){
            return;
        }
        routeRuleInfoMapper.deleteById(routeRuleDto.getId());
    }

    @Override
    public RouteRuleDto get(long id) {
        return toView(routeRuleInfoMapper.selectById(id));
    }

    @Override
    public RouteRuleDto toView(RouteRuleInfoPO routeRuleInfoPO) {
        if (routeRuleInfoPO == null) {
            return null;
        }
        RouteRuleDto routeRuleDto = new RouteRuleDto();
        BeanUtils.copyProperties(routeRuleInfoPO, routeRuleDto);
        ServiceDto serviceDto = serviceInfoService.get(routeRuleDto.getServiceId());
        routeRuleDto.setServiceName(serviceDto.getDisplayName());
        routeRuleDto.setServiceType(serviceDto.getServiceType());
        fillMatchView(routeRuleDto, routeRuleInfoPO);
        return routeRuleDto;
    }

    @Override
    public RouteRuleInfoPO toMeta(RouteRuleDto routeRuleDto) {
        if (routeRuleDto == null) {
            return null;
        }
        RouteRuleInfoPO routeRuleInfoPO = new RouteRuleInfoPO();
        BeanUtils.copyProperties(routeRuleDto, routeRuleInfoPO);
        fillMatchMeta(routeRuleInfoPO, routeRuleDto);
        return routeRuleInfoPO;
    }

    @Override
    public ErrorCode checkCreateParam(RouteRuleDto routeRuleDto) {
        boolean exist =  existRouteRule(routeRuleDto.getRouteRuleName().trim());
        if (exist) {
            logger.info("同名规则已存在，不允许重复创建");
            return CommonErrorCode.SAME_NAME_ROUTE_RULE_EXIST;
        }
        return checkParam(routeRuleDto);
    }


    @Override
    public ErrorCode checkUpdateParam(RouteRuleDto routeRuleDto) {
        RouteRuleDto ruleInDB = get(routeRuleDto.getId());
        if (null == ruleInDB) {
            logger.info("指定的路由规则不存在，id:{}", routeRuleDto.getId());
            return CommonErrorCode.NO_SUCH_ROUTE_RULE;
        }
        return checkParam(routeRuleDto);
    }

    private ErrorCode checkParam(RouteRuleDto routeRuleDto){
        RouteStringMatchDto uriMatchDto = routeRuleDto.getUriMatchDto();
        if (CollectionUtils.isEmpty(uriMatchDto.getValue())) {
            logger.info("更新路由规则，uri path为空，不允许更新");
            return CommonErrorCode.NO_ROUTE_RULE_PATH;
        }
        //正则中不允许出现nginx捕获正则
        if (!BaseConst.URI_TYPE_EXACT.equals(uriMatchDto.getType()) && uriMatchDto.getValue().stream().anyMatch(path -> Pattern.matches(BaseConst.NGINX_CAPTURE_REGEX, path))) {
            logger.info("更新路由，path中包含nginx 捕获正则，不允许更新");
            return CommonErrorCode.ROUTE_RULE_CONTAINS_NGINX_CAPTURE;
        }
        if (routeRuleDto.getMethodMatchDto() != null && !BaseConst.CONST_METHODS.containsAll(routeRuleDto.getMethodMatchDto().getValue())) {
            logger.info("更新路由，method参数填写不正确");
            return CommonErrorCode.ROUTE_RULE_METHOD_INVALID;
        }
        if (existSameRouteRule(routeRuleDto)) {
            logger.info("发布路由规则，存在参数完全相同路由规则，不允许发布");
            return CommonErrorCode.SAME_PARAM_ROUTE_RULE_EXIST;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public ErrorCode checkDeleteParam(RouteRuleDto routeRuleDto) {
        if (routeRuleDto.getPublishStatus() == NumberUtils.INTEGER_ONE) {
            logger.info("删除路由规则，已发布路由列表不存在数据，路由列表存在脏数据，需要fix数据");
            return CommonErrorCode.ROUTE_RULE_ALREADY_PUBLISHED;
        }
        if (!CollectionUtils.isEmpty(routeRuleProxyService.getRouteRuleProxyByRouteRuleId(routeRuleDto.getId()))) {
            logger.info("删除路由规则，已发布路由列表中仍然存在数据");
            return CommonErrorCode.ROUTE_RULE_ALREADY_PUBLISHED;
        }
        return CommonErrorCode.SUCCESS;
    }

    private boolean existRouteRule(String routeRuleName) {
        RouteRuleInfoPO query = RouteRuleInfoPO.builder().routeRuleName(routeRuleName).projectId(ProjectTraceHolder.getProId()).build();
        return routeRuleInfoMapper.exists(new QueryWrapper<>(query));
    }

    private boolean existSameRouteRule(RouteRuleDto routeRuleDto) {
        List<RouteRuleInfoPO> ruleInfoListByMatchInfo = routeRuleInfoDao.getRuleInfoListByMatchInfo(toMeta(routeRuleDto)).stream()
                .filter(o -> !o.getId().equals(routeRuleDto.getId())).collect(Collectors.toList());
        return !CollectionUtils.isEmpty(ruleInfoListByMatchInfo);
    }

    @Override
    public Page<RouteRuleInfoPO> getRouteRulePage(RouteRuleQueryDto queryDto) {
        return routeRuleInfoDao.getRuleInfoListPage(RouteRuleConvert.toMeta(queryDto), PageUtil.of(queryDto.getLimit(), queryDto.getOffset()));
    }

    @Override
    public List<RouteRuleInfoPO> getRouteRuleList(RouteRuleQuery query) {
        return routeRuleInfoDao.getRuleInfoList(query);
    }


    @Override
    public ErrorCode checkCopyParam(CopyRuleDto copyRuleDto) {
        RouteRuleDto ruleInDB = get(copyRuleDto.getRouteRuleId());
        if (ruleInDB == null) {
            logger.info("复制路由规则，路由规则不存在");
            return CommonErrorCode.NO_SUCH_ROUTE_RULE;
        }
        if (ruleInDB.getPriority() == copyRuleDto.getPriority()) {
            logger.info("复制路由规则，未修改路由规则优先级，不允许复制，priority:{}", copyRuleDto.getPriority());
            return CommonErrorCode.NOT_MODIFY_PRIORITY;
        }
        if (ruleInDB.getRouteRuleName().equals(copyRuleDto.getRouteRuleName())) {
            logger.info("复制路由规则，未修改路由规则名称，不允许复制，routeRuleName:{}", copyRuleDto.getRouteRuleName());
            return CommonErrorCode.NOT_MODIFY_ROUTE_RULE_NAME;
        }
        if (existRouteRule(copyRuleDto.getRouteRuleName())) {
            logger.info("复制路由规则，route名称冲突，不允许复制");
            return CommonErrorCode.SAME_NAME_ROUTE_RULE_EXIST;
        }
        if (serviceInfoService.get(copyRuleDto.getServiceId()) == null) {
            logger.info("复制路由规则，目的服务不存在，serviceId:{}", copyRuleDto.getServiceId());
            return CommonErrorCode.NO_SUCH_SERVICE;
        }
        if (existSameRouteRule(ruleInDB)) {
            logger.info("复制路由规则，存在完全相同的路由，不允许复制");
            return CommonErrorCode.SAME_PARAM_ROUTE_RULE_EXIST;
        }
        return CommonErrorCode.SUCCESS;
    }

    @Override
    public long copyRouteRule(CopyRuleDto copyRuleDto) {
        RouteRuleInfoPO routeRuleInfoPO = routeRuleInfoMapper.selectById(copyRuleDto.getRouteRuleId());
        routeRuleInfoPO.setServiceId(copyRuleDto.getServiceId());
        long orders = routeRuleInfoPO.getOrders() + (copyRuleDto.getPriority() - routeRuleInfoPO.getPriority()) * 1000000;
        routeRuleInfoPO.setOrders(orders);
        routeRuleInfoPO.setPriority(copyRuleDto.getPriority());
        routeRuleInfoPO.setRouteRuleName(copyRuleDto.getRouteRuleName());
        routeRuleInfoPO.setDescription(copyRuleDto.getDescription());
        //复制的路由，均属于未发布
        routeRuleInfoPO.setPublishStatus(0);
        routeRuleInfoPO.setId(null);
        routeRuleInfoMapper.insert(routeRuleInfoPO);
        return routeRuleInfoPO.getId();
    }

    @Override
    public List<RouteRuleDto> getRouteRuleList(List<Long> routeRuleIdList) {
        if (CollectionUtils.isEmpty(routeRuleIdList)) {
            return Lists.newArrayList();
        }
        List<RouteRuleInfoPO> routeRuleInfoPOS = routeRuleInfoMapper.selectBatchIds(routeRuleIdList);
        return routeRuleInfoPOS.stream().map(this::toView).collect(Collectors.toList());
    }

    @Override
    public int updatePublishStatus(Long id, Integer publishStatus) {
        RouteRuleInfoPO target = routeRuleInfoMapper.selectById(id);
        target.setPublishStatus(publishStatus);
        return routeRuleInfoMapper.updateById(target);
    }
}
