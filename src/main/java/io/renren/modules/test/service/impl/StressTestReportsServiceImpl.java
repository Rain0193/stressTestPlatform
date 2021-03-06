package io.renren.modules.test.service.impl;

import io.renren.common.exception.RRException;
import io.renren.modules.test.dao.StressTestReportsDao;
import io.renren.modules.test.entity.StressTestReportsEntity;
import io.renren.modules.test.handler.ReportCreateResultHandler;
import io.renren.modules.test.service.StressTestReportsService;
import io.renren.modules.test.utils.StressTestUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service("stressTestReportsService")
public class StressTestReportsServiceImpl implements StressTestReportsService {

    @Autowired
    private StressTestReportsDao stressTestReportsDao;

    @Autowired
    private StressTestUtils stressTestUtils;

    @Override
    public StressTestReportsEntity queryObject(Long reportId) {
        return stressTestReportsDao.queryObject(reportId);
    }

    @Override
    public List<StressTestReportsEntity> queryList(Map<String, Object> map) {
        return stressTestReportsDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return stressTestReportsDao.queryTotal(map);
    }

    @Override
    public void save(StressTestReportsEntity stressCaseReports) {
        stressTestReportsDao.save(stressCaseReports);
    }

    @Override
    public void update(StressTestReportsEntity stressCaseReports) {
        stressTestReportsDao.update(stressCaseReports);
    }

    @Override
    @Transactional
    public void deleteBatch(Long[] reportIds) {
        Arrays.asList(reportIds).stream().forEach(reportId -> {
            StressTestReportsEntity stressTestReport = queryObject(reportId);
            String casePath = stressTestUtils.getCasePath();
            String reportName = stressTestReport.getReportName();
            //csv结果文件路径
            String csvPath = casePath + File.separator + reportName;
            //测试报告文件目录
            String reportPath = csvPath.substring(0, csvPath.lastIndexOf("."));
            try {
                FileUtils.forceDelete(new File(reportPath));
            } catch (FileNotFoundException e) {
                //doNothing
            } catch (IOException e) {
                throw new RRException("删除测试报告文件夹异常失败", e);
            }
            deleteReportCSV(stressTestReport);
            deleteReportZip(stressTestReport);
            try {
                String jmxDir = reportPath.substring(0, reportPath.lastIndexOf(File.separator));
                File jmxDirFile = new File(jmxDir);
                if (FileUtils.sizeOf(jmxDirFile) == 0L) {
                    FileUtils.forceDelete(jmxDirFile);
                }
            } catch (IllegalArgumentException | FileNotFoundException e) {
                //doNothing
            } catch (IOException e) {
                throw new RRException("删除测试报告上级文件夹异常失败", e);
            }
        });
        stressTestReportsDao.deleteBatch(reportIds);
    }

    @Override
    public void createReport(Long[] reportIds) {
        for (Long reportId : reportIds) {
            excuteJmeterCreateReport(reportId);
        }
    }

    @Override
    public void deleteReportCSV(StressTestReportsEntity stressCaseReports) {
        String casePath = stressTestUtils.getCasePath();
        String reportName = stressCaseReports.getReportName();
        //csv结果文件路径
        String csvPath = casePath + File.separator + reportName;

        // 为了FileNotFoundException，找不到说明已经删除
        try {
            FileUtils.forceDelete(new File(csvPath));
        } catch (FileNotFoundException e) {
            //doNothing
        } catch (IOException e) {
            throw new RRException("删除测试报告来源文件异常失败", e);
        }
    }

    public void deleteReportZip(StressTestReportsEntity stressCaseReports) {
        String casePath = stressTestUtils.getCasePath();
        String reportName = stressCaseReports.getReportName();
        //csv结果文件路径
        String csvPath = casePath + File.separator + reportName;
        //测试报告文件目录
        String reportPathDir = csvPath.substring(0, csvPath.lastIndexOf("."));
        //zip文件名
        String reportZipPath = reportPathDir + ".zip";

        // 为了FileNotFoundException，找不到说明已经删除
        try {
            FileUtils.forceDelete(new File(reportZipPath));
        } catch (FileNotFoundException e) {
            //doNothing
        } catch (IOException e) {
            throw new RRException("删除测试报告zip文件异常失败", e);
        }
    }

    /**
     * 获取zip文件
     */
    @Override
    public FileSystemResource getZipFile(StressTestReportsEntity reportsEntity) throws IOException {
        String casePath = stressTestUtils.getCasePath();
        String reportName = reportsEntity.getReportName();
        //csv结果文件路径
        String csvPath = casePath + File.separator + reportName;
        //测试报告文件目录
        String reportPathDir = csvPath.substring(0, csvPath.lastIndexOf("."));

        //如果测试报告文件目录不存在，直接打断
        File reportDir = new File(reportPathDir);
        if (!reportDir.exists()) {
            throw new RRException("请先生成测试报告！");
        }

        //zip文件名
        String reportZipPath = reportPathDir + ".zip";

        FileSystemResource zipFileResource = new FileSystemResource(reportZipPath);

        if (!zipFileResource.exists()) {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(reportZipPath), Charset.forName("GBK"));
            try {
                File zipFile = new File(reportPathDir);
                zip(out, zipFile, zipFile.getName());
            } finally {
                out.close();
            }
        }

        return zipFileResource;
    }

    /**
     * 递归打包文件夹/文件
     */
    public void zip(ZipOutputStream zOut, File file, String name) throws IOException {
            if (file.isDirectory()) {
                File[] listFiles = file.listFiles();
                name += "/";
                zOut.putNextEntry(new ZipEntry(name));
                for (File listFile : listFiles) {
                    zip(zOut, listFile, name + listFile.getName());
                }
            } else {
                FileInputStream in = new FileInputStream(file);
                try{
                    zOut.putNextEntry(new ZipEntry(name));
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zOut.write(buffer, 0, len);
                        zOut.flush();
                    }
                } finally {
                    in.close();
                }
            }
    }

    /**
     * 使用Jmeter自带的生成测试报告脚本
     */
    @Transactional
    public void excuteJmeterCreateReport(Long reportId) {
        StressTestReportsEntity stressTestReport = queryObject(reportId);

        //首先判断，如果file_size为0或者空，说明没有结果文件，直接报错打断。
        if (stressTestReport.getFileSize() == 0L || stressTestReport.getFileSize() == null) {
            throw new RRException("找不到测试结果文件，请确认压测已经结束！");
        }

        String casePath = stressTestUtils.getCasePath();
        String reportName = stressTestReport.getReportName();

        //csv结果文件路径
        String csvPath = casePath + File.separator + reportName;
        //测试报告文件目录
        String reportPathDir = csvPath.substring(0, csvPath.lastIndexOf("."));

        //如果测试报告文件目录已经存在，说明生成过测试报告，直接打断
        File reportDir = new File(reportPathDir);
        if (reportDir.exists()) {
            throw new RRException("已经存在测试报告不要重复创建！");
        }

        //设置开始执行命令生成报告
        stressTestReport.setStatus(StressTestUtils.RUNNING);
        update(stressTestReport);

        //开始执行命令行
        String jmeterHomeBin = stressTestUtils.getJmeterHomeBin();
        String jmeterExc = stressTestUtils.getJmeterExc();

        CommandLine cmdLine = new CommandLine(jmeterHomeBin + File.separator + jmeterExc);
        // 设置参数，-g 指定测试结果文件路径，仅用于生成测试报告
        cmdLine.addArgument("-g");
        Map map = new HashMap();
        map.put("csvFile", new File(csvPath));
        map.put("reportDir", reportDir);
        cmdLine.addArgument("${csvFile}");
        // -o 指定测试报告生成文件夹必须为空或者不存在，这里必须为不存在。
        cmdLine.addArgument("-o");
        cmdLine.addArgument("${reportDir}");
        // 指定需要执行的JMX脚本
        cmdLine.setSubstitutionMap(map);

        DefaultExecutor executor = new DefaultExecutor();
        //非阻塞方式运行脚本命令，不耽误前端的操作。
        //流操作在executor执行源码中已经关闭。
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
        // 设置成功输入及错误输出，用于追查问题，打印日志。
        executor.setStreamHandler(streamHandler);

        try {
            // 自定义的钩子程序
            ReportCreateResultHandler resultHandler =
                    new ReportCreateResultHandler(stressTestReport,
                            this, outputStream, errorStream);
            // 执行脚本命令
            executor.execute(cmdLine, resultHandler);
        } catch (IOException e) {
            //保存状态，执行出现异常
            stressTestReport.setStatus(StressTestUtils.RUN_ERROR);
            update(stressTestReport);
            throw new RRException("执行生成测试报告脚本异常！", e);
        }
    }
}
