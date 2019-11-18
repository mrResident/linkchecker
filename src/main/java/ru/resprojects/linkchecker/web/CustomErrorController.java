package ru.resprojects.linkchecker.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import ru.resprojects.linkchecker.util.exeptions.ErrorInfo;
import ru.resprojects.linkchecker.util.exeptions.ErrorPlaceType;
import ru.resprojects.linkchecker.util.exeptions.ErrorType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

@Controller
public class CustomErrorController extends AbstractErrorController {

    private static final Logger LOG = LoggerFactory.getLogger(CustomErrorController.class);

    public CustomErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }

    @RequestMapping(value = "/error", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        HttpStatus status = getStatus(request);
        response.setStatus(status.value());
        Map<String, Object> model = getModel(request);
        ModelAndView modelAndView = resolveErrorView(request, response, status, model);
        LOG.debug("errorHtml: " + model);
        return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
    }

    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ErrorInfo error(HttpServletRequest request) {
        String errorRequestUrl = request.getScheme() + "://"+ request.getServerName()
            + ":" + request.getServerPort() + getModel(request).get("path").toString();
        ErrorInfo errorInfo = new ErrorInfo(errorRequestUrl,
            ErrorType.WRONG_REQUEST, ErrorPlaceType.APP, "Bad request");
        LOG.debug("error: " + errorInfo);
        return errorInfo;
    }

    private Map<String, Object> getModel(HttpServletRequest request) {
        return Collections.unmodifiableMap(getErrorAttributes(request, true));
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
