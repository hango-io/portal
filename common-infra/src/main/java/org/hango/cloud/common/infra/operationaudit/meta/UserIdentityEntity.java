package org.hango.cloud.common.infra.operationaudit.meta;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import org.hango.cloud.common.infra.base.dto.CommonExtensionDto;

/**
 * @author zhangbaojun
 * @version $Id: UserIdentityEntity.java, v 1.0 2018年07月23日 11:05
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@With
public class UserIdentityEntity extends CommonExtensionDto {

    @JSONField(name = "AccountId")
    private String accountId;

    @JSONField(name = "UserName")
    private String userName;

    @JSONField(name = "AccessKeyId")
    private String accessKeyId;
}
