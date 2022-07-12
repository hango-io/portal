package org.hango.cloud.dashboard.apiserver.service;

import org.hango.cloud.dashboard.apiserver.dto.PublishedServiceInfoForSkiffDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface IOpenApiService {
    List<PublishedServiceInfoForSkiffDto> getPublishedServiceInfoByAccountId(String accountId, HttpServletRequest request);
}
