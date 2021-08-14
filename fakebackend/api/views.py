from django.shortcuts import render
from django.http import JsonResponse
import json
import requests
from urllib import parse

# Create your views here.

SESSION = "guohao"
PASSWORD = "guohao"
EDUKG_ID = ""

HEADERS = {
    "Content-Type": "application/x-www-form-urlencoded;charset=utf-8"
}

cur_data = {
    "history": [],
    "favourite": []
}


def hello(request):
    return JsonResponse({
        "data": "hello, world!"
    })


def login(request):
    post_json = json.loads(request.body)
    user = post_json.get("user", "")
    pw = post_json.get("passwd", "")
    if user and pw and pw == PASSWORD:
        return JsonResponse({
            "ok": 1,
            "session": SESSION,
            "msg": f"login succeeded, user: {user}, pw: {pw}"
        })
    else:
        return JsonResponse({
            "ok": 0,
            "session": "",
            "msg": "login failed"
        })


def register(request):
    post_json = json.loads(request.body)
    user = post_json.get("user", "")
    pw = post_json.get("passwd", "")
    data = post_json.get("data", "")
    if user and pw:
        return JsonResponse({
            "ok": 1,
            "msg": f"register succeeded, user: {user}, pw: {pw}, data: {data}"
        })
    else:
        return JsonResponse({
            "ok": 0,
            "msg": "register failed"
        })


def userdata(request):
    global cur_data
    post_json = json.loads(request.body)
    sess = post_json.get("session", "")
    data = post_json.get("data", "")
    if data:
        cur_data = json.loads(data)
    if sess == SESSION:
        if data:
            return JsonResponse({
                "ok": 1,
                "data": json.dumps(data)
            })
        else:
            return JsonResponse({
                "ok": 1,
                "data": json.dumps(cur_data)
            })
    else:
        return JsonResponse({
            "ok": 0,
            "data": ""
        })


def __login():
    global EDUKG_ID
    url = "http://open.edukg.cn/opedukg/api/typeAuth/user/login"
    data = {
        "phone": 15049957199,
        "password": "Vg0jhog4SG461VhXx7Zq"
    }
    res = requests.post(url, data=parse.urlencode(data), headers=HEADERS)
    EDUKG_ID = json.loads(res.text).get("id", "")


def __get_res(url, method, data):
    global EDUKG_ID
    data["id"] = EDUKG_ID
    if method == "GET":
        res = requests.get(url, params=data, headers=HEADERS)
    elif method == "POST":
        res = requests.post(url, data=parse.urlencode(data), headers=HEADERS)
    return json.loads(res.text)


def proc(request):
    global EDUKG_ID
    post_json = json.loads(request.body)
    sess = post_json.get("session", "")
    url = post_json.get("url", "")
    method = post_json.get("method", "")
    data = json.loads(post_json.get("data", ""))
    if sess == SESSION and url and method:
        json_res = __get_res(url, method, data)
        if json_res.get("code", "-1") == "-1":
            __login()
            json_res = __get_res(url, method, data)
        return JsonResponse(json_res)
    else:
        return JsonResponse({})
