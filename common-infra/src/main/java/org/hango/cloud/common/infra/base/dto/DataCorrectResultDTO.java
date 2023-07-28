//package org.hango.cloud.common.infra.base.dto;
//
//import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @Author zhufengwei
// * @Date 2023/1/17
// */
//public class DataCorrectResultDTO {
//    /**
//     * 成功列表
//     */
//    private List<Long> successList;
//
//    /**
//     * 失败列表
//     */
//    private List<Long> faildList;
//
//    /**
//     * 总数
//     */
//    private int totalCount;
//
//    public DataCorrectResultDTO() {
//    }
//
//    public DataCorrectResultDTO(List<Long> successList, List<Long> faildList, int totalCount) {
//        this.successList = successList;
//        this.faildList = faildList;
//        this.totalCount = totalCount;
//    }
//
//    public List<Long> getSuccessList() {
//        return successList;
//    }
//
//    public void setSuccessList(List<Long> successList) {
//        this.successList = successList;
//    }
//
//    public List<Long> getFaildList() {
//        return faildList;
//    }
//
//    public void setFaildList(List<Long> faildList) {
//        this.faildList = faildList;
//    }
//
//    public int getTotalCount() {
//        return totalCount;
//    }
//
//    public void setTotalCount(int totalCount) {
//        this.totalCount = totalCount;
//    }
//
//    public static DataCorrectResultDTO ofTotalAndFailed(List<Long> totalList, List<Long> failedList){
//        DataCorrectResultDTO publishResultDto = new DataCorrectResultDTO();
//        publishResultDto.setTotalCount(totalList.size());
//        publishResultDto.setFaildList(failedList);
//        if (CollectionUtils.isNotEmpty(totalList)){
//            if (CollectionUtils.isNotEmpty(failedList)){
//                totalList.removeAll(failedList);
//            }
//            publishResultDto.setSuccessList(totalList);
//        }
//        return publishResultDto;
//    }
//
//    public void addSuccess(Long id) {
//        if (successList == null){
//            successList = new ArrayList<>();
//        }
//        successList.add(id);
//    }
//
//    public void addFaild(Long id) {
//        if (faildList == null){
//            faildList = new ArrayList<>();
//        }
//        faildList.add(id);
//    }
//    public static DataCorrectResultDTO ofEmpty(){
//        return new DataCorrectResultDTO(null, null, 0);
//    }
//}
//
