package org.hango.cloud.common.infra.base.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/7 16:17
 **/
@Repository
public interface CertificateInfoMapper extends BaseMapper<CertificateInfoPO> {

}
