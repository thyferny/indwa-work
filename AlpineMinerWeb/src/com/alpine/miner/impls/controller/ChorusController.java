package com.alpine.miner.impls.controller;

import com.alpine.miner.framework.DataStore;
import com.alpine.miner.framework.RequestUtil;
import com.alpine.miner.framework.RowInfo;
import com.alpine.miner.impls.chorus.ChorusConfiguration;
import com.alpine.miner.impls.chorus.ChorusUtil;
import com.alpine.miner.impls.resource.ResourceInfo;
import com.alpine.miner.impls.web.resource.FilePersistence;
import com.alpine.miner.impls.web.resource.FlowInfo;
import com.alpine.miner.impls.web.resource.WebWorkFlowRunner;
import com.alpine.miner.interfaces.ChorusConfigManager;
import com.alpine.miner.security.AuthenticationProvider;
import com.alpine.miner.security.UserInfo;
import com.alpine.miner.security.impl.ProviderFactory;
import com.alpine.miner.workflow.runner.FlowRunningHelper;
import com.alpine.utility.hadoop.HadoopConstants;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author robbie
 * Date: 01/14/2013
 */

@Controller
@RequestMapping("/main/chorus.do")
public class ChorusController extends AbstractControler {

    private static final String TEST_CONN_ROUTE = "/status";
    private static final String WORKSPACE_ROUTE = "/workspaces";
    private static final String WORKFILE_ROUTE = "/workfiles";


    private static final String ACTIVE = "active";
    private static final String WORKSPACE = "workspace";
    private static final String DESC = "description";
    public static final String API_KEY = "api_key";
    private static final String ALPINE_ID = "alpine_id";
    private static final String FLOW_NAME = "file_name";
    private static final String CHORUS_TYPE = "type";
    private static final String CHORUS_TYPE_ALPINE = "alpine";
    private static final String INSIGHT = "is_insight";
    private static final String ENTITY_TYPE = "entity_type";
    private static final String ENTITY_ID = "entity_id";
    public static final String ID = "id";
    public static final String CHORUS_WORKFILE_ID = "chorus_workfile_id";
    public static final String CHORUS_WORKFILE_TYPE = "chorus_workfile_type";
    public static final String CHORUS_WORKFILE_NAME = "chorus_workfile_name";


    public ChorusController() throws Exception {
        super();
    }

    @RequestMapping(params = "method=getChorusConfig", method = RequestMethod.GET)
    public void getChorusConfig(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException {
        try {
            ChorusConfiguration config = ChorusConfigManager.INSTANCE.readConfig();
            ProtocolUtil.sendResponse(response, config);
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }

    @RequestMapping(params = "method=updateChorusConfig", method = RequestMethod.POST)
    public void updateChorusConfig(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException {
        ChorusConfiguration config = ProtocolUtil.getRequest(request, ChorusConfiguration.class);
        try {
            ChorusConfigManager.INSTANCE.saveConfig(config);
            returnSuccess(response);
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }

    @RequestMapping(params = "method=testChorusConnection", method = RequestMethod.POST)
    public void testChorusConnection(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException {
        ChorusConfiguration config = ProtocolUtil.getRequest(request, ChorusConfiguration.class);
        String url = config.getURLBase(TEST_CONN_ROUTE);
        try {
            HttpClient httpClient = getHttpClientWithTimeout();

            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            if ( responseCode == 200) {
                BasicResponseHandler handler = new BasicResponseHandler();
                String body = handler.handleResponse(httpResponse);
                //System.out.println(body);
            } else {
                throw new Exception("The Chorus server is not running");
            }
            EntityUtils.consume(httpResponse.getEntity());
            returnSuccess(response);
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }

    @RequestMapping(params = "method=updateUserAPIKey", method = RequestMethod.GET)
    public void updateUserAPIKey(@RequestParam(API_KEY) String apiKey,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 ModelMap map) throws IOException {
        try {
            if (apiKey == null || apiKey.length() < 1) {
                throw new Exception("Invalid Chorus API key");
            }
            String loginName = getUserName(request);
            AuthenticationProvider auth = ProviderFactory.getAuthenticator(loginName);
            UserInfo userInfo = auth.getUserInfoByName(loginName);
            userInfo.setChorusKey(apiKey);
            auth.updateUser(userInfo);
            returnSuccess(response);
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }

    @RequestMapping(params = "method=getActiveWorkSpaces", method = RequestMethod.GET)
    public void getActiveWorkSpaces(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws IOException {
        try {
            ChorusConfiguration config = ChorusConfigManager.INSTANCE.readConfig();
            HttpClient httpClient = getHttpClientWithTimeout();
            String loginName = getUserName(request);
            AuthenticationProvider auth = ProviderFactory.getAuthenticator(loginName);
            UserInfo userInfo = auth.getUserInfoByName(loginName);
            String key = userInfo.getChorusKey();
            if (key == null || key.length() < 1) {
                throw new Exception("Please configure your Chorus API key");
            }

            List<BasicNameValuePair> queryParams = new ArrayList<BasicNameValuePair>();
            queryParams.add(new BasicNameValuePair(API_KEY, key));
            queryParams.add(new BasicNameValuePair(ACTIVE, "1"));
            String url = buildURLWithParams(config.getURLBase(WORKSPACE_ROUTE), queryParams);

            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            BasicResponseHandler handler = new BasicResponseHandler();
            String body = handler.handleResponse(httpResponse);
            EntityUtils.consume(httpResponse.getEntity());
            ProtocolUtil.sendResponse(response, body);

        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }


    /*
    * Publish flow to Chorus
    * WORKSPACE: Chorus workspace id for publishing
    * DESC: (optional) Description for creating the Chorus workfile
    * */
    @RequestMapping(params = "method=publishFlow", method = RequestMethod.POST)
    public void publishFlow(@RequestParam(WORKSPACE) String workspace,
                            @RequestParam(DESC) String description,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            ModelMap model) throws IOException {
        try {
            FlowInfo[] infos = ProtocolUtil.getRequest(request, FlowInfo[].class);
            //Only publish 1 flow at a time

            String loginName = getUserName(request);
            AuthenticationProvider auth = ProviderFactory.getAuthenticator(loginName);
            UserInfo userInfo = auth.getUserInfoByName(loginName);
            String key = userInfo.getChorusKey();
            if (key == null || key.length() < 1) {
                throw new Exception("Please configure your Chorus API key");
            }

            // new id for flow
            String alpineId = getSnapshotID();
            String flowPath = infos[0].getKey();
            String flowName = infos[0].getId();

            // setup path for generating flow images
            String path = getServletContext().getRealPath("/images/icons");
            ChorusUtil.storeFlowImage(path, flowPath, alpineId);
            ChorusUtil.storeFlowFile(path, flowPath, alpineId);


            ChorusConfiguration config = ChorusConfigManager.INSTANCE.readConfig();
            List<BasicNameValuePair> queryParams = new ArrayList<BasicNameValuePair>();
            queryParams.add(new BasicNameValuePair(API_KEY, key));
            StringBuilder sb = new StringBuilder(WORKSPACE_ROUTE).append("/").append(workspace).append(WORKFILE_ROUTE);
            String url = buildURLWithParams(config.getURLBase(sb.toString()), queryParams);

            HttpClient httpClient = getHttpClientWithTimeout();
            HttpPost httpPost = new HttpPost(url);
            List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
            nvps.add(new BasicNameValuePair(FLOW_NAME, flowName));
            nvps.add(new BasicNameValuePair(ALPINE_ID, alpineId));
            nvps.add(new BasicNameValuePair(CHORUS_TYPE, CHORUS_TYPE_ALPINE));
            if (description != null && description.length() > 0 ) {
                nvps.add(new BasicNameValuePair(DESC, URLDecoder.decode(description, FilePersistence.ENCODING)));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

            HttpResponse httpResponse = httpClient.execute(httpPost);
            BasicResponseHandler handler = new BasicResponseHandler();
            //String body = handler.handleResponse(httpResponse);
            //System.out.println(body);
            EntityUtils.consume(httpResponse.getEntity());
            returnSuccess(response);
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }


    /*
    * Run WorkFlow from Chorus by chorusUID
    * @ ID: id workflow
    * @ API_KEY: authenticate from chorus
    * @ CHORUS_WORKFILE_ID: chorus workfile id to enable post comment/insight
    * @ CHORUS_WORKFILE_TYPE: chorus workfile id to enable post comment/insight
    *
    * */
    @RequestMapping(params = "method=runWorkFlow", method = RequestMethod.GET)
    public void runWorkFlow(@RequestParam(ID) String id,
                            @RequestParam(API_KEY) String apiKey,
                            @RequestParam(CHORUS_WORKFILE_ID) String chorusWorkfileID,
                            @RequestParam(CHORUS_WORKFILE_TYPE) String chorusWorkfileType,
                            @RequestParam(CHORUS_WORKFILE_NAME) String chorusWorkfileName,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            ModelMap map) throws IOException {
        try {
            String user = "";
            try {
                // user should have a session or we need to redirect them to login
                user = getUserName(request);
            } catch (Exception e) {
                //TODO: we should have a nice redirect to login page then redirect back here
                ProtocolUtil.sendChorusSessionFailure(response, new ErrorDTO(401, "Please login to Alpine and run the flow again"));
                return;
            }

            String flowFilePath = ChorusUtil.createFilePath(id, FilePersistence.AFM);
            String uuid = UUID.randomUUID().toString();

            RowInfo resultInfo = FlowRunnerController.initResultInfo(user, uuid, request.getLocale());
            String fullFileName = "chorusPublished/" + chorusWorkfileName;
            FlowInfo info = new FlowInfo();
            info.setKey(String.valueOf(System.currentTimeMillis()));
            info.setModifiedUser(user);
            info.setResourceType(ResourceInfo.ResourceType.Personal);
            info.setVersion("1");
            info.setId(chorusWorkfileName);

            String requestURL = request.getRequestURL().toString();
            HadoopConstants.Flow_Call_Back_URL = requestURL;

            WebWorkFlowRunner runner = new WebWorkFlowRunner(flowFilePath,  resultInfo, uuid, fullFileName, request.getLocale(), info);
            Map<String, WebWorkFlowRunner> userMap = FlowRunningHelper.RUNNER_MAP.get(user);
            if( userMap == null ) {
                userMap = new HashMap<String, WebWorkFlowRunner>();
                FlowRunningHelper.RUNNER_MAP.put(user,userMap);
            }
            userMap.put(uuid, runner);
            runner.runWorkFlow();
            DataStore ds = new DataStore();
            ds.setRow(resultInfo);

            StringBuilder redirectURL = new StringBuilder(getBaseURL(requestURL)).append("/alpine/result/result.jsp?uuid=").append(uuid);
            redirectURL.append("&flowName=").append(chorusWorkfileName);
            redirectURL.append("&" + CHORUS_WORKFILE_ID + "=").append(chorusWorkfileID);
            redirectURL.append("&" + CHORUS_WORKFILE_TYPE + "=").append(chorusWorkfileType);
            redirectURL.append("&" + CHORUS_WORKFILE_NAME + "=").append(chorusWorkfileName);
            response.sendRedirect(redirectURL.toString());
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }

    /*
    * Fetch WorkFlow image from Chorus by chorusUID
    * ID: Alpine unique id for workflow
    * API_KEY: for authorization with Alpine
    * */
    @RequestMapping(params = "method=getWorkFlowImage", method = RequestMethod.GET)
    public void getWorkFlowImage(@RequestParam(ID) String id,
                                 @RequestParam(API_KEY) String apiKey,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 ModelMap map) throws IOException {
        try {
            String imgPath = ChorusUtil.createFilePath(id, ".png");
            ProtocolUtil.sendChorusImgResponse(response, imgPath);
        } catch (Exception e) {
            generateErrorDTO(response, e, request.getLocale());
        }
    }

    @RequestMapping( params = "method=postComment", method = RequestMethod.POST)
    public void postComment(@RequestParam(INSIGHT) String isInsight,
                            @RequestParam(ENTITY_TYPE) String entityType,
                            @RequestParam(ENTITY_ID) String entityId,
                            HttpServletRequest request,
                            HttpServletResponse response,
                            ModelMap map) throws IOException {

        try {
            ChorusComment chorusComment = ProtocolUtil.getRequest(request, ChorusComment.class);
            String body = chorusComment.getComment();

            String loginName = getUserName(request);
            AuthenticationProvider auth = ProviderFactory.getAuthenticator(loginName);
            UserInfo userInfo = auth.getUserInfoByName(loginName);
            String key = userInfo.getChorusKey();
            if (key == null || key.length() < 1) {
                throw new Exception("Please configure your Chorus API key");
            }

            ChorusConfiguration config = ChorusConfigManager.INSTANCE.readConfig();
            List<BasicNameValuePair> queryParams = new ArrayList<BasicNameValuePair>();
            queryParams.add(new BasicNameValuePair(API_KEY, key));
            String url = buildURLWithParams(config.getURLBase() + "/notes", queryParams);
            HttpClient httpClient = getHttpClientWithTimeout();
            HttpPost httpPost = new HttpPost(url);
            List<BasicNameValuePair> nvps = new ArrayList<BasicNameValuePair>();
            if (isInsight.equalsIgnoreCase("1")) {
                nvps.add(new BasicNameValuePair(INSIGHT, "1"));
            }
            nvps.add(new BasicNameValuePair(ENTITY_TYPE, entityType));
            nvps.add(new BasicNameValuePair(ENTITY_ID, entityId));
            nvps.add(new BasicNameValuePair("body", body));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            HttpResponse httpResponse = httpClient.execute(httpPost);

            BasicResponseHandler handler = new BasicResponseHandler();
            //String body = handler.handleResponse(httpResponse);
            //System.out.println(body);
            EntityUtils.consume(httpResponse.getEntity());
            returnSuccess(response);

        } catch (Exception e ) {
            generateErrorDTO(response, e, request.getLocale());
        }

    }

    private String buildURLWithParams(String baseURL, List<BasicNameValuePair> queryParams) {
        StringBuilder sb = new StringBuilder(baseURL);
        if (sb.charAt(sb.length()-1) != '?') {
            sb.append("?");
        }
        return sb.append(URLEncodedUtils.format(queryParams, Consts.UTF_8)).toString();
    }

    private String getSnapshotID() {
        return UUID.randomUUID().toString();
    }

    private HttpClient getHttpClientWithTimeout(int timeout) {
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        return new DefaultHttpClient(httpParams);
    }

    private HttpClient getHttpClientWithTimeout() {
        return getHttpClientWithTimeout(10000);
    }

    private String getBaseURL(String requestURL) {
        int index = requestURL.lastIndexOf("/main/chorus.do");
        return requestURL.substring(0, index);

    }

    public static class ChorusComment {

        private String comment;

        public ChorusComment() {
            this.comment = "";
        }

        public ChorusComment(String _comment) {
            this.comment = _comment;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

}
