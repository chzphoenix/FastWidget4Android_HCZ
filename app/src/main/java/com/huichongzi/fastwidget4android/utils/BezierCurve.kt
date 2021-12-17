package com.huichongzi.fastwidget4android.utils

open class ThirdOrderBezierCurve(private val p0: FloatArray, private val p1: FloatArray, val p2: FloatArray) {

    open fun getPointBy01(p : FloatArray) : FloatArray{
        if(p[0] == p0[0] && p[1] == p0[1]){
            return p0
        }
        if(p[0] == p2[0] && p[1] == p2[1]){
            return p2
        }
        var res = FloatArray(2)
        var t = 0f
        if(p0[0] == p1[0]){
            t = (p0[1] - p[1]) / (p0[1] - p1[1])
        }
        else{
            t = (p0[0] - p[0]) / (p0[0] - p1[0])
        }
        var qx = p1[0] - t * (p1[0] - p2[0])
        var qy = p1[1] - t * (p1[1] - p2[1])
        res[0] = p[0] - t * (p[0] - qx)
        res[1] = p[1] - t * (p[1] - qy)
        return res
    }

    open fun getPointBy12(p : FloatArray) : FloatArray{
        if(p[0] == p0[0] && p[1] == p0[1]){
            return p0
        }
        if(p[0] == p2[0] && p[1] == p2[1]){
            return p2
        }
        var t = 0f
        if(p0[0] == p1[0]){
            t = (p0[1] - p[1]) / (p0[1] - p1[1])
        }
        else{
            t = (p0[0] - p[0]) / (p0[0] - p1[0])
        }
        var qx = p0[0] - t * (p0[0] - p1[0])
        var qy = p0[1] - t * (p0[1] - p1[1])
        var res = FloatArray(2)
        res[0] = qx - t * (qx - p[0])
        res[1] = qy - t * (qy - p[1])
        return res
    }
}