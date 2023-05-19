package org.hango.cloud.common.infra.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.hango.cloud.common.infra.credential.pojo.CertificateInfoPO;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/8/7 16:17
 **/
@Mapper
public interface CertificateInfoMapper extends BaseMapper<CertificateInfoPO> {

}
