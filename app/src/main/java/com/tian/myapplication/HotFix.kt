package com.tian.myapplication

import android.util.Log
import java.io.File
import java.io.IOException
import java.lang.reflect.Method

/**
 *  create by txm  on 2020/8/2
 *  desc
 */
class HotFix {
    val  TAG = "HotFix"

    var classLoader :ClassLoader? = null
    var appApplication1: AppApplication1   ? = null

    constructor(appApplication1: AppApplication1 ) {
        this.appApplication1 = appApplication1
        this.classLoader = appApplication1.classLoader
    }

    private fun getFiledObj(obj:Any?, fieldName:String?) : Any ? {
        if(obj == null || fieldName.isNullOrEmpty()) {
            return null
        }

        var classObj:Class<*> ?  = obj::class.java

        while (classObj != null) {
            kotlin.runCatching {
                val field = classObj?.getDeclaredField(fieldName)
                if(field != null) {
                    field.isAccessible = true
                    return field.get(obj)
                }
            }.getOrElse {
                it.printStackTrace()
            }
            classObj = classObj.superclass
        }
        return null
    }

    private fun setFiled(obj:Any?,fieldName:String?,fieldValue: Any?)   {
        if(obj == null || fieldName.isNullOrEmpty()) {
            return
        }

        val classObj = obj::class.java
        kotlin.runCatching {
            val field = classObj.getDeclaredField(fieldName)
            field.isAccessible = true
            field.set(obj,fieldValue)
        }.getOrElse {
            it.printStackTrace()
            return
        }
    }




   private  fun getMethod(
       obj:Any?,
       methodName:String?, vararg parameterTypes: Class<*>?
   ) : Method ? {
        if(obj == null || methodName.isNullOrEmpty()) {
            return null
        }

        val classObj = obj::class.java
        kotlin.runCatching {
            val method = classObj.getDeclaredMethod(methodName,*parameterTypes)
            method.isAccessible = true
            return method
        }.getOrElse {
            it.printStackTrace()
            return null
        }
    }


    fun loadLocalDex() {
        //加载本地dex

        val path = appApplication1?.externalCacheDir?.absolutePath

        val dexFile = File("$path/test.dex")

        if(!dexFile.exists()) {
            Log.v(TAG,"dexFile 未查询到," + dexFile.absolutePath)
            return
        }

        val pathListObj = getFiledObj(classLoader,"pathList")


        //获取 DexPathList的makePathElements的方法
        val method = getMethod(pathListObj,"makePathElements", List::class.java,File::class.java,List::class.java)
            if(method == null) {
            Log.v(TAG,"makePathElements 未查询到")
            return
        }

        //调用makePathElements的方法
        val file =  appApplication1!!.cacheDir
        val localElements = method.invoke(null, arrayListOf(dexFile),file,ArrayList<IOException>())
        if(localElements == null) {
            Log.v(TAG,"localElements 未查询到")
            return
        }

        //获取DexPathList的dexElements的方法
        val dexElements = getFiledObj(pathListObj,"dexElements")
        if(dexElements == null) {
            Log.v(TAG,"dexElements 未查询到")
            return
        }

        //拼接，把本地的dex放置在前面
        val localElementArray = localElements as Array<Any>
        val dexElementArray = dexElements as Array<Any>

        //合并后的数组
        val newElements = java.lang.reflect.Array.newInstance(
            dexElements.javaClass.componentType,
            localElementArray.size + dexElementArray.size
        ) as Array<Any>


        // 先拷贝新数组
        System.arraycopy(localElementArray, 0, newElements, 0, localElementArray.size)
        System.arraycopy(dexElementArray, 0, newElements, localElementArray.size, dexElementArray.size)




        //给DexPathList 的 dexElements 赋值
        setFiled(pathListObj,"dexElements",newElements)
    }
}