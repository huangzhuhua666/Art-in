package com.tw.artin.http

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.BusUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.NetworkUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.gson.GsonBuilder
import com.hjq.http.EasyLog
import com.hjq.http.config.IRequestHandler
import com.hjq.http.exception.*
import com.hjq.http.exception.ResponseException
import com.tw.artin.R
import com.tw.artin.http.model.HttpData
import com.tw.artin.http.model.UpLoadData
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import java.net.UnknownHostException


class RequestHandler(val application : Application) : IRequestHandler{

    var mGson : GsonBuilder? = null

    override fun requestFail(lifecycle: LifecycleOwner?, e: Exception): Exception {

        if (e is SocketTimeoutException){

            return TimeoutException(application.getString(R.string.http_server_out_time),e)

        }else if(e is UnknownHostException){

            if (NetworkUtils.isConnected()){

                return ServerException(application.getString(R.string.http_server_error), e);
            }else{

                return  NetworkException(application.getString(R.string.http_network_error), e);
            }

        }else if (e is IOException){

            return CancelException(application.getString(R.string.http_request_cancel), e)
        }else{

            return  HttpException(e.message,e)
        }

    }

    override fun requestSucceed(lifecycle: LifecycleOwner?, response: Response, type: Type): Any {

        if (!response.isSuccessful){// 返回响应异常
            throw ResponseException(
                application.getString(R.string.http_server_error),response)
        }

        if (Response::class.java == type) {
            return response
        }

        response.body()?.run {

            if (Bitmap::class.java == type) { // 如果这是一个 Bitmap 对象
                return BitmapFactory.decodeStream(this.byteStream())
            }

            val text : String= try {
                string()
            } catch (e: IOException) { // 返回结果读取异常
                throw DataException(application.getString(R.string.http_data_explain_error), e)
            }

            // 打印这个 Json
            //EasyLog.json(text)
            EasyLog.print(text)

            var result: Any

            if (String::class.java == type) { // 如果这是一个 String 对象
                result = text
            } else if (JSONObject::class.java == type) {
                result = try { // 如果这是一个 JSONObject 对象
                    JSONObject(text)
                } catch (e: JSONException) {
                    throw DataException(application.getString(R.string.http_data_explain_error), e)
                }
            }else if (JSONArray::class.java == type) {

                result = try { // 如果这是一个 JSONArray 对象
                    JSONArray(text)
                } catch (e: JSONException) {
                    throw DataException(application.getString(R.string.http_data_explain_error), e)
                }

            }else {

               result = GsonUtils.fromJson(text,type)

                if (result is HttpData<*>){

                    if (result.type == "success"){// 代表执行成功

                    }else{

                        throw ResultException(result.content, result)
                    }

                }else if (result is UpLoadData){

                    if (result.message.type == "success"){

                    }else{
                        throw ResultException(result.message.content, result)
                    }
                }

            }

            return result

        }

        return Any()
    }

}