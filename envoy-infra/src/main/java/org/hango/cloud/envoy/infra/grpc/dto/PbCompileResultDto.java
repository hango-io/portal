package org.hango.cloud.envoy.infra.grpc.dto;

import java.util.List;

/**
 * @author Xin Li
 * @date 2023/1/9 18:46
 */
public class PbCompileResultDto {
    /**
     * pb文件名称
     */
    private String pbFileName;

    /**
     * pb文件内容
     */
    private String pbFileContent;
    /**
     * proto服务列表
     */
    private List<String> pbServiceList;
    public PbCompileResultDto() {
    }

    public PbCompileResultDto(String pbFileName, String pbFileContent, List<String> pbServiceList) {
        this.pbFileName = pbFileName;
        this.pbFileContent = pbFileContent;
        this.pbServiceList = pbServiceList;
    }

    public String getPbFileName() {
        return pbFileName;
    }

    public void setPbFileName(String pbFileName) {
        this.pbFileName = pbFileName;
    }

    public String getPbFileContent() {
        return pbFileContent;
    }

    public void setPbFileContent(String pbFileContent) {
        this.pbFileContent = pbFileContent;
    }

    public List<String> getPbServiceList() {
        return pbServiceList;
    }

    public void setPbServiceList(List<String> pbServiceList) {
        this.pbServiceList = pbServiceList;
    }
}
