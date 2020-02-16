package com.metricsfab.utils.http;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


public class RestApiClient extends AsyncTask<RestApiParameter, Integer, JSONObject>
{

    /*

     */
    private IRestApiOperationCompleted restApiOperationCompleted;


    /*
        Constructor de la clase
     */
    public  RestApiClient(IRestApiOperationCompleted restApiOperationCompleted)
    {
        this.restApiOperationCompleted = restApiOperationCompleted;
    }


    private HttpsURLConnection setForGETMethod(URL paramURL) throws IOException
    {
        return (HttpsURLConnection)paramURL.openConnection();
    }

    private HttpsURLConnection setForPOSTMethod(URL paramURL, Map<String, String> paramMap) throws IOException
    {
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection)paramURL.openConnection();
        Uri.Builder builder = new Uri.Builder();

        for (Map.Entry entry : paramMap.entrySet())
        {
            builder.appendQueryParameter(entry.getKey().toString(), entry.getValue().toString());

        }

        httpsURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpsURLConnection.setDoOutput(true);
        String str = builder.build().getEncodedQuery();
        PrintWriter printWriter = new PrintWriter(httpsURLConnection.getOutputStream());
        printWriter.print(str);
        printWriter.flush();
        printWriter.close();
        return httpsURLConnection;
    }

    protected JSONObject doInBackground(RestApiParameter... restApiParameters)
    {
        if (restApiParameters != null) {
            try {
                if (restApiParameters.length > 0)
                {
                    HttpsURLConnection httpsURLConnection = null;
                    if (Debug.isDebuggerConnected())
                        Debug.waitForDebugger();

                    RestApiParameter parameter = restApiParameters[0];
                    URL uRL = new URL(parameter.protocol, parameter.hostname, parameter.port, parameter.url);


                    int method = parameter.method;
                    if (method == RestApiParameter.POST)
                    {
                            httpsURLConnection = setForPOSTMethod(uRL, parameter.parameters);
                    }
                    else if(method == RestApiParameter.GET)
                    {
                        httpsURLConnection = setForGETMethod(uRL);
                    }

                    if(httpsURLConnection != null)
                    {
                        JSONObject jSONObject = new JSONObject();

                        InputStream inputStream = null;
                        if (httpsURLConnection.getResponseCode() < 400)
                        {
                            inputStream = httpsURLConnection.getInputStream();
                        }
                        else
                        {
                            inputStream = httpsURLConnection.getErrorStream();
                        }

                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                        String str = "";
                        while (true)
                        {
                            String str1 = bufferedReader.readLine();
                            if (str1 != null)
                            {
                                StringBuilder stringBuilder = new StringBuilder();
                                stringBuilder.append(str);
                                stringBuilder.append(str1);
                                str = stringBuilder.toString();
                                continue;
                            }
                            break;
                        }

                        jSONObject.put("Content", str);
                        jSONObject.put("Message", httpsURLConnection.getResponseMessage());
                        jSONObject.put("Length", httpsURLConnection.getContentLength());
                        jSONObject.put("Type", httpsURLConnection.getContentType());

                        Log.d("Client API Rest", str);
                        return new JSONObject(str);
                    }
                    else
                    {

                    }

                }
                return new JSONObject();
            }
            catch (IOException exception)
            {
                Log.d("ClientApiRest", exception.getMessage());
            }
            catch (JSONException jsonException)
            {
                Log.d("ClientApiRest", jsonException.getMessage());
            }


        }
        return new JSONObject();
    }


    protected void onPostExecute(JSONObject paramJSONObject)
    {
        this.restApiOperationCompleted.completed(paramJSONObject);
    }


    protected void onPreExecute() { super.onPreExecute(); }


    protected void onProgressUpdate(Integer... paramVarArgs) {}



}
