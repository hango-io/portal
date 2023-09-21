package org.hango.cloud.common.advanced.ops.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: zhufengwei.sx
 * @Date: 2022/6/19 11:07
 **/
public class PublishResultDto {
    /**
     * 成功列表
     */
    private List<Long> successList;

    /**
     * 失败列表
     */
    private List<Long> faildList;

    /**
     * 总数
     */
    private int totalCount;

    public PublishResultDto() {
    }

    public PublishResultDto(List<Long> successList, List<Long> faildList, int totalCount) {
        this.successList = successList;
        this.faildList = faildList;
        this.totalCount = totalCount;
    }

    public static PublishResultDto ofNull() {
        return new PublishResultDto(null, null, 0);
    }

    public static PublishResultDto ofInit(int totalCount) {
        PublishResultDto publishResultDto = new PublishResultDto();
        publishResultDto.setTotalCount(totalCount);
        if (totalCount > 0) {
            publishResultDto.setSuccessList(new ArrayList<>());
            publishResultDto.setFaildList(new ArrayList<>());
        }
        return publishResultDto;
    }

    public List<Long> getSuccessList() {
        return successList;
    }

    public void setSuccessList(List<Long> successList) {
        this.successList = successList;
    }

    public List<Long> getFaildList() {
        return faildList;
    }

    public void setFaildList(List<Long> faildList) {
        this.faildList = faildList;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void addSuccessResult(Long id) {
        successList.add(id);
    }

    public void addFailResult(Long id) {
        faildList.add(id);
    }
}
