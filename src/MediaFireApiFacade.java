import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * rest api ex with mediafire:
 * http://users.jyu.fi/~miselico/teaching/TIES456/exercises/week37/
 * rest doc:
 * https://www.ibm.com/developerworks/webservices/library/ws-restful/
 *
 * Created by Tamar on 10/03/2016.
 */
public class MediaFireApiFacade {

    private String BASE_URL = "https://www.mediafire.com/api";;

    public User getUser(String appId, String apiKey, String email,String password) throws IOException, JSONException, URISyntaxException {
        String signature = DigestUtils.sha1Hex(email + password + appId + apiKey);
        String sessionToken = getSessionToken(appId, email, signature, password);

        String url = buildUrlForUserData(signature, BASE_URL, sessionToken);
        JSONObject userData = makeCallForUserInfo(url);

        return createUserFromData(userData);
    }

    private String getSessionToken(String appId, String email, String signature, String password) throws URISyntaxException, IOException, JSONException {
        String url = buildUrlForSessionToken(appId, email, signature, BASE_URL, password);
        return makeCallForSessionToken(url);
    }

    private User createUserFromData(JSONObject userData) throws JSONException {
        User user = new User();
        user.setDisplayName(userData.getString("display_name"));
        user.setEmail(userData.getString("email"));
        user.setBirthDate(userData.getString("birth_date"));
        user.setFirstName(userData.getString("first_name"));
        user.setLastName(userData.getString("last_name"));
        user.setGender(userData.getString("gender"));
        return user;
    }

    private String buildUrlForUserData(String signature, String baseUrl, String sessionToken) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(baseUrl + "/1.4/user/get_info.php"); //$NON-NLS-1$
        builder.addParameter("session_token", sessionToken);
        builder.addParameter("response_format", "json");
        builder.addParameter("signature", signature);
        return builder.toString();
    }

    private String buildUrlForSessionToken(String appId, String email, String signature, String baseUrl, String password) throws URISyntaxException {
        URIBuilder builder = new URIBuilder(baseUrl + "/user/get_session_token.php"); //$NON-NLS-1$
        builder.addParameter("application_id", appId);
        builder.addParameter("signature", signature);
        builder.addParameter("email", email);
        builder.addParameter("password", password);
        builder.addParameter("token_version", "1");
        builder.addParameter("response_format", "json");
        return builder.toString();
    }

    private String makeCallForSessionToken(String url) throws IOException, JSONException {
        Content content = Request.Get(url).execute().returnContent();
        JSONObject json = new JSONObject(content.toString());
        JSONObject result = json.getJSONObject("response");
        return result.getString("session_token");
    }

    private JSONObject makeCallForUserInfo(String url) throws IOException, JSONException {
        Content content = Request.Get(url).execute().returnContent();
        JSONObject json = new JSONObject(content.toString());
        JSONObject result = json.getJSONObject("response");
        return result.getJSONObject("user_info");
    }
}
