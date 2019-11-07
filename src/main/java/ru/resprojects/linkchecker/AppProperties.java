package ru.resprojects.linkchecker;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "appmsg")
public class AppProperties {

    private Map<String, String> appMsg = new HashMap<>();
    private Map<String, String> nodeMsg = new HashMap<>();
    private Map<String, String> edgeMsg = new HashMap<>();

    public Map<String, String> getAppMsg() {
        return appMsg;
    }

    public Map<String, String> getNodeMsg() {
        return nodeMsg;
    }

    public Map<String, String> getEdgeMsg() {
        return edgeMsg;
    }

    public void setAppMsg(Map<String, String> appMsg) {
        this.appMsg = appMsg;
    }

    public void setNodeMsg(Map<String, String> nodeMsg) {
        this.nodeMsg = nodeMsg;
    }

    public void setEdgeMsg(Map<String, String> edgeMsg) {
        this.edgeMsg = edgeMsg;
    }
}
