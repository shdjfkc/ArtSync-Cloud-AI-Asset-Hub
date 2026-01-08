package com.acaah.artsync.api.imagesearch.sub;

import com.acaah.artsync.exception.BusinessException;
import com.acaah.artsync.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取图片列表页面地址的工具类
 * 使用Jsoup解析HTML页面，提取特定脚本中的firstUrl值
 */
@Slf4j
public class GetImageFirstUrlApi {

    /**
     * 获取图片列表页面地址
     * 该方法通过解析目标页面的HTML内容，查找包含firstUrl的脚本标签，并提取其中的URL值

     *
     * @param url 目标页面的URL地址
     * @return 提取到的图片firstUrl
     * @throws BusinessException 当未找到URL或发生异常时抛出业务异常
     */
    public static String getImageFirstUrl(String url) {
        try {
            // 使用 Jsoup 获取 HTML 内容
            // 设置5秒超时时间，防止请求卡死
            Document document = Jsoup.connect(url)
                    .timeout(5000)
                    .get();

            // 获取所有 <script> 标签
            // 这些标签中可能包含我们需要的firstUrl信息
            Elements scriptElements = document.getElementsByTag("script");

            // 遍历找到包含 `firstUrl` 的脚本内容
            // 通过检查脚本内容是否包含"firstUrl"字符串来定位目标脚本
            for (Element script : scriptElements) {
                String scriptContent = script.html();
                if (scriptContent.contains("\"firstUrl\"")) {
                    // 正则表达式提取 firstUrl 的值
                    // 匹配模式为："firstUrl" : "URL值"
                    Pattern pattern = Pattern.compile("\"firstUrl\"\\s*:\\s*\"(.*?)\"");
                    Matcher matcher = pattern.matcher(scriptContent);
                    if (matcher.find()) {
                        String firstUrl = matcher.group(1);
                        // 处理转义字符
                        // 将脚本中的"\/"转换成"/"
                        firstUrl = firstUrl.replace("\\/", "/");
                        return firstUrl;
                    }
                }
            }

            // 如果遍历完所有脚本仍未找到firstUrl，抛出业务异常
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到 url");
        } catch (Exception e) {
            // 记录错误日志
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        // 请求目标 URL
        String url = "https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData[isLogoShow]=1&f=all&isLogoShow=1&session_id=9976667499045488714&sign=1211a1616da44f43d326001767081052&tpl_from=pc";
        String imageFirstUrl = getImageFirstUrl(url);
        System.out.println("搜索成功，结果 URL：" + imageFirstUrl);
    }
}
