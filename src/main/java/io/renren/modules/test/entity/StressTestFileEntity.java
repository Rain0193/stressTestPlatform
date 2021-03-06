package io.renren.modules.test.entity;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Date;

/**
 * 性能测试用例文件
 */
public class StressTestFileEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    private Long fileId;

    /**
     * 用例名称
     */
    @Min(value= 1)
    private Long caseId;

    /**
     * 用例文件名
     */
    @NotBlank(message="文件名不能为空")
    private String originName;

    /**
     * 用例保存文件名，唯一化
     */
    private String fileName;

    /**
     * 任务状态  0：初始状态  1：正在运行  2：成功执行  3：运行出现异常
     */
    private Integer status;

    /**
     * 状态  0：保存测试报告原始文件  1：不需要测试报告
     * 默认0
     */
    private Integer reportStatus;

    /**
     * 状态  0：需要前端监控  1：不需要前端监控
     * 默认0
     */
    private Integer webchartStatus;

    /**
     * 状态  0：不需要前端显示日志  1：前端仅显示错误日志
     *      2：前端仅显示正确日志   3：前端正确和错误日志都显示
     * 默认 0
     */
    private Integer weblogStatus;

    /**
     * 提交的用户
     */
    private String addBy;

    /**
     * 修改的用户
     */
    private String updateBy;

    /**
     * 提交的时间
     */
    private Date addTime;

    /**
     * 更新的时间
     */
    private Date updateTime;

    /**
     * 分布式节点的字符串，可以用来判断是否使用分布式。
     * 不入库。
     */
    private String slaveStr;

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getAddBy() {
        return addBy;
    }

    public void setAddBy(String addBy) {
        this.addBy = addBy;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getReportStatus() {
        return reportStatus;
    }

    public void setReportStatus(Integer reportStatus) {
        this.reportStatus = reportStatus;
    }

    public Integer getWebchartStatus() {
        return webchartStatus;
    }

    public void setWebchartStatus(Integer webchartStatus) {
        this.webchartStatus = webchartStatus;
    }

    public Integer getWeblogStatus() {
        return weblogStatus;
    }

    public void setWeblogStatus(Integer weblogStatus) {
        this.weblogStatus = weblogStatus;
    }

    public String getSlaveStr() {
        return slaveStr;
    }

    public void setSlaveStr(String slaveStr) {
        this.slaveStr = slaveStr;
    }
}
